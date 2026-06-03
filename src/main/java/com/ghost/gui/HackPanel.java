package com.ghost.gui;

import com.ghost.hack.HackModule;
import com.ghost.hack.ModuleManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Hack Panel - F7 to open.
 * Collapsible categories for hack modules with toggle + keybind.
 * Works as overlay on top of any screen.
 */
public class HackPanel extends Screen {
    // --- Data ---
    private static final List<HackCategory> categories = new ArrayList<>();
    private static final Map<String, Integer> moduleKeybinds = new HashMap<>();
    private static boolean inited = false;

    // --- UI State ---
    private int scrollOffset = 0;
    private String bindingModule = null;
    private String statusMsg = "";
    private int statusTimer = 0;

    // --- Layout ---
    private static final int PANEL_W = 480;
    private static final int ROW_H = 22;
    private static final int CAT_H = 26;
    private int panelX, panelY, panelH;

    public HackPanel() { super(Component.literal("AdminGhost-Hacks")); }

    @Override protected void init() {
        if (!inited) { loadKeybinds(); buildEntries(); inited = true; }
        panelX = (this.width - PANEL_W) / 2;
        panelY = 8;
        panelH = this.height - 16;
    }

    @Override public boolean isPauseScreen() { return false; }

    @Override public boolean mouseScrolled(double mx, double my, double delta) {
        scrollOffset -= (int)(delta * 3);
        scrollOffset = Math.max(0, scrollOffset);
        return true;
    }

    // ==================== RENDERING ====================

