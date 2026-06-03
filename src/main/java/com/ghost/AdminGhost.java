package com.ghost;

import com.ghost.command.GhostCommand;
import com.ghost.gui.ExploitOverlay;
import com.ghost.gui.HackPanel;
import com.ghost.gui.HackHUD;
import com.ghost.hack.ModuleManager;
import com.ghost.scanner.PacketScanner;
import net.minecraft.client.Minecraft;
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

    public AdminGhost() {
        MinecraftForge.EVENT_BUS.addListener(this::onCommands);
        MinecraftForge.EVENT_BUS.addListener(this::onClientTick);
        MinecraftForge.EVENT_BUS.addListener(this::onKey);
        MinecraftForge.EVENT_BUS.addListener(this::onHudRender);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onKeyRegister);
        ModuleManager.init();
        LOGGER.info("[Ghost] AdminGhost loaded. RSHIFT=Exploit F7=Hack F6=HUD INSERT=MasterSwitch");
    }

    private void onCommands(RegisterCommandsEvent e) {
        GhostCommand.register(e.getDispatcher());
    }

    private void onKeyRegister(net.minecraftforge.client.event.RegisterKeyMappingsEvent e) { }

    private void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        ModuleManager.onTickAll();
    }

    private void onHudRender(RenderGuiOverlayEvent.Post event) {
        HackHUD.render(event.getGuiGraphics());
    }

    private void onKey(InputEvent.Key event) {
        if (event.getAction() != GLFW.GLFW_PRESS) return;
        Minecraft mc = Minecraft.getInstance();
        int key = event.getKey();

        // GUI toggles - work even when other screens are open
        if (key == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            if (mc.screen instanceof ExploitOverlay || mc.screen instanceof HackPanel) {
                mc.setScreen(null);
            } else {
                mc.setScreen(new ExploitOverlay());
            }
            return;
        }
        if (key == GLFW.GLFW_KEY_F7) {
            if (mc.screen instanceof HackPanel || mc.screen instanceof ExploitOverlay) {
                mc.setScreen(null);
            } else {
                mc.setScreen(new HackPanel());
            }
            return;
        }
        if (key == GLFW.GLFW_KEY_F6) {
            HackHUD.toggle();
            return;
        }

        // Module/exploit keybinds - only when no screen is open
        if (mc.screen == null) {
            ExploitOverlay.processGlobalKey(key);
            ModuleManager.processKey(key);
        }
    }
}
