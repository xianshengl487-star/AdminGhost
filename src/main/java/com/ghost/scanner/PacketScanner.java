package com.ghost.scanner;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Packet scanner: captures mod packets, analyzes structure, matches vulns.
 */
public class PacketScanner {

    private final Map<String, PacketProfile> profiles = new ConcurrentHashMap<>();
    private boolean scanning = false;

    public static class PacketProfile {
        public final String className;
        public final String modId;
        public final String direction;
        public final List<FieldInfo> fields = new ArrayList<>();
        public final List<String> matchedVulns = new ArrayList<>();
        public int count = 0;
        public long firstSeen;
        public long lastSeen;

        public PacketProfile(String className, String modId, String direction) {
            this.className = className;
            this.modId = modId;
            this.direction = direction;
            this.firstSeen = System.currentTimeMillis();
        }
    }

    public static class FieldInfo {
        public final String name;
        public final String type;
        public final boolean isNumber;
        public final boolean isString;
        public final boolean isCollection;
        public Object testValue;

        public FieldInfo(String name, String type, boolean isNumber, boolean isString, boolean isCollection) {
            this.name = name;
            this.type = type;
            this.isNumber = isNumber;
            this.isString = isString;
            this.isCollection = isCollection;
        }
    }

    // vuln patterns
    private static final String[] VULN_KEYWORDS = {
        "boss", "kill", "damage", "attack", "reward", "loot", "drop",
        "give", "item", "spawn", "summon", "teleport", "tp", "warp",
        "gui", "button", "click", "slot", "inventory", "craft", "recipe",
        "config", "sync", "capability", "data", "stat", "level", "xp",
        "skill", "ability", "cooldown", "effect", "buff", "potion",
        "nbt", "tag", "command", "op", "ban", "kick", "weather", "time",
        "place", "break", "build", "ride", "mount", "trade", "shop",
        "quest", "mission", "achievement", "advancement", "unlock"
    };

    private static final String[] CRITICAL_KEYWORDS = {
        "boss", "kill", "damage", "give", "item", "reward", "loot",
        "teleport", "tp", "op", "command", "config", "override",
        "creative", "gamemode", "spawn", "summon"
    };

    private static final String[] HIGH_KEYWORDS = {
        "gui", "button", "click", "slot", "inventory", "craft",
        "capability", "data", "stat", "level", "skill", "ability",
        "trade", "shop", "quest", "unlock", "enchant", "effect", "buff"
    };

    // ============================================================
    //  Core scanning
    // ============================================================

    public void onPacket(Object packet, boolean outgoing) {
        if (!scanning) return;
        String className = packet.getClass().getName();
        if (isVanilla(className)) return;

        String dir = outgoing ? "OUT" : "IN";

        profiles.computeIfAbsent(className, k -> {
            String modId = extractModId(className);
            PacketProfile p = new PacketProfile(className, modId, dir);
            analyzeFields(packet, p);
            matchVulns(p);
            return p;
        });

        PacketProfile p = profiles.get(className);
        p.count++;
        p.lastSeen = System.currentTimeMillis();
    }

    private boolean isVanilla(String cn) {
        return cn.startsWith("net.minecraft.network.protocol.") ||
               cn.startsWith("net.minecraft.network.BundlerInfo") ||
               cn.startsWith("net.minecraft.network.codec.") ||
               cn.startsWith("net.minecraft.network.chat.") ||
               cn.startsWith("java.") || cn.startsWith("io.netty.");
    }

