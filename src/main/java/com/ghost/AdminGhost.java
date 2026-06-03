package com.ghost;

import com.ghost.command.GhostCommand;
import com.ghost.gui.ExploitOverlay;
import com.ghost.gui.HackPanel;
import com.ghost.gui.HackHUD;
import com.ghost.hack.ModuleManager;
import com.ghost.scanner.PacketScanner;
import com.ghost.util.PresetManager;
import com.ghost.util.ContextDetector;
import com.ghost.util.KeyBindings;
import com.ghost.util.MasterSwitch;
import com.ghost.exploit.OneKeyExploit;
import com.ghost.exploit.ExploitSender;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

@Mod("adminghost")
public class AdminGhost {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final PacketScanner SCANNER = new PacketScanner();
    private static long lastToggleTime = 0;

    public AdminGhost() {
        MinecraftForge.EVENT_BUS.addListener(this::onCommands);
        MinecraftForge.EVENT_BUS.addListener(this::onClientTick);
        MinecraftForge.EVENT_BUS.addListener(this::onKey);
        MinecraftForge.EVENT_BUS.addListener(this::onHudRender);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onKeyRegister);
        ModuleManager.init();
        PresetManager.load();
        LOGGER.info("[Ghost] AdminGhost v2.0 loaded. RSHIFT=Exploit F7=Hack F6=HUD INSERT=Master K=Enchant G=OneKey");
    }

    private void onCommands(RegisterCommandsEvent e) {
        GhostCommand.register(e.getDispatcher());
    }

    private void onKeyRegister(net.minecraftforge.client.event.RegisterKeyMappingsEvent e) {
        KeyBindings.register(e);
    }

    private void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // Quick exploit keys - work even when GUI is open
        if (KeyBindings.QUICK_ENCHANT.consumeClick()) {
            String result = ExploitSender.infuserEnchant("minecraft:sharpness", true);
            mc.player.displayClientMessage(Component.literal("\u00a7b[AdminGhost] " + result), false);
        }

        if (KeyBindings.ONE_KEY_FILL.consumeClick()) {
            OneKeyExploit.performOneKeyFill();
        }

        if (KeyBindings.MASTER_SWITCH.consumeClick()) {
            MasterSwitch.toggle();
        }

        ModuleManager.onTickAll();
    }

    private void onHudRender(RenderGuiOverlayEvent.Post event) {
        HackHUD.render(event.getGuiGraphics());
    }

    // ===== Smart GUI switching - prevents screen conflicts =====
    private void onKey(InputEvent.Key event) {
        if (event.getAction() != GLFW.GLFW_PRESS) return;
        Minecraft mc = Minecraft.getInstance();
        int key = event.getKey();

        if (KeyBindings.EXPLOIT_PANEL.matches(key, event.getScanCode())) {
            toggleExploitGui();
            return;
        }
        if (KeyBindings.HACK_PANEL.matches(key, event.getScanCode())) {
            toggleHackGui();
            return;
        }
        if (KeyBindings.HUD_TOGGLE.matches(key, event.getScanCode())) {
            HackHUD.toggle();
            return;
        }

        // Module keybinds only when no screen is open
        if (mc.screen == null) {
            ExploitOverlay.processGlobalKey(key);
            ModuleManager.processKey(key);
        }
    }

    /** Smart switching: close current screen first, then open on next tick */
    public static void toggleExploitGui() {
        Minecraft mc = Minecraft.getInstance();
        long now = System.currentTimeMillis();
        if (now - lastToggleTime < 200) return; // 200ms cooldown
        lastToggleTime = now;
        if (mc.screen instanceof ExploitOverlay) {
            mc.setScreen(null);
        } else {
            mc.setScreen(null);
            mc.execute(() -> mc.setScreen(new ExploitOverlay()));
        }
    }

    public static void toggleHackGui() {
        Minecraft mc = Minecraft.getInstance();
        long now = System.currentTimeMillis();
        if (now - lastToggleTime < 200) return; // 200ms cooldown
        lastToggleTime = now;
        if (mc.screen instanceof HackPanel) {
            mc.setScreen(null);
        } else {
            mc.setScreen(null);
            mc.execute(() -> mc.setScreen(new HackPanel()));
        }
    }
}