package com.ghost.hack.modules;

import com.ghost.hack.HackModule;

public class NoSlowDownMod extends HackModule {
    public NoSlowDownMod() { super("NoSlowDown", "\u65e0\u51cf\u901f - \u4f7f\u7528\u7269\u54c1\u4d0d\u51cf\u901f", "\u79fb\u52a8"); }

    @Override public void onTick() {
        if (mc.player == null) return;
        // Reset slowdown by directly setting movement input speed
        mc.player.setSpeed(mc.player.isSprinting() ? 0.13f : 0.1f);
    }
}