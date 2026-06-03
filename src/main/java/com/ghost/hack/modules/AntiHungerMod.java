package com.ghost.hack.modules;

import com.ghost.hack.HackModule;

public class AntiHungerMod extends HackModule {
    public AntiHungerMod() { super("AntiHunger", "\u9632\u9965\u997f - \u4e0d\u6d88\u8017\u9965\u997f\u503c", "\u73a9\u5bb6"); }

    @Override public void onTick() {
        if (mc.player == null) return;
        // Reset food exhaustion to 0
        mc.player.getFoodData().setExhaustion(0);
        // Keep food level high
        if (mc.player.getFoodData().getFoodLevel() < 18) {
            mc.player.getFoodData().setFoodLevel(20);
        }
    }
}
