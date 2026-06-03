package com.ghost.hack.modules;

import com.ghost.hack.HackModule;

public class SprintMod extends HackModule {
    public SprintMod() { super("AutoSprint", "\u81ea\u52a8\u51b2\u523a - \u59cb\u7ec8\u51b2\u523a", "\u79fb\u52a8"); }

    @Override public void onTick() {
        if (mc.player == null) return;
        if (mc.player.input.up && !mc.player.isSprinting() && mc.player.getFoodData().getFoodLevel() > 6) {
            mc.player.setSprinting(true);
        }
    }
}
