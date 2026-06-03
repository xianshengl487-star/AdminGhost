package com.ghost.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.ghost.AdminGhost;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Preset system: save/load item injection presets from presets.json.
 * Each preset contains a list of (slot, itemId, count) entries.
 */
public class PresetManager {
    private static final String PRESET_FILE = "config/adminghost_presets.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static List<Preset> presets = new ArrayList<>();

    public static class Preset {
        public String name;
        public String description;
        public List<PresetItem> items;
        public long createdAt;

        public Preset() { this.items = new ArrayList<>(); this.createdAt = System.currentTimeMillis(); }
        public Preset(String name, String desc) {
            this.name = name;
            this.description = desc;
            this.items = new ArrayList<>();
            this.createdAt = System.currentTimeMillis();
        }
    }

    public static class PresetItem {
        public String item;
        public int count;
        public int slot; // -1 = auto-find empty slot

        public PresetItem() {}
        public PresetItem(String item, int count, int slot) {
            this.item = item; this.count = count; this.slot = slot;
        }
    }

    /** Load presets from disk */
    public static void load() {
        try {
            Path p = Path.of(PRESET_FILE);
            if (Files.exists(p)) {
                String json = Files.readString(p);
                List<Preset> loaded = GSON.fromJson(json, new TypeToken<List<Preset>>(){}.getType());
                if (loaded != null) {
                    presets = loaded;
                    AdminGhost.LOGGER.info("[PresetManager] Loaded {} presets", presets.size());
                }
            } else {
                // Create defaults
                createDefaults();
                save();
                AdminGhost.LOGGER.info("[PresetManager] Created default presets");
            }
        } catch (Exception e) {
            AdminGhost.LOGGER.error("[PresetManager] Failed to load presets: {}", e.getMessage());
            createDefaults();
        }
    }

    /** Save presets to disk */
    public static void save() {
        try {
            Path dir = Path.of("config");
            if (!Files.exists(dir)) Files.createDirectories(dir);
            Files.writeString(Path.of(PRESET_FILE), GSON.toJson(presets));
        } catch (Exception e) {
            AdminGhost.LOGGER.error("[PresetManager] Failed to save: {}", e.getMessage());
        }
    }

    public static List<Preset> getPresets() { return presets; }

    public static Preset getByName(String name) {
        for (Preset p : presets) {
            if (p.name.equalsIgnoreCase(name)) return p;
        }
        return null;
    }

    public static void addPreset(Preset preset) {
        // Replace if same name exists
        presets.removeIf(p -> p.name.equalsIgnoreCase(preset.name));
        presets.add(preset);
        save();
    }

    public static void removePreset(String name) {
        presets.removeIf(p -> p.name.equalsIgnoreCase(name));
        save();
    }

    private static void createDefaults() {
        // Diamond Kit
        Preset diamond = new Preset("\\u94bb\\u77f3\\u5957\\u88c5", "\\u94bb\\u77f3x64 + \\u5de5\\u5177");
        diamond.items.add(new PresetItem("minecraft:diamond", 64, -1));
        diamond.items.add(new PresetItem("minecraft:diamond_block", 64, -1));
        diamond.items.add(new PresetItem("minecraft:diamond_sword", 1, -1));
        diamond.items.add(new PresetItem("minecraft:diamond_pickaxe", 1, -1));
        presets.add(diamond);

        // Netherite Kit
        Preset netherite = new Preset("\\u4e0b\\u754c\\u5408\\u91d1\\u5957\\u88c5", "\\u4e0b\\u754c\\u5408\\u91d1\\u88c5\\u5907 + \\u5de5\\u5177");
        netherite.items.add(new PresetItem("minecraft:netherite_ingot", 64, -1));
        netherite.items.add(new PresetItem("minecraft:netherite_sword", 1, -1));
        netherite.items.add(new PresetItem("minecraft:netherite_pickaxe", 1, -1));
        netherite.items.add(new PresetItem("minecraft:netherite_helmet", 1, -1));
        netherite.items.add(new PresetItem("minecraft:netherite_chestplate", 1, -1));
        netherite.items.add(new PresetItem("minecraft:netherite_leggings", 1, -1));
        netherite.items.add(new PresetItem("minecraft:netherite_boots", 1, -1));
        presets.add(netherite);

        // God Kit
        Preset god = new Preset("\\u795e\\u88c5\\u5957\\u88c5", "\\u5168\\u5957\\u795e\\u88c5 + \\u91d1\\u82f9\\u679c + \\u56fe\\u817e");
        god.items.add(new PresetItem("minecraft:netherite_sword", 1, -1));
        god.items.add(new PresetItem("minecraft:netherite_pickaxe", 1, -1));
        god.items.add(new PresetItem("minecraft:netherite_helmet", 1, -1));
        god.items.add(new PresetItem("minecraft:netherite_chestplate", 1, -1));
        god.items.add(new PresetItem("minecraft:netherite_leggings", 1, -1));
        god.items.add(new PresetItem("minecraft:netherite_boots", 1, -1));
        god.items.add(new PresetItem("minecraft:enchanted_golden_apple", 64, -1));
        god.items.add(new PresetItem("minecraft:totem_of_undying", 64, -1));
        god.items.add(new PresetItem("minecraft:elytra", 1, -1));
        presets.add(god);

        // Endgame
        Preset endgame = new Preset("\\u7ec8\\u6781\\u5957\\u88c5", "\\u4e0b\\u754c\\u4e4b\\u661f + \\u4fe1\\u6807 + \\u7ffc + \\u9f99\\u86cb");
        endgame.items.add(new PresetItem("minecraft:nether_star", 64, -1));
        endgame.items.add(new PresetItem("minecraft:beacon", 64, -1));
        endgame.items.add(new PresetItem("minecraft:elytra", 1, -1));
        endgame.items.add(new PresetItem("minecraft:shulker_box", 64, -1));
        endgame.items.add(new PresetItem("minecraft:dragon_egg", 1, -1));
        presets.add(endgame);

        // Building
        Preset build = new Preset("\\u5efa\\u7b51\\u6750\\u6599\\u5305", "\\u9ed1\\u66dc\\u77f3 + \\u73bb\\u7483 + \\u6728\\u677f + \\u8f89\\u5149\\u77f3");
        build.items.add(new PresetItem("minecraft:obsidian", 64, -1));
        build.items.add(new PresetItem("minecraft:glass", 64, -1));
        build.items.add(new PresetItem("minecraft:oak_planks", 64, -1));
        build.items.add(new PresetItem("minecraft:glowstone", 64, -1));
        build.items.add(new PresetItem("minecraft:end_stone", 64, -1));
        presets.add(build);
    }
}