    private void analyzeFields(Object packet, PacketProfile profile) {
        Class<?> clazz = packet.getClass();
        Set<String> seen = new HashSet<>();
        while (clazz != null && clazz != Object.class) {
            for (Field f : clazz.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers())) continue;
                if (Modifier.isTransient(f.getModifiers())) continue;
                if (!seen.add(f.getName())) continue;
                f.setAccessible(true);

                Class<?> type = f.getType();
                boolean isNum = type.isPrimitive() && type != boolean.class ||
                    Number.class.isAssignableFrom(type);
                boolean isStr = type == String.class;
                boolean isColl = Collection.class.isAssignableFrom(type) ||
                    Map.class.isAssignableFrom(type);

                FieldInfo fi = new FieldInfo(f.getName(), type.getSimpleName(), isNum, isStr, isColl);

                // generate test value
                if (isNum) {
                    if (type == int.class || type == Integer.class) fi.testValue = Integer.MAX_VALUE;
                    else if (type == float.class || type == Float.class) fi.testValue = Float.MAX_VALUE;
                    else if (type == double.class || type == Double.class) fi.testValue = Double.MAX_VALUE;
                    else if (type == long.class || type == Long.class) fi.testValue = Long.MAX_VALUE;
                    else if (type == short.class || type == Short.class) fi.testValue = Short.MAX_VALUE;
                    else if (type == byte.class || type == Byte.class) fi.testValue = Byte.MAX_VALUE;
                } else if (isStr) {
                    fi.testValue = "A".repeat(10000);
                }

                profile.fields.add(fi);
            }
            clazz = clazz.getSuperclass();
        }
    }

    private void matchVulns(PacketProfile profile) {
        String combined = (profile.className + " " + profile.modId).toLowerCase();
        String[] fieldNames = profile.fields.stream()
            .map(f -> f.name.toLowerCase()).toArray(String[]::new);

        // check class name + field names against keywords
        Set<String> matches = new LinkedHashSet<>();

        for (String kw : VULN_KEYWORDS) {
            if (combined.contains(kw)) matches.add(kw);
            for (String fn : fieldNames) {
                if (fn.contains(kw)) { matches.add(kw); break; }
            }
        }

        // build vuln descriptions
        for (String m : matches) {
            boolean critical = Arrays.asList(CRITICAL_KEYWORDS).contains(m);
            boolean high = Arrays.asList(HIGH_KEYWORDS).contains(m);
            String severity = critical ? "CRITICAL" : high ? "HIGH" : "MEDIUM";

            String desc = switch (m) {
                case "boss", "kill" -> severity + ": Boss/mob kill bypass - possible instant kill";
                case "damage", "attack" -> severity + ": Damage value tampering";
                case "give", "item", "reward", "loot", "drop" -> severity + ": Item/reward injection";
                case "teleport", "tp", "warp" -> severity + ": Teleport coordinate spoofing";
                case "gui", "button", "click" -> severity + ": GUI interaction bypass";
                case "slot", "inventory" -> severity + ": Inventory slot overflow/desync";
                case "craft", "recipe" -> severity + ": Craft result spoofing";
                case "config", "sync" -> severity + ": Config data override";
                case "capability", "data", "stat" -> severity + ": Capability data injection";
                case "level", "xp" -> severity + ": Level/XP value overflow";
                case "skill", "ability" -> severity + ": Skill cooldown bypass";
                case "effect", "buff", "potion" -> severity + ": Effect injection";
                case "nbt", "tag" -> severity + ": NBT injection (deep nest/overflow)";
                case "command", "op" -> severity + ": Command injection";
                case "ban", "kick" -> severity + ": Admin command spoofing";
                case "trade", "shop" -> severity + ": Trade/shop bypass";
                case "quest", "mission", "achievement", "unlock" -> severity + ": Quest/achievement bypass";
                case "enchant" -> severity + ": Enchantment injection";
                case "spawn", "summon" -> severity + ": Entity spawn abuse";
                case "ride", "mount" -> severity + ": Mount bypass";
                case "place", "break", "build" -> severity + ": World modification bypass";
                case "weather", "time" -> severity + ": World state manipulation";
                default -> severity + ": Potential exploit via " + m;
            };
            profile.matchedVulns.add(desc);
        }

        // structural vuln detection
        boolean hasLargeNumbers = profile.fields.stream()
            .anyMatch(f -> f.isNumber && f.testValue != null);
        boolean hasStrings = profile.fields.stream()
            .anyMatch(f -> f.isString);
        boolean hasCollections = profile.fields.stream()
            .anyMatch(f -> f.isCollection);

        if (hasLargeNumbers) {
            profile.matchedVulns.add("INFO: Has numeric fields - test with MAX_VALUE overflow");
        }
        if (hasStrings) {
            profile.matchedVulns.add("INFO: Has string fields - test with oversized input");
        }
        if (hasCollections) {
            profile.matchedVulns.add("INFO: Has collection fields - test with large collections");
        }
    }

    private String extractModId(String cn) {
        String[] parts = cn.split("\\.");
        if (parts.length >= 3) {
            String candidate = parts[2];
            if (!Set.of("minecraft", "mojang", "common", "util", "api", "core", "network", "protocol").contains(candidate)) {
                return candidate;
            }
        }
        if (parts.length >= 2) return parts[1];
        return "unknown";
    }

    // ============================================================
    //  Report generation
    // ============================================================

    public String generateReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== LOOFOR SCAN REPORT ===\n");
        sb.append("Total mod packets: ").append(profiles.size()).append("\n\n");

        // group by mod
        Map<String, List<PacketProfile>> byMod = new TreeMap<>();
        for (PacketProfile p : profiles.values()) {
            byMod.computeIfAbsent(p.modId, k -> new ArrayList<>()).add(p);
        }

        int totalVulns = 0;
        int criticalCount = 0;
        int highCount = 0;

        for (var entry : byMod.entrySet()) {
            String mod = entry.getKey();
            List<PacketProfile> packets = entry.getValue();
            int modVulns = packets.stream().mapToInt(p -> p.matchedVulns.size()).sum();
            totalVulns += modVulns;
            criticalCount += (int) packets.stream().flatMap(p -> p.matchedVulns.stream())
                .filter(v -> v.startsWith("CRITICAL")).count();
            highCount += (int) packets.stream().flatMap(p -> p.matchedVulns.stream())
                .filter(v -> v.startsWith("HIGH")).count();

            sb.append("MOD: ").append(mod).append("\n");
            sb.append("  Packets: ").append(packets.size());
            sb.append(" | Vulns: ").append(modVulns).append("\n");

            for (PacketProfile p : packets) {
                String shortName = p.className.substring(p.className.lastIndexOf('.') + 1);
                sb.append("  [").append(p.direction).append("] ").append(shortName);
                sb.append(" (x").append(p.count).append(")\n");
                sb.append("    Fields: ");
                for (FieldInfo f : p.fields) {
                    sb.append(f.name).append("(").append(f.type).append(") ");
                }
                sb.append("\n");
                for (String v : p.matchedVulns) {
                    sb.append("    >> ").append(v).append("\n");
                }
            }
            sb.append("\n");
        }

        sb.append("=== SUMMARY ===\n");
        sb.append("Total vulns found: ").append(totalVulns).append("\n");
        sb.append("  CRITICAL: ").append(criticalCount).append("\n");
        sb.append("  HIGH: ").append(highCount).append("\n");
        sb.append("  MEDIUM: ").append(totalVulns - criticalCount - highCount).append("\n");

        return sb.toString();
    }

    // ============================================================
    //  Control
    // ============================================================

    public void start() { scanning = true; profiles.clear(); }
    public void stop() { scanning = false; }
    public boolean isScanning() { return scanning; }
    public int getCount() { return profiles.size(); }
    public Collection<PacketProfile> getProfiles() { return profiles.values(); }
    public Map<String, List<PacketProfile>> getByMod() {
        Map<String, List<PacketProfile>> result = new TreeMap<>();
        for (PacketProfile p : profiles.values()) {
            result.computeIfAbsent(p.modId, k -> new ArrayList<>()).add(p);
        }
        return result;
    }
}