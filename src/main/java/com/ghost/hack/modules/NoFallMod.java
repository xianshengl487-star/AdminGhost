package com.ghost.hack.modules;

import com.ghost.hack.HackModule;

public class NoFallMod extends HackModule {
    public NoFallMod() { super("NoFall", "\u65e0\u5760\u843d\u4f24\u5bb3 - \u53d6\u6d88\u6389\u843d\u4f24\u5bb3", "\u79fb\u52a8"); }

    @Override public void onTick() {
        if (mc.player == null) return;
        if (mc.player.fallDistance > 2.0f) {
            mc.player.fallDistance = 0;
        }
    }
}
