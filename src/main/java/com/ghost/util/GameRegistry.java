package com.ghost.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import java.util.*;

/**
 * Registry reader: enumerates enchantments and items from ForgeRegistries.
 * Used by selector GUIs.
 */
public class GameRegistry {

    // ============ Enchantment Data ============

    public static class EnchEntry {
        public final int id;
        public final String registryName;
        public final String displayName;
        public final int maxLevel;
        public final String category;

        public EnchEntry(int id, String registryName, String displayName, int maxLevel, String category) {
            this.id = id;
            this.registryName = registryName;
            this.displayName = displayName;
            this.maxLevel = maxLevel;
            this.category = category;
        }
    }

    private static List<EnchEntry> enchCache = null;

    /** Read all enchantments from Forge registry with IDs */
    public static List<EnchEntry> getAllEnchantments() {
        if (enchCache != null) return enchCache;
        enchCache = new ArrayList<>();
        try {
            ForgeRegistry<Enchantment> reg = (ForgeRegistry<Enchantment>) ForgeRegistries.ENCHANTMENTS;
            for (Enchantment ench : reg) {
                int id = reg.getID(ench);
                ResourceLocation rl = ForgeRegistries.ENCHANTMENTS.getKey(ench);
                String name = rl != null ? rl.toString() : "unknown";
                String display = ench.getFullname(0).getString();
                if (display.isEmpty()) display = name;
                int max = ench.getMaxLevel();

                // Categorize
                String cat = categorizeEnchantment(ench, name);
                enchCache.add(new EnchEntry(id, name, display, max, cat));
            }
            enchCache.sort(Comparator.comparing(e -> e.category + ":" + e.displayName));
        } catch (Exception e) {
            // Fallback: common enchantments
            enchCache.add(new EnchEntry(0, "minecraft:sharpness", "Sharpness", 5, "\u653b\u51fb"));
            enchCache.add(new EnchEntry(1, "minecraft:smite", "Smite", 5, "\u653b\u51fb"));
            enchCache.add(new EnchEntry(2, "minecraft:bane_of_arthropods", "Bane of Arthropods", 5, "\u653b\u51fb"));
            enchCache.add(new EnchEntry(3, "minecraft:knockback", "Knockback", 2, "\u653b\u51fb"));
            enchCache.add(new EnchEntry(4, "minecraft:fire_aspect", "Fire Aspect", 2, "\u653b\u51fb"));
            enchCache.add(new EnchEntry(5, "minecraft:looting", "Looting", 3, "\u653b\u51fb"));
            enchCache.add(new EnchEntry(6, "minecraft:sweeping", "Sweeping Edge", 3, "\u653b\u51fb"));
            enchCache.add(new EnchEntry(7, "minecraft:protection", "Protection", 4, "\u9632\u62a4"));
            enchCache.add(new EnchEntry(8, "minecraft:fire_protection", "Fire Protection", 4, "\u9632\u62a4"));
            enchCache.add(new EnchEntry(9, "minecraft:feather_falling", "Feather Falling", 4, "\u9632\u62a4"));
            enchCache.add(new EnchEntry(10, "minecraft:blast_protection", "Blast Protection", 4, "\u9632\u62a4"));
            enchCache.add(new EnchEntry(11, "minecraft:projectile_protection", "Projectile Protection", 4, "\u9632\u62a4"));
            enchCache.add(new EnchEntry(12, "minecraft:respiration", "Respiration", 3, "\u9632\u62a4"));
            enchCache.add(new EnchEntry(13, "minecraft:aqua_affinity", "Aqua Affinity", 1, "\u9632\u62a4"));
            enchCache.add(new EnchEntry(14, "minecraft:thorns", "Thorns", 3, "\u9632\u62a4"));
            enchCache.add(new EnchEntry(15, "minecraft:depth_strider", "Depth Strider", 3, "\u79fb\u52a8"));
            enchCache.add(new EnchEntry(16, "minecraft:frost_walker", "Frost Walker", 2, "\u79fb\u52a8"));
            enchCache.add(new EnchEntry(17, "minecraft:soul_speed", "Soul Speed", 3, "\u79fb\u52a8"));
            enchCache.add(new EnchEntry(18, "minecraft:swift_sneak", "Swift Sneak", 3, "\u79fb\u52a8"));
            enchCache.add(new EnchEntry(19, "minecraft:efficiency", "Efficiency", 5, "\u5de5\u5177"));
            enchCache.add(new EnchEntry(20, "minecraft:silk_touch", "Silk Touch", 1, "\u5de5\u5177"));
            enchCache.add(new EnchEntry(21, "minecraft:fortune", "Fortune", 3, "\u5de5\u5177"));
            enchCache.add(new EnchEntry(22, "minecraft:luck_of_the_sea", "Luck of the Sea", 3, "\u94a4\u9c7c"));
            enchCache.add(new EnchEntry(23, "minecraft:lure", "Lure", 3, "\u94a4\u9c7c"));
            enchCache.add(new EnchEntry(24, "minecraft:power", "Power", 5, "\u8fdc\u7a0b"));
            enchCache.add(new EnchEntry(25, "minecraft:punch", "Punch", 2, "\u8fdc\u7a0b"));
            enchCache.add(new EnchEntry(26, "minecraft:flame", "Flame", 1, "\u8fdc\u7a0b"));
            enchCache.add(new EnchEntry(27, "minecraft:infinity", "Infinity", 1, "\u8fdc\u7a0b"));
            enchCache.add(new EnchEntry(28, "minecraft:unbreaking", "Unbreaking", 3, "\u901a\u7528"));
            enchCache.add(new EnchEntry(29, "minecraft:mending", "Mending", 1, "\u901a\u7528"));
            enchCache.add(new EnchEntry(30, "minecraft:vanishing_curse", "Vanishing Curse", 1, "\u8bc5\u5492"));
            enchCache.add(new EnchEntry(31, "minecraft:binding_curse", "Binding Curse", 1, "\u8bc5\u5492"));
            enchCache.add(new EnchEntry(32, "minecraft:multishot", "Multishot", 1, "\u8fdc\u7a0b"));
            enchCache.add(new EnchEntry(33, "minecraft:quick_charge", "Quick Charge", 3, "\u8fdc\u7a0b"));
            enchCache.add(new EnchEntry(34, "minecraft:piercing", "Piercing", 4, "\u8fdc\u7a0b"));
            enchCache.add(new EnchEntry(35, "minecraft:loyalty", "Loyalty", 3, "\u8fdc\u7a0b"));
            enchCache.add(new EnchEntry(36, "minecraft:impaling", "Impaling", 5, "\u8fdc\u7a0b"));
            enchCache.add(new EnchEntry(37, "minecraft:riptide", "Riptide", 3, "\u8fdc\u7a0b"));
            enchCache.add(new EnchEntry(38, "minecraft:channeling", "Channeling", 1, "\u8fdc\u7a0b"));
        }
        return enchCache;
    }

