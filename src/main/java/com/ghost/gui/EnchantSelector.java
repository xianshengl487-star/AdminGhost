package com.ghost.gui;

import com.ghost.exploit.ExploitSender;
import com.ghost.util.GameRegistry;
import com.ghost.util.GameRegistry.EnchEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Enchantment Selector GUI.
 * Browse all enchantments, select level, apply via Apotheosis exploit.
 * Opens on top of ExploitOverlay via button.
 */
public class EnchantSelector extends Screen {
    private List<EnchEntry> allEnchantments;
    private List<EnchEntry> filtered;
    private Map<String, List<EnchEntry>> byCategory;
    private Map<String, Boolean> collapsed = new HashMap<>();
    private int selectedLevel = 5;
    private int scrollOffset = 0;
    private EditBox searchBox;
    private String statusMsg = "";
    private int statusTimer = 0;

    // Layout
    private static final int PANEL_W = 520;
    private static final int ROW_H = 20;
    private static final int CAT_H = 24;
    private int panelX, panelY, panelH;

    public EnchantSelector() { super(Component.literal("EnchantSelector")); }

    @Override protected void init() {
        allEnchantments = GameRegistry.getAllEnchantments();
        filtered = new ArrayList<>(allEnchantments);
        rebuildCategories();

        panelX = (this.width - PANEL_W) / 2;
        panelY = 8;
        panelH = this.height - 16;

        searchBox = new EditBox(this.font, panelX + 80, panelY + 26, PANEL_W - 180, 16, Component.literal("search"));
        searchBox.setMaxLength(64);
        searchBox.setBordered(true);
        searchBox.setVisible(true);
        searchBox.setHint(Component.literal("\u641c\u7d22\u9644\u9b54.."));
        this.addRenderableWidget(searchBox);
    }

    @Override public boolean isPauseScreen() { return false; }

    @Override public boolean mouseScrolled(double mx, double my, double delta) {
        scrollOffset -= (int)(delta * 3);
        scrollOffset = Math.max(0, scrollOffset);
        return true;
    }

    @Override public void tick() {
        if (searchBox != null) searchBox.tick();
    }

    // ==================== RENDER ====================

