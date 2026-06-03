package com.ghost.util;

import com.ghost.AdminGhost;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * Single source of truth for Master Switch state.
 * All exploit actions MUST check MasterSwitch.isEnabled() before executing.
 */
public class MasterSwitch {
    private static boolean enabled = true;

    public static boolean isEnabled() { return enabled; }

    public static void setEnabled(boolean v) {
        enabled = v;
        AdminGhost.LOGGER.info("[MasterSwitch] {}", enabled ? "ENABLED" : "DISABLED");
    }

    public static void toggle() {
        setEnabled(!enabled);
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.displayClientMessage(Component.literal(
                enabled ? "\u00a7a[ON] Master Switch" : "\u00a7c[OFF] Master Switch (INSERT to re-enable)"
            ), false);
        }
    }
}
