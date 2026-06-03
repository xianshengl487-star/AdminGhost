package com.ghost.hack.modules;

import com.ghost.hack.HackModule;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class FlyMod extends HackModule {
    private double flySpeed = 0.05;

    public FlyMod() { super("Fly", "\u98de\u884c\u6a21\u5f0f - \u521b\u9020\u5f0f\u98de\u884c", "\u79fb\u52a8"); }

    @Override public void onTick() {
        Player p = mc.player;
        if (p == null) return;
        p.getAbilities().mayfly = true;
        p.getAbilities().flying = true;
        
        double dx = 0, dy = 0, dz = 0;
        if (mc.options.keyJump.isDown()) dy += flySpeed;
        if (mc.options.keyShift.isDown()) dy -= flySpeed;
        
        // Forward/backward movement
        if (mc.options.keyUp.isDown()) {
            double yaw = Math.toRadians(p.getYRot());
            dx -= Math.sin(yaw) * flySpeed;
            dz += Math.cos(yaw) * flySpeed;
        }
        if (mc.options.keyDown.isDown()) {
            double yaw = Math.toRadians(p.getYRot());
            dx += Math.sin(yaw) * flySpeed;
            dz -= Math.cos(yaw) * flySpeed;
        }
        
        if (dx != 0 || dy != 0 || dz != 0) {
            p.setDeltaMovement(dx, dy, dz);
        }
        p.onUpdateAbilities();
    }

    @Override protected void onDisable() {
        if (mc.player != null) {
            mc.player.getAbilities().flying = false;
            mc.player.getAbilities().mayfly = mc.player.isCreative();
            mc.player.onUpdateAbilities();
        }
    }
}