    @Override public void render(GuiGraphics gfx, int mx, int my, float pt) {
        // Semi-transparent background
        gfx.fill(0, 0, this.width, this.height, 0xAA000000);

        // Panel
        gfx.fill(panelX, panelY, panelX + PANEL_W, panelY + panelH, 0xF0101018);
        gfx.fill(panelX, panelY, panelX + PANEL_W, panelY + 2, 0xFF00AAFF);
        gfx.fill(panelX, panelY + panelH - 2, panelX + PANEL_W, panelY + panelH, 0xFF00AAFF);
        gfx.fill(panelX, panelY, panelX + 3, panelY + panelH, 0xFF00AAFF);

        Font f = this.font;
        gfx.drawCenteredString(f, "\u00a7b\u00a7lAdminGhost \u00a77HACK\u9762\u677f \u00a7f| \u00a7e\u5de6\u952e=\u5f00/\u5173  \u00a77\u53f3\u952e=\u7ed1\u5b9a  \u00a77Right Shift=\u6f0f\u6d1e\u9762\u677f", panelX + PANEL_W / 2, panelY + 6, 0xFFFFFF);

        // Build visible list
        List<VisEntry> visible = buildVisibleList();
        int contentY = panelY + 24;
        int contentH = panelH - 30;
        int maxVisible = contentH / ROW_H;
        int totalRows = visible.size();
        int maxScroll = Math.max(0, totalRows - maxVisible);
        scrollOffset = Math.min(scrollOffset, maxScroll);

        int startIdx = scrollOffset;
        int endIdx = Math.min(totalRows, startIdx + maxVisible);

        for (int i = startIdx; i < endIdx; i++) {
            VisEntry ve = visible.get(i);
            int rowY = contentY + (i - startIdx) * ROW_H;

            if (ve.isCategory()) {
                gfx.fill(panelX + 4, rowY, panelX + PANEL_W - 4, rowY + CAT_H - 2, 0xFF222230);
                gfx.fill(panelX + 4, rowY, panelX + 8, rowY + CAT_H - 2, 0xFF00AAFF);
                String arrow = ve.cat.collapsed ? "\u25b6" : "\u25bc";
                gfx.drawString(f, "\u00a7b\u00a7l" + arrow + " " + ve.cat.name, panelX + 14, rowY + 5, 0xFFFFFF);
                int count = ve.cat.modules.size();
                gfx.drawString(f, "\u00a77(" + count + ")", panelX + PANEL_W - 50, rowY + 5, 0x888888);
            } else {
                HackModule m = ve.mod;
                boolean hover = mx >= panelX + 8 && mx <= panelX + PANEL_W - 8 && my >= rowY && my < rowY + ROW_H;
                gfx.fill(panelX + 8, rowY, panelX + PANEL_W - 8, rowY + ROW_H - 2, hover ? 0xFF2A2A3A : 0xFF181822);
                if (hover) gfx.fill(panelX + 8, rowY, panelX + 11, rowY + ROW_H - 2, 0xFF00AAFF);

                // Toggle indicator
                boolean on = m.isEnabled();
                gfx.drawString(f, on ? "\u00a7a\u2611" : "\u00a7c\u2610", panelX + 16, rowY + 4, 0);
                // Name
                gfx.drawString(f, (on ? "\u00a7a" : "\u00a7f") + m.getName(), panelX + 34, rowY + 4, on ? 0xFF55FF55 : 0xFFDDDDDD);
                // Description
                gfx.drawString(f, "\u00a77" + m.getDesc(), panelX + 130, rowY + 4, 0xFF888888);

                // Keybind
                int kb = moduleKeybinds.getOrDefault(m.getName(), -1);
                if (bindingModule != null && bindingModule.equals(m.getName())) {
                    int blink = (int)(System.currentTimeMillis() / 300) % 2;
                    gfx.drawString(f, blink == 0 ? "\u00a7c\u00a7l[\u6309\u952e\u4e2d..]" : "\u00a76\u00a7l[\u6309\u952e\u4e2d..]", panelX + PANEL_W - 95, rowY + 4, 0);
                } else if (kb > 0) {
                    String kbName = GLFW.glfwGetKeyName(kb, 0);
                    if (kbName == null) kbName = "Key" + kb;
                    gfx.fill(panelX + PANEL_W - 95, rowY + 2, panelX + PANEL_W - 8, rowY + ROW_H - 4, 0xFF333344);
                    gfx.drawCenteredString(f, "\u00a7b[" + kbName + "]", panelX + PANEL_W - 50, rowY + 4, 0);
                } else {
                    gfx.drawString(f, "\u00a78[\u53f3\u952e\u7ed1\u5b9a]", panelX + PANEL_W - 95, rowY + 4, 0);
                }
            }
        }

        // Scrollbar
        if (totalRows > maxVisible) {
            float ratio = (float) scrollOffset / Math.max(1, maxScroll);
            int barH = Math.max(16, (int)((float) maxVisible / totalRows * contentH));
            int barY = contentY + (int)(ratio * (contentH - barH));
            gfx.fill(panelX + PANEL_W - 4, contentY, panelX + PANEL_W - 1, contentY + contentH, 0xFF333333);
            gfx.fill(panelX + PANEL_W - 4, barY, panelX + PANEL_W - 1, barY + barH, 0xFF888888);
        }

        // Status
        if (statusTimer > 0) {
            statusTimer--;
            int sc = statusMsg.startsWith("[OK]") ? 0xFF55FF55 : statusMsg.startsWith("[X]") ? 0xFFFF5555 : 0xFFAAAA00;
            gfx.fill(panelX, panelY + panelH - 18, panelX + PANEL_W, panelY + panelH, 0xEE111111);
            gfx.drawString(f, "\u00a7f" + statusMsg, panelX + 8, panelY + panelH - 14, sc);
        }

        super.render(gfx, mx, my, pt);
    }

    // ==================== MOUSE ====================

