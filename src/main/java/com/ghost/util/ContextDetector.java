package com.ghost.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * Context-aware container detector.
 * Detects what mod container the player currently has open,
 * so ExploitOverlay can auto-recommend relevant exploits.
 */
public class ContextDetector {
    public enum Context {
        SOPHISTICATED("Sophisticated Backpacks/Storage", "\\u00a7a"),
        CURIOS("Curios", "\\u00a76"),
        GOETY("Goety", "\\u00a75"),
        CATACLYSM("Cataclysm", "\\u00a74"),
        ENIGMATIC("Enigmatic Legacy", "\\u00a7b"),
        TCONSTRUCT("Tinkers Construct", "\\u00a73"),
        ENDINGLIB("Ending Library", "\\u00a7d"),
        TOMS_STORAGE("Tom's Storage", "\\u00a7e"),
        BAUBLELEY("Baubley Heart Canisters", "\\u00a7c"),
        ARTIFACTS("Artifacts", "\\u00a79"),
        INVENTORY("Player Inventory", "\\u00a7f"),
        NONE("None", "\\u00a77");

        public final String displayName;
        public final String color;
        Context(String name, String color) { this.displayName = name; this.color = color; }
    }

    private static final Minecraft mc = Minecraft.getInstance();

    /** Detect the current container context */
    public static Context getCurrentContext() {
        if (!isOk()) return Context.NONE;
        AbstractContainerMenu menu = mc.player.containerMenu;
        if (menu == null) return Context.NONE;
        String className = menu.getClass().getName();

        // Sophisticated
        if (isInstanceOf(menu, "net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase"))
            return Context.SOPHISTICATED;
        // Curios
        if (isInstanceOf(menu, "top.theillusivec4.curios.common.inventory.container.CuriosContainer"))
            return Context.CURIOS;
        // Goety
        if (className.contains("Polarice3") || className.contains("goety"))
            return Context.GOETY;
        // Cataclysm
        if (className.contains("cataclysm") || className.contains("L_Ender"))
            return Context.CATACLYSM;
        // Enigmatic
        if (className.contains("enigmatic"))
            return Context.ENIGMATIC;
        // Tinkers Construct
        if (className.contains("tconstruct") || className.contains("slimeknights"))
            return Context.TCONSTRUCT;
        // Ending Library
        if (className.contains("endinglib") || className.contains("mega"))
            return Context.ENDINGLIB;
        // Tom's Storage
        if (className.contains("storagemod") || className.contains("tom."))
            return Context.TOMS_STORAGE;
        // Baubley Heart Canisters
        if (className.contains("baubley") || className.contains("heartcanister"))
            return Context.BAUBLELEY;
        // Artifacts
        if (className.contains("artifacts"))
            return Context.ARTIFACTS;
        // Generic inventory
        if (className.contains("InventoryMenu") || className.contains("CraftingMenu"))
            return Context.INVENTORY;

        return Context.NONE;
    }

    /** Get a recommendation string for the current context */
    public static String getRecommendation(Context ctx) {
        switch (ctx) {
            case SOPHISTICATED: return "\\u00a7a\\u2714 SetGhostSlot \\u7269\\u54c1\\u6ce8\\u5165 | TransferFullSlot";
            case CURIOS: return "\\u00a76\\u2714 \\u6b7b\\u4ea1\\u590d\\u5236 | destroyAll | \\u5237\\u7269\\u54c1";
            case GOETY: return "\\u00a75\\u2714 VoidSlash | ScytheStrike | \\u80fd\\u529b\\u6ce8\\u5165";
            case CATACLYSM: return "\\u00a74\\u2714 ArmorKey | AltarInject | \\u795e\\u88c5\\u5237";
            case ENIGMATIC: return "\\u00a7b\\u2714 ElytraBoost | Magnet | \\u53e4\\u8461\\u888b\\u590d\\u5236";
            case TCONSTRUCT: return "\\u00a73\\u2714 tile=null \\u7ed5\\u8fc7 | \\u5bb9\\u5668\\u64cd\\u4f5c";
            case ENDINGLIB: return "\\u00a7d\\u2714 \\u8de8\\u73a9\\u5bb6\\u80cc\\u5305\\u8bbf\\u95ee";
            case TOMS_STORAGE: return "\\u00a7e\\u2714 \\u65e0\\u7ebf\\u7ec8\\u7aef | \\u8fdc\\u7a0b\\u63d0\\u53d6";
            case BAUBLELEY: return "\\u00a7c\\u2714 \\u6d3b\\u529b\\u4e4b\\u5203 stillValid | \\u590d\\u5236";
            case ARTIFACTS: return "\\u00a79\\u2714 \\u65e0\\u74f6\\u5b50\\u4e8c\\u6bb5\\u8df3 | \\u8df3\\u8dc3";
            default: return "\\u00a77\\u65e0\\u7279\\u5b9a\\u4e0a\\u4e0b\\u6587 - \\u8bf7\\u6253\\u5f00\\u4e00\\u4e2a\\u5bb9\\u5668";
        }
    }

    /** Get recommended exploit category name for auto-expand */
    public static String getRecommendedCategory(Context ctx) {
        switch (ctx) {
            case SOPHISTICATED: return "Sophisticated";
            case CURIOS: return "Curios";
            case GOETY: return "Goety";
            case CATACLYSM: return "Cataclysm";
            case ENIGMATIC: return "Enigmatic";
            default: return null;
        }
    }

    private static boolean isOk() {
        return mc.player != null && mc.player.containerMenu != null;
    }

    private static boolean isInstanceOf(Object obj, String className) {
        try {
            Class<?> cls = Class.forName(className);
            return cls.isInstance(obj);
        } catch (ClassNotFoundException e) { return false; }
    }

    /** Generate a brief analysis report */
    public static String generateReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== AdminGhost v2.0 Audit Report ===\\n");
        sb.append("Time: ").append(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date())).append("\\n\\n");

        // Context
        Context ctx = getCurrentContext();
        sb.append("Current Context: ").append(ctx.displayName).append("\\n");
        sb.append("Recommendation: ").append(getRecommendation(ctx)).append("\\n\\n");

        // Loaded mods
        try {
            var mods = net.minecraftforge.fml.ModList.get().getMods();
            sb.append("Loaded Mods (").append(mods.size()).append("):\\n");
            for (var mod : mods) {
                sb.append("  - ").append(mod.getModId()).append(" v").append(mod.getVersion()).append("\\n");
            }
        } catch (Exception e) {
            sb.append("Error listing mods: ").append(e.getMessage()).append("\\n");
        }

        return sb.toString();
    }
}