    private static String categorizeEnchantment(Enchantment ench, String name) {
        if (name.contains("protection") || name.contains("thorns") || name.contains("respiration") ||
            name.contains("aqua_affinity") || name.contains("feather_falling")) return "\u9632\u62a4";
        if (name.contains("sharpness") || name.contains("smite") || name.contains("bane") ||
            name.contains("knockback") || name.contains("fire_aspect") || name.contains("looting") ||
            name.contains("sweeping") || name.contains("impaling")) return "\u653b\u51fb";
        if (name.contains("efficiency") || name.contains("silk_touch") || name.contains("fortune")) return "\u5de5\u5177";
        if (name.contains("power") || name.contains("punch") || name.contains("flame") ||
            name.contains("infinity") || name.contains("multishot") || name.contains("quick_charge") ||
            name.contains("piercing") || name.contains("loyalty") || name.contains("riptide") ||
            name.contains("channeling")) return "\u8fdc\u7a0b";
        if (name.contains("luck") || name.contains("lure")) return "\u94a4\u9c7c";
        if (name.contains("frost") || name.contains("depth") || name.contains("soul_speed") ||
            name.contains("swift_sneak")) return "\u79fb\u52a8";
        if (name.contains("unbreaking") || name.contains("mending")) return "\u901a\u7528";
        if (name.contains("curse")) return "\u8bc5\u5492";
        return "\u5176\u4ed6";
    }

    // ============ Item Data ============

    public static class ItemEntry {
        public final String registryName;
        public final String displayName;

        public ItemEntry(String registryName, String displayName) {
            this.registryName = registryName;
            this.displayName = displayName;
        }
    }

