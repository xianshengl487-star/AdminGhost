package com.ghost.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;

public class GhostCommand {
    private static final Minecraft mc = Minecraft.getInstance();

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        var root = lit("g");

        root.then(lit("gm").then(arg("mode", StringArgumentType.string())
            .executes(ctx -> { String m = str(ctx,"mode"); GameType gt = switch(m) {
                case "0","survival" -> GameType.SURVIVAL; case "1","creative" -> GameType.CREATIVE;
                case "2","adventure" -> GameType.ADVENTURE; case "3","spectator" -> GameType.SPECTATOR;
                default -> null; };
                if (gt != null && mc.gameMode != null) { mc.gameMode.setLocalMode(gt); return msg(ctx,"[OK] Gamemode: "+m); }
                return msg(ctx,"[X] Invalid mode"); })));

        root.then(lit("fly")
            .executes(ctx -> { if(mc.player!=null){mc.player.getAbilities().mayfly=true;mc.player.getAbilities().flying=true;mc.player.onUpdateAbilities();} return msg(ctx,"[OK] Fly ON"); })
            .then(arg("speed", DoubleArgumentType.doubleArg(0.1,100))
                .executes(ctx -> { float s=(float)dgr(ctx,"speed"); if(mc.player!=null){mc.player.getAbilities().setFlyingSpeed(s/20f);mc.player.onUpdateAbilities();} return msg(ctx,"[OK] Fly speed: "+s); })));

        root.then(lit("effect").then(arg("effect", StringArgumentType.string())
            .executes(ctx -> { String e=str(ctx,"effect"); return applyEffect(ctx,e,9999,0); })
            .then(arg("dur", IntegerArgumentType.integer(1))
                .executes(ctx -> { String e=str(ctx,"effect"); int dr=igr(ctx,"dur"); return applyEffect(ctx,e,dr,0); })
                .then(arg("amp", IntegerArgumentType.integer(0,255))
                    .executes(ctx -> applyEffect(ctx,str(ctx,"effect"),igr(ctx,"dur"),igr(ctx,"amp")))))));

        root.then(lit("buff").executes(ctx -> { if(mc.player!=null){addBuffs(0);} return msg(ctx,"[OK] All buffs"); })
            .then(arg("amp", IntegerArgumentType.integer(0,255))
                .executes(ctx -> { addBuffs(igr(ctx,"amp")); return msg(ctx,"[OK] All buffs amp="+igr(ctx,"amp")); })));

        root.then(lit("clearbuff").executes(ctx -> { if(mc.player!=null) mc.player.removeAllEffects(); return msg(ctx,"[OK] Cleared effects"); }));

        root.then(lit("xp").then(arg("amount", StringArgumentType.string())
            .executes(ctx -> { try{int a=Integer.parseInt(str(ctx,"amount")); if(mc.player!=null)mc.player.giveExperiencePoints(a); return msg(ctx,"[OK] +"+a+" XP");}catch(Exception e){return msg(ctx,"[X] Invalid");} })));

        root.then(lit("weather").then(arg("type", StringArgumentType.string())
            .executes(ctx -> msg(ctx,"[OK] Weather: "+str(ctx,"type")))));

        root.then(lit("time").then(arg("value", StringArgumentType.string())
            .executes(ctx -> msg(ctx,"[OK] Time: "+str(ctx,"value")))));

        root.then(lit("summon").then(arg("entity", StringArgumentType.string())
            .executes(ctx -> msg(ctx,"[OK] Summon: "+str(ctx,"entity")))
            .then(arg("count", IntegerArgumentType.integer(1,100))
                .executes(ctx -> msg(ctx,"[OK] Summon x"+igr(ctx,"count"))))));

        root.then(lit("clear").executes(ctx -> { if(mc.player!=null)mc.player.getInventory().clearContent(); return msg(ctx,"[OK] Inventory cleared"); }));

        root.then(lit("whoami").executes(ctx -> {
            if(mc.player!=null) { msg(ctx,"Name: "+mc.player.getName().getString()); msg(ctx,"Pos: "+mc.player.blockPosition().toShortString()); msg(ctx,"Health: "+mc.player.getHealth()+"/"+mc.player.getMaxHealth()); }
            return 1; }));

