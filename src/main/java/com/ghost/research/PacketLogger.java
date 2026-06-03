package com.ghost.research;

import com.ghost.AdminGhost;
import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Simple packet logger for research.
 * Logs outgoing packets to a file for analysis.
 */
public class PacketLogger {
    private static final String LOG_DIR = "config/adminghost_logs";
    private static boolean enabled = false;
    private static int packetCount = 0;
    private static final List<String> recentPackets = new ArrayList<>();
    private static final int MAX_RECENT = 50;

    public static void toggle() {
        enabled = !enabled;
        if (enabled) {
            packetCount = 0;
            AdminGhost.LOGGER.info("[PacketLogger] ENABLED");
        } else {
            AdminGhost.LOGGER.info("[PacketLogger] DISABLED ({} packets logged)", packetCount);
        }
    }

    public static boolean isEnabled() { return enabled; }

    /** Log an outgoing packet */
    public static void logOutgoing(String channel, int discriminator, int bytes) {
        if (!enabled) return;
        packetCount++;
        String timestamp = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
        String entry = String.format("[%s] OUT ch=%s disc=%d bytes=%d", timestamp, channel, discriminator, bytes);

        synchronized (recentPackets) {
            recentPackets.add(entry);
            if (recentPackets.size() > MAX_RECENT) recentPackets.remove(0);
        }

        // Also write to file
        try {
            Path dir = Path.of(LOG_DIR);
            if (!Files.exists(dir)) Files.createDirectories(dir);
            String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            Files.writeString(
                Path.of(LOG_DIR, "packets_" + dateStr + ".log"),
                entry + System.lineSeparator(),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND
            );
        } catch (Exception ignored) {}
    }

    /** Log an exploit action */
    public static void logExploit(String action, String result) {
        if (!enabled) return;
        String timestamp = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
        String entry = String.format("[%s] EXPLOIT: %s -> %s", timestamp, action, result);

        synchronized (recentPackets) {
            recentPackets.add(entry);
            if (recentPackets.size() > MAX_RECENT) recentPackets.remove(0);
        }

        try {
            Path dir = Path.of(LOG_DIR);
            if (!Files.exists(dir)) Files.createDirectories(dir);
            String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            Files.writeString(
                Path.of(LOG_DIR, "exploits_" + dateStr + ".log"),
                entry + System.lineSeparator(),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND
            );
        } catch (Exception ignored) {}
    }

    /** Get recent packet entries for display */
    public static List<String> getRecent() {
        synchronized (recentPackets) {
            return new ArrayList<>(recentPackets);
        }
    }

    public static int getPacketCount() { return packetCount; }
}