    /** Pre-defined valuable items for quick spawn */
    public static final String[][] VALUABLE_ITEMS = {
        {"minecraft:netherite_ingot", "\u4e0b\u754c\u5408\u91d1\u9524"},
        {"minecraft:netherite_block", "\u4e0b\u754c\u5408\u91d1\u5757"},
        {"minecraft:diamond_block", "\u94bb\u77f3\u5757"},
        {"minecraft:diamond", "\u94bb\u77f3"},
        {"minecraft:enchanted_golden_apple", "\u9644\u9b54\u91d1\u82f9\u679c"},
        {"minecraft:totem_of_undying", "\u4e0d\u6b7b\u56fe\u817e"},
        {"minecraft:elytra", "\u9798\u7fc5"},
        {"minecraft:nether_star", "\u4e0b\u754c\u4e4b\u661f"},
        {"minecraft:beacon", "\u4fe1\u6807"},
        {"minecraft:shulker_box", "\u6f5c\u5f12\u7bb1"},
        {"minecraft:ender_pearl", "\u672b\u5f71\u73ca\u74ca"},
        {"minecraft:experience_bottle", "\u7ecf\u9a8c\u74f6"},
        {"minecraft:saddle", "\u978d"},
        {"minecraft:name_tag", "\u540d\u724c"},
        {"minecraft:trident", "\u4e09\u53c9\u621f"},
        {"minecraft:golden_apple", "\u91d1\u82f9\u679c"},
        {"minecraft:end_crystal", "\u672b\u5f71\u6c34\u6676"},
        {"minecraft:firework_rocket", "\u706b\u7bad"},
        {"minecraft:echo_shard", "\u56de\u58f0\u7247"},
        {"minecraft:netherite_scrap", "\u4e0b\u754c\u5408\u91d1\u788e\u7247"},
        {"minecraft:ancient_debris", "\u8fdc\u53e4\u6b8b\u9ab8"},
        {"minecraft:emerald_block", "\u7eff\u5b9d\u77f3\u5757"},
        {"minecraft:gold_block", "\u91d1\u5757"},
        {"minecraft:iron_block", "\u94c1\u5757"},
    };

    /** Get common items grouped by category */
    public static Map<String, List<ItemEntry>> getCommonItems() {
        Map<String, List<ItemEntry>> map = new LinkedHashMap<>();

        List<ItemEntry> combat = new ArrayList<>();
        combat.add(new ItemEntry("minecraft:netherite_sword", "\u4e0b\u754c\u5408\u91d1\u5251"));
        combat.add(new ItemEntry("minecraft:netherite_axe", "\u4e0b\u754c\u5408\u91d1\u659f"));
        combat.add(new ItemEntry("minecraft:netherite_pickaxe", "\u4e0b\u754c\u5408\u91d1\u9550"));
        combat.add(new ItemEntry("minecraft:trident", "\u4e09\u53c9\u621f"));
        combat.add(new ItemEntry("minecraft:bow", "\u5f13"));
        combat.add(new ItemEntry("minecraft:crossbow", "\u5f29"));
        combat.add(new ItemEntry("minecraft:shield", "\u76fe"));
        combat.add(new ItemEntry("minecraft:totem_of_undying", "\u4e0d\u6b7b\u56fe\u817e"));
        map.put("\u6218\u6597\u88c5\u5907", combat);

        List<ItemEntry> armor = new ArrayList<>();
        armor.add(new ItemEntry("minecraft:netherite_helmet", "\u4e0b\u754c\u5408\u91d1\u5934\u76d4"));
        armor.add(new ItemEntry("minecraft:netherite_chestplate", "\u4e0b\u754c\u5408\u91d1\u80f8\u7532"));
        armor.add(new ItemEntry("minecraft:netherite_leggings", "\u4e0b\u754c\u5408\u91d1\u62a4\u817f"));
        armor.add(new ItemEntry("minecraft:netherite_boots", "\u4e0b\u754c\u5408\u91d1\u9774\u5b50"));
        armor.add(new ItemEntry("minecraft:elytra", "\u9798\u7fc5"));
        map.put("\u62a4\u7532", armor);

        List<ItemEntry> valuable = new ArrayList<>();
        for (String[] v : VALUABLE_ITEMS) {
            valuable.add(new ItemEntry(v[0], v[1]));
        }
        map.put("\u8d35\u91cd\u7269\u54c1", valuable);

        return map;
    }
}