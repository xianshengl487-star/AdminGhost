package com.ghost.hack.modules;

import com.ghost.hack.HackModule;

public class FullBrightMod extends HackModule {
    private double oldGamma = 1.0;

    public FullBrightMod() { super("FullBright", "\u6700\u5927\u4eae\u5ea6 - \u65e0\u9650\u5149\u660e", "\u6e32\u67d3"); }

    @Override protected void onEnable() {
        oldGamma = mc.options.gamma().get();
        mc.options.gamma().set(15.0);
    }

    @Override protected void onDisable() {
        mc.options.gamma().set(oldGamma);
    }
}
