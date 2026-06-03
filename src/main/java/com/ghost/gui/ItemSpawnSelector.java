package com.ghost.gui;

import com.ghost.exploit.ExploitSender;
import com.ghost.util.MasterSwitch;
import com.ghost.util.GameRegistry;
import com.ghost.util.GameRegistry.ItemEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import java.util.*;

/**
 * Item Spawn Selector GUI.
 * Browse categories of items, choose count, spawn via exploits.
 */
public class ItemSpawnSelector extends Screen {
    private Map<String, List<ItemEntry>> categories;
    private Map<String, Boolean> collapsed = new HashMap<>();
    private int spawnCount = 64;
    private int scrollOffset = 0;
    private long lastActionTime = 0;
    private static final long RATE_LIMIT_MS = 500;
    private EditBox searchBox;
    private EditBox countBox;
    private EditBox customIdBox;
    private String statusMsg = "";
    private int statusTimer = 0;

    private static final int PANEL_W = 520;
    private static final int ROW_H = 20;
    private static final int CAT_H = 24;
    private int panelX, panelY, panelH;

    public ItemSpawnSelector() { super(Component.literal("ItemSpawnSelector")); }

    @Override protected void init() {
        categories = GameRegistry.getCommonItems();
        panelX = (this.width - PANEL_W) / 2;
        panelY = 8;
        panelH = this.height - 16;

        searchBox = new EditBox(this.font, panelX + 80, panelY + 26, 160, 16, Component.literal("search"));
        searchBox.setMaxLength(64);
        searchBox.setBordered(true);
        searchBox.setVisible(true);
        searchBox.setHint(Component.literal("\u641c\u7d22\u7269\u54c1.."));
        this.addRenderableWidget(searchBox);

        countBox = new EditBox(this.font, panelX + 330, panelY + 26, 50, 16, Component.literal("count"));
        countBox.setMaxLength(3);
        countBox.setBordered(true);
        countBox.setVisible(true);
        countBox.setValue("64");
        this.addRenderableWidget(countBox);

        customIdBox = new EditBox(this.font, panelX + 80, panelY + 46, 300, 16, Component.literal("custom"));
        customIdBox.setMaxLength(128);
        customIdBox.setBordered(true);
        customIdBox.setVisible(true);
        customIdBox.setHint(Component.literal("\u81ea\u5b9a\u4e49ID: minecraft:item_name"));
        this.addRenderableWidget(customIdBox);
    }

    @Override public boolean isPauseScreen() { return false; }
    @Override public void tick() {
        if (searchBox != null) searchBox.tick();
        if (countBox != null) countBox.tick();
        if (customIdBox != null) customIdBox.tick();
    }
    @Override public boolean mouseScrolled(double mx, double my, double delta) {
        scrollOffset -= (int)(delta * 3);
        scrollOffset = Math.max(0, scrollOffset);
        return true;
    }

    // ==================== RENDER ====================

