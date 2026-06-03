package com.ghost.hack;

import net.minecraft.client.Minecraft;

public abstract class HackModule {
    protected static final Minecraft mc = Minecraft.getInstance();
    private final String name;
    private final String desc;
    private final String category;
    private boolean enabled;
    private int keyBind = -1;

    public HackModule(String name, String desc, String category) {
        this.name = name;
        this.desc = desc;
        this.category = category;
    }

    public String getName() { return name; }
    public String getDesc() { return desc; }
    public String getCategory() { return category; }
    public boolean isEnabled() { return enabled; }
    public int getKeyBind() { return keyBind; }
    public void setKeyBind(int key) { this.keyBind = key; }

    public void toggle() {
        enabled = !enabled;
        if (enabled) onEnable(); else onDisable();
    }

    public void setEnabled(boolean v) {
        if (enabled == v) return;
        enabled = v;
        if (enabled) onEnable(); else onDisable();
    }

    protected void onEnable() {}
    protected void onDisable() {}
    public void onTick() {}
    public void onRender() {}
}
