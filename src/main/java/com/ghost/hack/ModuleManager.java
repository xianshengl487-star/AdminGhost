package com.ghost.hack;

import com.ghost.hack.modules.*;
import java.util.*;

public class ModuleManager {
    private static final List<HackModule> modules = new ArrayList<>();
    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;
        initialized = true;

        // Movement
        modules.add(new FlyMod());
        modules.add(new SpeedMod());
        modules.add(new NoFallMod());
        modules.add(new StepMod());
        modules.add(new NoSlowDownMod());
        modules.add(new SprintMod());
        modules.add(new NoClipMod());

        // Combat
        modules.add(new KillAuraMod());
        modules.add(new CriticalsMod());
        modules.add(new ReachMod());
        modules.add(new AutoTotemMod());

        // Render
        modules.add(new ESPMod());
        modules.add(new TracersMod());
        modules.add(new FullBrightMod());
        modules.add(new ChestESPMod());
        modules.add(new NametagsMod());

        // Player
        modules.add(new FastBreakMod());
        modules.add(new ScaffoldMod());
        modules.add(new AutoFishMod());
        modules.add(new AntiHungerMod());
        modules.add(new AutoEatMod());
    }

    public static List<HackModule> getModules() { return modules; }

    public static HackModule getByName(String name) {
        for (HackModule m : modules) {
            if (m.getName().equalsIgnoreCase(name)) return m;
        }
        return null;
    }

    public static List<HackModule> getByCategory(String cat) {
        List<HackModule> list = new ArrayList<>();
        for (HackModule m : modules) {
            if (m.getCategory().equals(cat)) list.add(m);
        }
        return list;
    }

    /** Called every client tick - only ticks enabled modules */
    public static void onTickAll() {
        for (HackModule m : modules) {
            if (m.isEnabled()) m.onTick();
        }
    }

    /** Called from render event - only renders enabled modules */
    public static void onRenderAll() {
        for (HackModule m : modules) {
            if (m.isEnabled()) m.onRender();
        }
    }

    public static void processKey(int keyCode) {
        for (HackModule m : modules) {
            if (m.getKeyBind() == keyCode) m.toggle();
        }
    }
}