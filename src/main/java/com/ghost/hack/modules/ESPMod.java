package com.ghost.hack.modules;

import com.ghost.hack.HackModule;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class ESPMod extends HackModule {
    private int tick = 0;

    public ESPMod() { super("ESP", "\u900f\u89c6 - \u7a7f\u5899\u770b\u5b9e\u4f53", "\u6e32\u67d3"); }

    @Override public void onTick() {
        tick++;
        if (tick % 20 != 0) return;
        if (mc.player == null || mc.level == null) return;
        for (Entity e : mc.level.entitiesForRendering()) {
            if (e instanceof LivingEntity && e != mc.player) {
                e.setGlowingTag(true);
            }
        }
    }

    @Override protected void onDisable() {
        if (mc.level == null) return;
        for (Entity e : mc.level.entitiesForRendering()) {
            if (e instanceof LivingEntity && e != mc.player) {
                e.setGlowingTag(false);
            }
        }
    }
}