package com.ghost;

import com.ghost.command.GhostCommand;
import com.ghost.gui.ExploitOverlay;
import com.ghost.gui.HackPanel;
import com.ghost.gui.HackHUD;
import com.ghost.hack.ModuleManager;
import com.ghost.hack.HackModule;
import com.ghost.scanner.PacketScanner;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

@Mod("adminghost")
public class AdminGhost {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final PacketScanner SCANNER = new PacketScanner();

    private static int guiMode = 0;
    private static long windowHandle = 0;
    private static GLFWKeyCallback originalCallback = null;

    public AdminGhost() {
        MinecraftForge.EVENT_BUS.addListener(this::onCommands);
        MinecraftForge.EVENT_BUS.addListener(this::onClientTick);
        MinecraftForge.EVENT_BUS.addListener(this::onKey);
        MinecraftForge.EVENT_BUS.addListener(this::onHudRender);
        
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onKeyRegister);
        ModuleManager.init();
        LOGGER.info("[Ghost] AdminGhost v11 loaded. RIGHT SHIFT = Exploit Panel, F7 = Hack Panel");
    }

    private void onCommands(RegisterCommandsEvent e) {
        GhostCommand.register(e.getDispatcher());
    }

    private void onKeyRegister(net.minecraftforge.client.event.RegisterKeyMappingsEvent e) {
    }

    private void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        
        if (windowHandle == 0 && mc.getWindow() != null) {
            windowHandle = mc.getWindow().getWindow();
            if (windowHandle != 0) {
                installGlfwCallback();
            }
        }
        
        ModuleManager.onTickAll();
    }

    private void installGlfwCallback() {
        originalCallback = GLFW.glfwSetKeyCallback(windowHandle, (window, key, scancode, action, mods) -> {
            if (originalCallback != null) {
                originalCallback.invoke(window, key, scancode, action, mods);
            }
            
            if (action != GLFW.GLFW_PRESS) return;
            
            if (key == GLFW.GLFW_KEY_RIGHT_SHIFT) {
                Minecraft mc = Minecraft.getInstance();
                mc.execute(() -> {
                    if (mc.screen instanceof ExploitOverlay || mc.screen instanceof HackPanel) {
                        mc.setScreen(null);
                        guiMode = 0;
                    } else {
                        mc.setScreen(new ExploitOverlay());
                        guiMode = 1;
                    }
                });
                return;
            }
            
            if (key == GLFW.GLFW_KEY_F7) {
                Minecraft mc = Minecraft.getInstance();
                mc.execute(() -> {
                    if (mc.screen instanceof HackPanel || mc.screen instanceof ExploitOverlay) {
                        mc.setScreen(null);
                        guiMode = 0;
                    } else {
                        mc.setScreen(new HackPanel());
                        guiMode = 2;
                    }
                });
                return;
            }
            
            if (key == GLFW.GLFW_KEY_F6) {
                Minecraft mc = Minecraft.getInstance();
                mc.execute(() -> HackHUD.toggle());
                return;
            }
            
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen == null) {
                ExploitOverlay.processGlobalKey(key);
                ModuleManager.processKey(key);
            }
        });
        LOGGER.info("[Ghost] GLFW callback installed - Right Shift / F7 / F6 (Forge onKey disabled)");
    }

    private void onHudRender(RenderGuiOverlayEvent.Post event) {
        HackHUD.render(event.getGuiGraphics());
    }

    // Forge key handler is intentionally a no-op.
    // All key processing is done in the GLFW callback (installGlfwCallback) to avoid double-fire.
    private void onKey(InputEvent.Key event) { }
}