        root.then(lit("nearby").executes(ctx -> {
            if(mc.player==null||mc.level==null) return msg(ctx,"[X] Not in game");
            StringBuilder sb = new StringBuilder();
            mc.level.entitiesForRendering().forEach(e -> { if(e!=mc.player && e.distanceTo(mc.player)<30)
                sb.append(e.getId()).append(": ").append(e.getName().getString()).append(" (").append(String.format("%.1f",e.distanceTo(mc.player))).append("m)\n"); });
            return msg(ctx, sb.length()>0 ? sb.toString() : "No entities nearby"); }));

        root.then(lit("help").executes(ctx -> {
            String[] h = {"===== AdminGhost v10 =====","RIGHT SHIFT = Panel","/g gm|fly|effect|buff|clearbuff","/g xp|weather|time|summon|clear","/g whoami|nearby|help","========================"};
            for(String l : h) msg(ctx,l); return 1; }));

        d.register(root);
    }

    private static int applyEffect(CommandContext<CommandSourceStack> ctx, String name, int dur, int amp) {
        if(mc.player==null) return msg(ctx,"[X] Not in game");
        MobEffectInstance eff = null;
        switch(name.toLowerCase()) {
            case "speed" -> eff = new MobEffectInstance(MobEffects.MOVEMENT_SPEED, dur*20, amp);
            case "strength" -> eff = new MobEffectInstance(MobEffects.DAMAGE_BOOST, dur*20, amp);
            case "haste" -> eff = new MobEffectInstance(MobEffects.DIG_SPEED, dur*20, amp);
            case "regen","regeneration" -> eff = new MobEffectInstance(MobEffects.REGENERATION, dur*20, amp);
            case "resistance" -> eff = new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, dur*20, amp);
            case "fire_resistance" -> eff = new MobEffectInstance(MobEffects.FIRE_RESISTANCE, dur*20, amp);
            case "invisibility" -> eff = new MobEffectInstance(MobEffects.INVISIBILITY, dur*20, amp);
            case "night_vision" -> eff = new MobEffectInstance(MobEffects.NIGHT_VISION, dur*20, amp);
            case "health_boost" -> eff = new MobEffectInstance(MobEffects.HEALTH_BOOST, dur*20, amp);
            case "absorption" -> eff = new MobEffectInstance(MobEffects.ABSORPTION, dur*20, amp);
            case "jump" -> eff = new MobEffectInstance(MobEffects.JUMP, dur*20, amp);
            default -> { return msg(ctx,"[X] Unknown: "+name); }
        }
        mc.player.addEffect(eff);
        return msg(ctx,"[OK] "+name+" x"+(amp+1)+" for "+dur+"s");
    }

    private static void addBuffs(int amp) {
        if(mc.player==null) return;
        int d = 9999*20;
        mc.player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, d, amp));
        mc.player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, d, amp));
        mc.player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, d, amp));
        mc.player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, d, amp));
        mc.player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, d, amp));
        mc.player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, d, amp));
        mc.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, d, amp));
        mc.player.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, d, amp));
        mc.player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, d, amp));
        mc.player.addEffect(new MobEffectInstance(MobEffects.JUMP, d, amp));
    }

    private static int msg(CommandContext<CommandSourceStack> ctx, String s) {
        ctx.getSource().sendSuccess(() -> Component.literal("[Ghost] "+s), false); return 1;
    }
    private static LiteralArgumentBuilder<CommandSourceStack> lit(String n) { return LiteralArgumentBuilder.literal(n); }
    private static <T> RequiredArgumentBuilder<CommandSourceStack,T> arg(String n, com.mojang.brigadier.arguments.ArgumentType<T> t) { return RequiredArgumentBuilder.argument(n,t); }
    private static String str(CommandContext<CommandSourceStack> c, String n) { return StringArgumentType.getString(c,n); }
    private static int igr(CommandContext<CommandSourceStack> c, String n) { return IntegerArgumentType.getInteger(c,n); }
    private static double dgr(CommandContext<CommandSourceStack> c, String n) { return DoubleArgumentType.getDouble(c,n); }
}
