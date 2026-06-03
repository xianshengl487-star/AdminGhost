package com.ghost.hack.modules;

import com.ghost.hack.HackModule;

public class StepMod extends HackModule {
    private float stepHeight = 1.0f;

    public StepMod() { super("Step", "\u81ea\u52a8\u53f0\u9636 - \u65e0\u9700\u8df3\u8dc3\u4e0a\u65b9\u5757", "\u79fb\u52a8"); }

    @Override public void onTick() {
        if (mc.player == null) return;
        mc.player.setMaxUpStep(stepHeight);
    }

    @Override protected void onDisable() {
        if (mc.player != null) mc.player.setMaxUpStep(0.6f);
    }
}
