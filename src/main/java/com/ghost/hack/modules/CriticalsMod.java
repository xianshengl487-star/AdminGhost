package com.ghost.hack.modules;

import com.ghost.hack.HackModule;

public class CriticalsMod extends HackModule {
    public CriticalsMod() { super("Criticals", "\u59cb\u7ec8\u66b4\u51fb - \u6bcf\u6b21\u653b\u51fb\u90fd\u662f\u66b4\u51fb", "\u6218\u6597"); }

    @Override public void onTick() {
        if (mc.player == null) return;
        // Small hop for critical hit before each attack
        if (mc.player.onGround() && mc.options.keyAttack.isDown()) {
            mc.player.setDeltaMovement(mc.player.getDeltaMovement().add(0, 0.05, 0));
        }
    }
}