    @Override public void render(GuiGraphics gfx, int mx, int my, float pt) {
        gfx.fill(0, 0, this.width, this.height, 0xAA000000);
        gfx.fill(panelX, panelY, panelX + PANEL_W, panelY + panelH, 0xF0101018);
        gfx.fill(panelX, panelY, panelX + PANEL_W, panelY + 2, 0xFFFFAA00);
        gfx.fill(panelX, panelY + panelH - 2, panelX + PANEL_W, panelY + panelH, 0xFFFFAA00);
        gfx.fill(panelX, panelY, panelX + 3, panelY + panelH, 0xFFFFAA00);

        Font f = this.font;
        gfx.drawCenteredString(f, "\u00a76\u00a7l\u670d\u52a1\u7aef\u7269\u54c1\u83b7\u53d6 \u00a7f| \u00a7e\u5de6\u952e=\u751f\u6210  \u00a77\u53f3\u952e=\u751f\u6210\u5e76\u62fe\u53d6", panelX + PANEL_W / 2, panelY + 6, 0xFFFFFF);

        // Labels
        gfx.drawString(f, "\u00a7e\u641c\u7d22:", panelX + 10, panelY + 28, 0);
        gfx.drawString(f, "\u00a7e\u6570\u91cf:", panelX + 290, panelY + 28, 0);
        gfx.drawString(f, "\u00a7e\u81ea\u5b9a\u4e49:", panelX + 10, panelY + 48, 0);

        // Custom spawn button
        String customId = customIdBox.getValue().trim();
        if (!customId.isEmpty()) {
            int bx = panelX + 390;
            boolean btnHover = mx >= bx && mx < bx + 120 && my >= panelY + 44 && my < panelY + 62;
            gfx.fill(bx, panelY + 44, bx + 120, panelY + 62, btnHover ? 0xFFAA6600 : 0xFF884400);
            gfx.drawCenteredString(f, "\u00a7e\u751f\u6210\u81ea\u5b9a\u4e49\u7269\u54c1", bx + 60, panelY + 48, 0);
        }

        // Quick buttons row
        int quickY = panelY + 66;
        gfx.fill(panelX + 8, quickY, panelX + PANEL_W - 8, quickY + 22, 0xFF222230);
        gfx.drawString(f, "\u00a7e\u5feb\u6377\u751f\u6210:", panelX + 14, quickY + 4, 0);
        String[][] quickItems = {
            {"\u94bb\u77f3", "minecraft:diamond"},
            {"\u4e0b\u754c\u5408\u91d1", "minecraft:netherite_ingot"},
            {"\u4e0d\u6b7b\u56fe\u817e", "minecraft:totem_of_undying"},
            {"\u9644\u9b54\u82f9\u679c", "minecraft:enchanted_golden_apple"},
            {"\u9798\u7fc5", "minecraft:elytra"},
        };
        for (int i = 0; i < quickItems.length; i++) {
            int bx = panelX + 100 + i * 82;
            boolean h = mx >= bx && mx < bx + 78 && my >= quickY + 1 && my < quickY + 21;
            gfx.fill(bx, quickY + 1, bx + 78, quickY + 21, h ? 0xFF554400 : 0xFF332200);
            gfx.drawCenteredString(f, "\u00a7e" + quickItems[i][0], bx + 39, quickY + 4, 0);
        }

        // Content area
        int contentY = quickY + 28;
        int contentH = panelH - (contentY - panelY) - 24;
        int maxVisible = contentH / ROW_H;

        // Build visible list
        List<Object> visible = buildVisibleList();
        int totalRows = visible.size();
        int maxScroll = Math.max(0, totalRows - maxVisible);
        scrollOffset = Math.min(scrollOffset, maxScroll);

        for (int i = scrollOffset; i < Math.min(totalRows, scrollOffset + maxVisible); i++) {
            int rowY = contentY + (i - scrollOffset) * ROW_H;
            Object obj = visible.get(i);

            if (obj instanceof String catName) {
                gfx.fill(panelX + 4, rowY, panelX + PANEL_W - 4, rowY + CAT_H - 2, 0xFF222230);
                gfx.fill(panelX + 4, rowY, panelX + 8, rowY + CAT_H - 2, 0xFFFFAA00);
                boolean isCollapsed = collapsed.getOrDefault(catName, false);
                String arrow = isCollapsed ? "\u25b6" : "\u25bc";
                int count = categories.getOrDefault(catName, Collections.emptyList()).size();
                gfx.drawString(f, "\u00a76" + arrow + " " + catName + " \u00a77(" + count + ")", panelX + 14, rowY + 4, 0);
            } else if (obj instanceof ItemEntry e) {
                boolean hover = mx >= panelX + 8 && mx <= panelX + PANEL_W - 8 && my >= rowY && my < rowY + ROW_H;
                gfx.fill(panelX + 8, rowY, panelX + PANEL_W - 8, rowY + ROW_H - 2, hover ? 0xFF2A2A3A : 0xFF181822);
                if (hover) gfx.fill(panelX + 8, rowY, panelX + 11, rowY + ROW_H - 2, 0xFFFFAA00);

                // Name
                gfx.drawString(f, "\u00a7f" + e.displayName, panelX + 16, rowY + 3, 0xFFDDDDDD);
                // Registry name
                gfx.drawString(f, "\u00a78" + e.registryName, panelX + 140, rowY + 3, 0xFF666666);

                // Spawn button
                int btnX = panelX + PANEL_W - 70;
                boolean btnH = mx >= btnX && mx < btnX + 60 && my >= rowY + 1 && my < rowY + ROW_H - 3;
                gfx.fill(btnX, rowY + 1, btnX + 60, rowY + ROW_H - 3, btnH ? 0xFFAA6600 : 0xFF884400);
                gfx.drawCenteredString(f, "\u00a7e\u751f\u6210", btnX + 30, rowY + 3, 0);
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


    /** Execute a spawn action with MasterSwitch + rate limit checks */
    private String gatedSpawn(java.util.function.Supplier<String> action) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null) return "[X] Not in game";
        if (!MasterSwitch.isEnabled()) return "[X] Master Switch OFF (INSERT)";
        long now = System.currentTimeMillis();
        if (now - lastActionTime < RATE_LIMIT_MS) return "[!] Too fast, wait...";
        lastActionTime = now;
        return action.get();
    }

    @Override public boolean mouseClicked(double mx, double my, int btn) {
        // Quick buttons
        int quickY = panelY + 66;
        if (btn == 0 && my >= quickY + 1 && my < quickY + 21) {
            String[][] quickItems = {
                {"\u94bb\u77f3", "minecraft:diamond"},
                {"\u4e0b\u754c\u5408\u91d1", "minecraft:netherite_ingot"},
                {"\u4e0d\u6b7b\u56fe\u817e", "minecraft:totem_of_undying"},
                {"\u9644\u9b54\u82f9\u679c", "minecraft:enchanted_golden_apple"},
                {"\u9798\u7fc5", "minecraft:elytra"},
            };
            for (int i = 0; i < quickItems.length; i++) {
                int bx = panelX + 100 + i * 82;
                if (mx >= bx && mx < bx + 78) {
                    int count = getCount();
                    String finalId = quickItems[i][1]; String result = gatedSpawn(() -> ExploitSender.serverGiveItem(finalId, count));
                    statusMsg = result + " | " + quickItems[i][0] + " x" + count;
                    statusTimer = 100;
                    return true;
                }
            }
        }

        // Custom spawn button
        String customId = customIdBox.getValue().trim();
        if (btn == 0 && !customId.isEmpty()) {
            int bx = panelX + 390;
            if (mx >= bx && mx < bx + 120 && my >= panelY + 44 && my < panelY + 62) {
                int count = getCount();
                String finalCustom = customId; String result = gatedSpawn(() -> ExploitSender.serverGiveItem(finalCustom, count));
                statusMsg = result + " | " + customId + " x" + count;
                statusTimer = 100;
                return true;
            }
        }

        // Content clicks
        if (btn == 0 || btn == 1) {
            int contentY = quickY + 28;
            int contentH = panelH - (contentY - panelY) - 24;
            int maxVisible = contentH / ROW_H;
            List<Object> visible = buildVisibleList();

            for (int i = scrollOffset; i < Math.min(visible.size(), scrollOffset + maxVisible); i++) {
                int rowY = contentY + (i - scrollOffset) * ROW_H;
                Object obj = visible.get(i);

                if (mx >= panelX + 4 && mx <= panelX + PANEL_W - 4 && my >= rowY && my < rowY + ROW_H) {
                    if (obj instanceof String catName) {
                        collapsed.put(catName, !collapsed.getOrDefault(catName, false));
                        return true;
                    } else if (obj instanceof ItemEntry e) {
                        int count = getCount();
                        String finalReg = e.registryName; String result = gatedSpawn(() -> ExploitSender.serverGiveItem(finalReg, count));
                        statusMsg = result + " | " + e.displayName + " x" + count;
                        statusTimer = 100;
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mx, my, btn);
    }

    @Override public boolean keyPressed(int keyCode, int scanCode, int mods) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, mods);
    }

    @Override public boolean charTyped(char ch, int mods) {
        if (searchBox != null && searchBox.isFocused()) {
            searchBox.charTyped(ch, mods);
            return true;
        }
        if (countBox != null && countBox.isFocused()) {
            countBox.charTyped(ch, mods);
            return true;
        }
        if (customIdBox != null && customIdBox.isFocused()) {
            customIdBox.charTyped(ch, mods);
            return true;
        }
        return super.charTyped(ch, mods);
    }

    // ==================== HELPERS ====================

    private int getCount() {
        try { return Math.min(64, Math.max(1, Integer.parseInt(countBox.getValue().trim()))); }
        catch (Exception e) { return 64; }
    }

    private List<Object> buildVisibleList() {
        List<Object> list = new ArrayList<>();
        String query = searchBox != null ? searchBox.getValue().toLowerCase().trim() : "";
        for (var entry : categories.entrySet()) {
            String cat = entry.getKey();
            List<ItemEntry> items = entry.getValue();
            List<ItemEntry> filtered;
            if (query.isEmpty()) {
                filtered = items;
            } else {
                filtered = new ArrayList<>();
                for (ItemEntry e : items) {
                    if (e.displayName.toLowerCase().contains(query) || e.registryName.toLowerCase().contains(query)) {
                        filtered.add(e);
                    }
                }
            }
            if (!filtered.isEmpty()) {
                list.add(cat);
                if (!collapsed.getOrDefault(cat, false)) {
                    list.addAll(filtered);
                }
            }
        }
        return list;
    }
}