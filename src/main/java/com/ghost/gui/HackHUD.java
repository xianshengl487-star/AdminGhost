package com.ghost.gui;

import com.ghost.hack.HackModule;
import com.ghost.hack.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import java.util.ArrayList;
import java.util.List;

public class HackHUD {
    private static final Minecraft mc = Minecraft.getInstance();
    private static boolean visible = true;
    public static void toggle() { visible = !visible; }
    public static boolean isVisible() { return visible; }

    public static void render(GuiGraphics gfx) {
        if (!visible || mc.player == null) return;
        Font f = mc.font;
        List<HackModule> active = new ArrayList<>();
        for (HackModule m : ModuleManager.getModules()) {
            if (m.isEnabled()) active.add(m);
        }
        if (active.isEmpty()) return;

        int x = 2, y = 2;
        int maxW = 0;
        for (HackModule m : active) {
            int w = f.width(m.getName());
            if (w > maxW) maxW = w;
        }
        // Background
        gfx.fill(x, y, x + maxW + 6, y + active.size() * 12 + 4, 0xAA000000);
        // Module names
        int cy = y + 2;
        for (HackModule m : active) {
            gfx.drawString(f, "\u00a7a" + m.getName(), x + 3, cy, 0xFF55FF55);
            cy += 12;
        }
    }
}