    @Override public void render(GuiGraphics gfx, int mx, int my, float pt) {
        gfx.fill(0, 0, this.width, this.height, 0xAA000000);
        gfx.fill(panelX, panelY, panelX + PANEL_W, panelY + panelH, 0xF0101018);
        gfx.fill(panelX, panelY, panelX + PANEL_W, panelY + 2, 0xFFAA55FF);
        gfx.fill(panelX, panelY + panelH - 2, panelX + PANEL_W, panelY + panelH, 0xFFAA55FF);
        gfx.fill(panelX, panelY, panelX + 3, panelY + panelH, 0xFFAA55FF);

        Font f = this.font;
        // Title
        gfx.drawCenteredString(f, "\u00a7d\u00a7l\u9644\u9b54\u9009\u62e9\u5668 \u00a7f| \u00a7e\u5171 " + allEnchantments.size() + " \u4e2a\u9644\u9b54", panelX + PANEL_W / 2, panelY + 6, 0xFFFFFF);

        // Search label
        gfx.drawString(f, "\u00a7e\u641c\u7d22:", panelX + 10, panelY + 28, 0);

        // Level selector
        int levelY = panelY + 46;
        gfx.fill(panelX + 8, levelY, panelX + PANEL_W - 8, levelY + 22, 0xFF222230);
        gfx.drawString(f, "\u00a7e\u7b49\u7ea7\u9009\u62e9:", panelX + 14, levelY + 4, 0);
        // Level buttons
        for (int i = 1; i <= 10; i++) {
            int bx = panelX + 100 + (i - 1) * 38;
            boolean sel = (i == selectedLevel);
            boolean hover = mx >= bx && mx < bx + 34 && my >= levelY + 2 && my < levelY + 20;
            gfx.fill(bx, levelY + 2, bx + 34, levelY + 20, sel ? 0xFFAA55FF : hover ? 0xFF444455 : 0xFF333344);
            gfx.drawCenteredString(f, (sel ? "\u00a7f\u00a7l" : "\u00a77") + "Lv" + i, bx + 17, levelY + 5, sel ? 0xFFFFFF : 0xAAAAAA);
        }
        // MAX button
        int maxBx = panelX + 100 + 10 * 38;
        boolean maxSel = (selectedLevel == -1);
        boolean maxHover = mx >= maxBx && mx < maxBx + 50 && my >= levelY + 2 && my < levelY + 20;
        gfx.fill(maxBx, levelY + 2, maxBx + 50, levelY + 20, maxSel ? 0xFFFF5555 : maxHover ? 0xFF554444 : 0xFF443333);
        gfx.drawCenteredString(f, (maxSel ? "\u00a7f\u00a7l" : "\u00a7c") + "MAX", maxBx + 25, levelY + 5, maxSel ? 0xFFFFFF : 0xFFAAAA);

        // Content area
        int contentY = levelY + 28;
        int contentH = panelH - (contentY - panelY) - 24;
        int maxVisible = contentH / ROW_H;

        // Build flat visible list
        List<Object> visible = buildVisibleList(filtered);
        int totalRows = visible.size();
        int maxScroll = Math.max(0, totalRows - maxVisible);
        scrollOffset = Math.min(scrollOffset, maxScroll);

        int startIdx = scrollOffset;
        int endIdx = Math.min(totalRows, startIdx + maxVisible);

        for (int i = startIdx; i < endIdx; i++) {
            int rowY = contentY + (i - startIdx) * ROW_H;
            Object obj = visible.get(i);

            if (obj instanceof String catName) {
                // Category header
                gfx.fill(panelX + 4, rowY, panelX + PANEL_W - 4, rowY + CAT_H - 2, 0xFF222230);
                gfx.fill(panelX + 4, rowY, panelX + 8, rowY + CAT_H - 2, 0xFFAA55FF);
                boolean isCollapsed = collapsed.getOrDefault(catName, false);
                String arrow = isCollapsed ? "\u25b6" : "\u25bc";
                int count = byCategory.getOrDefault(catName, Collections.emptyList()).size();
                gfx.drawString(f, "\u00a7d" + arrow + " " + catName + " \u00a77(" + count + ")", panelX + 14, rowY + 4, 0);
            } else if (obj instanceof EnchEntry e) {
                boolean hover = mx >= panelX + 8 && mx <= panelX + PANEL_W - 8 && my >= rowY && my < rowY + ROW_H;
                gfx.fill(panelX + 8, rowY, panelX + PANEL_W - 8, rowY + ROW_H - 2, hover ? 0xFF2A2A3A : 0xFF181822);
                if (hover) gfx.fill(panelX + 8, rowY, panelX + 11, rowY + ROW_H - 2, 0xFFAA55FF);

                // ID
                gfx.drawString(f, "\u00a78" + e.id, panelX + 14, rowY + 3, 0xFF666666);
                // Name
                gfx.drawString(f, "\u00a7f" + e.displayName, panelX + 50, rowY + 3, 0xFFDDDDDD);
                // Registry name
                gfx.drawString(f, "\u00a78" + e.registryName, panelX + 200, rowY + 3, 0xFF666666);
                // Max level
                gfx.drawString(f, "\u00a77\u6700\u9ad8Lv" + e.maxLevel, panelX + PANEL_W - 130, rowY + 3, 0xFF888888);

                // Apply button
                int btnX = panelX + PANEL_W - 70;
                boolean btnHover = mx >= btnX && mx < btnX + 60 && my >= rowY + 1 && my < rowY + ROW_H - 3;
                gfx.fill(btnX, rowY + 1, btnX + 60, rowY + ROW_H - 3, btnHover ? 0xFF44AA44 : 0xFF336633);
                gfx.drawCenteredString(f, "\u00a7a\u5e94\u7528", btnX + 30, rowY + 3, 0);
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
        // Level selector clicks
        int levelY = panelY + 46;
        if (my >= levelY + 2 && my < levelY + 20) {
            for (int i = 1; i <= 10; i++) {
                int bx = panelX + 100 + (i - 1) * 38;
                if (mx >= bx && mx < bx + 34) {
                    selectedLevel = i;
                    return true;
                }
            }
            int maxBx = panelX + 100 + 10 * 38;
            if (mx >= maxBx && mx < maxBx + 50) {
                selectedLevel = -1;
                return true;
            }
        }

        // Content clicks
        if (btn == 0) {
            int contentY = levelY + 28;
            int contentH = panelH - (contentY - panelY) - 24;
            int maxVisible = contentH / ROW_H;
            List<Object> visible = buildVisibleList(filtered);

            for (int i = scrollOffset; i < Math.min(visible.size(), scrollOffset + maxVisible); i++) {
                int rowY = contentY + (i - scrollOffset) * ROW_H;
                Object obj = visible.get(i);

                if (mx >= panelX + 4 && mx <= panelX + PANEL_W - 4 && my >= rowY && my < rowY + ROW_H) {
                    if (obj instanceof String catName) {
                        collapsed.put(catName, !collapsed.getOrDefault(catName, false));
                        return true;
                    } else if (obj instanceof EnchEntry e) {
                        // Check if clicking apply button
                        int btnX = panelX + PANEL_W - 70;
                        if (mx >= btnX) {
                            applyEnchantment(e);
                            return true;
                        }
                        // Click on row = also apply
                        applyEnchantment(e);
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mx, my, btn);
    }

    @Override public boolean charTyped(char ch, int mods) {
        if (searchBox != null && searchBox.isFocused()) {
            searchBox.charTyped(ch, mods);
            filterEnchantments();
            return true;
        }
        return super.charTyped(ch, mods);
    }

    @Override public boolean keyPressed(int keyCode, int scanCode, int mods) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }
        if (searchBox != null && searchBox.isFocused()) {
            if (searchBox.keyPressed(keyCode, scanCode, mods)) {
                filterEnchantments();
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, mods);
    }

    // ==================== ACTIONS ====================

    private void applyEnchantment(EnchEntry e) {
        boolean maxLevel = (selectedLevel == -1);
        String result = ExploitSender.apotheosisEnchant(e.id, maxLevel);
        String lvl = maxLevel ? "MAX" : (selectedLevel + "");
        statusMsg = result + " | " + e.displayName + " Lv" + lvl;
        statusTimer = 100;
    }

    // ==================== HELPERS ====================

    private void filterEnchantments() {
        String query = searchBox.getValue().toLowerCase().trim();
        if (query.isEmpty()) {
            filtered = new ArrayList<>(allEnchantments);
        } else {
            filtered = allEnchantments.stream()
                .filter(e -> e.displayName.toLowerCase().contains(query) ||
                             e.registryName.toLowerCase().contains(query) ||
                             e.category.toLowerCase().contains(query) ||
                             String.valueOf(e.id).equals(query))
                .collect(Collectors.toList());
        }
        rebuildCategories();
    }

    private void rebuildCategories() {
        byCategory = new LinkedHashMap<>();
        for (EnchEntry e : filtered) {
            byCategory.computeIfAbsent(e.category, k -> new ArrayList<>()).add(e);
        }
    }

    private List<Object> buildVisibleList(List<EnchEntry> entries) {
        List<Object> list = new ArrayList<>();
        for (var entry : byCategory.entrySet()) {
            String cat = entry.getKey();
            list.add(cat);
            if (!collapsed.getOrDefault(cat, false)) {
                list.addAll(entry.getValue());
            }
        }
        return list;
    }
}