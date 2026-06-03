package com.ghost.hack.modules;

import com.ghost.hack.HackModule;

public class NoClipMod extends HackModule {
    private double speed = 0.2;

    public NoClipMod() { super("NoClip", "\u7a7f\u5899\u6a21\u5f0f - \u7a7f\u8d8a\u65b9\u5757", "\u79fb\u52a8"); }

    @Override public void onTick() {
        if (mc.player == null) return;
        mc.player.noPhysics = true;
        mc.player.fallDistance = 0;
        mc.player.setDeltaMovement(0, 0, 0);
        if (mc.options.keyJump.isDown()) mc.player.setDeltaMovement(mc.player.getDeltaMovement().add(0, speed, 0));
        if (mc.options.keyShift.isDown()) mc.player.setDeltaMovement(mc.player.getDeltaMovement().add(0, -speed, 0));
    }

    @Override protected void onDisable() {
        if (mc.player != null) mc.player.noPhysics = false;
    }
}
