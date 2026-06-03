package com.ghost.hack.modules;

import com.ghost.hack.HackModule;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class KillAuraMod extends HackModule {
    private double range = 4.5;
    private int delay = 6;
    private int tickCounter = 0;

    public KillAuraMod() { super("KillAura", "\u81ea\u52a8\u653b\u51fb - \u81ea\u52a8\u653b\u51fb\u9644\u8fd1\u5b9e\u4f53", "\u6218\u6597"); }

    @Override public void onTick() {
        if (mc.player == null || mc.level == null) return;
        tickCounter++;
        if (tickCounter < delay) return;
        tickCounter = 0;

        LivingEntity closest = null;
        double best = range;
        for (Entity e : mc.level.entitiesForRendering()) {
            if (!(e instanceof LivingEntity le) || e == mc.player || !e.isAlive()) continue;
            double d = e.distanceTo(mc.player);
            if (d < best) { best = d; closest = le; }
        }
        if (closest != null) {
            mc.player.attack(closest);
            mc.player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        }
    }
}