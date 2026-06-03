package com.ghost.util;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.glfw.GLFW;

/**
 * Forge KeyMapping system - proper keybind registration.
 * Keys are consumed via consumeClick() to prevent double-fire.
 */
public class KeyBindings {
    public static final String CATEGORY = "key.categories.adminghost";

    // Core panel toggles
    public static final KeyMapping EXPLOIT_PANEL = new KeyMapping(
        "key.adminghost.exploit_panel",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_RIGHT_SHIFT,
        CATEGORY
    );

    public static final KeyMapping HACK_PANEL = new KeyMapping(
        "key.adminghost.hack_panel",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_F7,
        CATEGORY
    );

    public static final KeyMapping HUD_TOGGLE = new KeyMapping(
        "key.adminghost.hud_toggle",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_F6,
        CATEGORY
    );

    public static final KeyMapping MASTER_SWITCH = new KeyMapping(
        "key.adminghost.master_switch",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_INSERT,
        CATEGORY
    );

    // Quick exploit keys (work even when GUI is open)
    public static final KeyMapping QUICK_ENCHANT = new KeyMapping(
        "key.adminghost.quick_enchant",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_K,
        CATEGORY
    );

    public static final KeyMapping ONE_KEY_FILL = new KeyMapping(
        "key.adminghost.one_key_fill",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_G,
        CATEGORY
    );

    public static final KeyMapping[] ALL_KEYS = {
        EXPLOIT_PANEL, HACK_PANEL, HUD_TOGGLE, MASTER_SWITCH,
        QUICK_ENCHANT, ONE_KEY_FILL
    };

    /** Register all key mappings with Forge */
    public static void register(RegisterKeyMappingsEvent event) {
        for (KeyMapping key : ALL_KEYS) {
            event.register(key);
        }
    }
}
