package com.ghost.hack.modules;

import com.ghost.hack.HackModule;
import net.minecraft.world.entity.player.Player;

public class SpeedMod extends HackModule {
    private double speedMultiplier = 2.5;

    public SpeedMod() { super("Speed", "\u79fb\u52a8\u52a0\u901f - \u591a\u500d\u901f\u5ea6", "\u79fb\u52a8"); }

    @Override public void onTick() {
        Player p = mc.player;
        if (p == null || !p.onGround()) return;
        if (!mc.options.keyUp.isDown() && !mc.options.keyDown.isDown() &&
            !mc.options.keyLeft.isDown() && !mc.options.keyRight.isDown()) return;

        double yaw = Math.toRadians(p.getYRot());
        double forward = 0, strafe = 0;
        if (mc.options.keyUp.isDown()) forward += 1;
        if (mc.options.keyDown.isDown()) forward -= 1;
        if (mc.options.keyLeft.isDown()) strafe += 1;
        if (mc.options.keyRight.isDown()) strafe -= 1;
        // Normalize diagonal
        double len = Math.sqrt(forward*forward + strafe*strafe);
        if (len > 0) { forward /= len; strafe /= len; }
        double cos = Math.cos(yaw), sin = Math.sin(yaw);
        double mx = (forward * sin + strafe * cos) * speedMultiplier * 0.2;
        double mz = (forward * cos - strafe * sin) * speedMultiplier * 0.2;
        p.setDeltaMovement(mx, p.getDeltaMovement().y, mz);
    }
}