    @Override public boolean mouseClicked(double mx, double my, int btn) {
        if (btn == 0 || btn == 1) {
            List<VisEntry> visible = buildVisibleList();
            int contentY = panelY + 24;
            int contentH = panelH - 30;
            int maxVisible = contentH / ROW_H;

            for (int i = scrollOffset; i < Math.min(visible.size(), scrollOffset + maxVisible); i++) {
                VisEntry ve = visible.get(i);
                int rowY = contentY + (i - scrollOffset) * ROW_H;

                if (mx >= panelX + 4 && mx <= panelX + PANEL_W - 4 && my >= rowY && my < rowY + ROW_H) {
                    if (ve.isCategory()) {
                        ve.cat.collapsed = !ve.cat.collapsed;
                        scrollOffset = 0;
                        return true;
                    }

                    HackModule m = ve.mod;
                    if (btn == 0) {
                        // Left click = toggle
                        m.toggle();
                        boolean on = m.isEnabled();
                        statusMsg = on ? "[OK] " + m.getName() + " \u5f00\u542f" : "[OK] " + m.getName() + " \u5173\u95ed";
                        statusTimer = 60;
                    } else {
                        // Right click = bind key
                        bindingModule = m.getName();
                        statusMsg = "\u00a7e\u6309\u4e00\u4e2a\u952e\u7ed1\u5b9a\u5230: " + m.getName();
                        statusTimer = 200;
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, btn);
    }

    @Override public boolean keyPressed(int keyCode, int scanCode, int mods) {
        if (bindingModule != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                bindingModule = null;
                statusMsg = "[X] \u53d6\u6d88\u7ed1\u5b9a";
                statusTimer = 60;
            } else {
                // Save keybind to module
                HackModule m = ModuleManager.getByName(bindingModule);
                if (m != null) {
                    m.setKeyBind(keyCode);
                    moduleKeybinds.put(bindingModule, keyCode);
                    saveKeybinds();
                    statusMsg = "[OK] " + bindingModule + " \u7ed1\u5b9a\u5b8c\u6210";
                }
                statusTimer = 60;
                bindingModule = null;
            }
            return true;
        }

        // F7 or Right Shift = close
        if (keyCode == GLFW.GLFW_KEY_F7 || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            this.onClose();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, mods);
    }

    // ==================== VISIBLE LIST ====================

    private List<VisEntry> buildVisibleList() {
        List<VisEntry> list = new ArrayList<>();
        for (HackCategory cat : categories) {
            list.add(new VisEntry(cat, null));
            if (!cat.collapsed) {
                for (HackModule m : cat.modules) {
                    list.add(new VisEntry(null, m));
                }
            }
        }
        return list;
    }

    private static class VisEntry {
        final HackCategory cat;
        final HackModule mod;
        VisEntry(HackCategory c, HackModule m) { cat = c; mod = m; }
        boolean isCategory() { return cat != null; }
    }

    // ==================== DATA ====================

    public static class HackCategory {
        String name;
        boolean collapsed = false;
        List<HackModule> modules = new ArrayList<>();
        HackCategory(String n) { name = n; }
    }

    // ==================== BUILD ====================

    private void buildEntries() {
        HackCategory move = new HackCategory("\u00a7e\u79fb\u52a8\u00a7r \u6a21\u5757");
        move.modules.addAll(ModuleManager.getByCategory("\u79fb\u52a8"));
        categories.add(move);

        HackCategory combat = new HackCategory("\u00a7c\u6218\u6597\u00a7r \u6a21\u5757");
        combat.modules.addAll(ModuleManager.getByCategory("\u6218\u6597"));
        categories.add(combat);

        HackCategory render = new HackCategory("\u00a7d\u6e32\u67d3\u00a7r \u6a21\u5757");
        render.modules.addAll(ModuleManager.getByCategory("\u6e32\u67d3"));
        categories.add(render);

        HackCategory player = new HackCategory("\u00a7a\u73a9\u5bb6\u00a7r \u6a21\u5757");
        player.modules.addAll(ModuleManager.getByCategory("\u73a9\u5bb6"));
        categories.add(player);

        // Restore keybinds
        for (HackCategory cat : categories) {
            for (HackModule m : cat.modules) {
                int saved = moduleKeybinds.getOrDefault(m.getName(), -1);
                if (saved > 0) m.setKeyBind(saved);
            }
        }
    }

    // ==================== KEYBIND PERSISTENCE ====================

    private static void loadKeybinds() {
        try {
            Path p = Path.of("config/adminghost_hack_keybinds.json");
            if (Files.exists(p)) {
                String json = Files.readString(p);
                Map<String, Integer> loaded = new Gson().fromJson(json, new TypeToken<Map<String, Integer>>(){}.getType());
                if (loaded != null) moduleKeybinds.putAll(loaded);
            }
        } catch (Exception e) { com.ghost.AdminGhost.LOGGER.warn("[HackPanel] Keybind error: " + e.getMessage()); }
    }

    private static void saveKeybinds() {
        try {
            Path dir = Path.of("config");
            if (!Files.exists(dir)) Files.createDirectories(dir);
            Files.writeString(Path.of("config/adminghost_hack_keybinds.json"), new Gson().toJson(moduleKeybinds));
        } catch (Exception e) { com.ghost.AdminGhost.LOGGER.warn("[HackPanel] Keybind error: " + e.getMessage()); }
    }
}