package com.ghost.hack.modules;

import com.ghost.hack.HackModule;
import net.minecraft.world.entity.Entity;

public class NametagsMod extends HackModule {
    private int tick = 0;

    public NametagsMod() { super("Nametags", "\u540d\u5b57\u6807\u7b7e - \u663e\u793a\u5168\u90e8\u540d\u5b57", "\u6e32\u67d3"); }

    @Override public void onTick() {
        tick++;
        if (tick % 40 != 0) return;
        if (mc.player == null || mc.level == null) return;
        for (Entity e : mc.level.entitiesForRendering()) {
            if (e != mc.player && !e.isCustomNameVisible()) {
                e.setCustomNameVisible(true);
            }
        }
    }

    @Override protected void onDisable() {
        if (mc.level == null) return;
        for (Entity e : mc.level.entitiesForRendering()) {
            if (e != mc.player) {
                e.setCustomNameVisible(false);
            }
        }
    }
}