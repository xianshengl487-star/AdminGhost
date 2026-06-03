package com.ghost.hack.modules;

import com.ghost.hack.HackModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class ScaffoldMod extends HackModule {
    public ScaffoldMod() { super("Scaffold", "\u81ea\u52a8\u642d\u6865 - \u81ea\u52a8\u5728\u811a\u4e0b\u653e\u65b9\u5757", "\u73a9\u5bb6"); }

    @Override public void onTick() {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;

        BlockPos below = mc.player.blockPosition().below();
        if (!mc.level.getBlockState(below).isAir()) return;

        // Find block item in hotbar
        int slot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getItem(i).getItem() instanceof BlockItem) {
                slot = i; break;
            }
        }
        if (slot == -1) return;

        int prevSlot = mc.player.getInventory().selected;
        mc.player.getInventory().selected = slot;

        BlockHitResult hit = new BlockHitResult(
            new Vec3(below.getX() + 0.5, below.getY() + 0.5, below.getZ() + 0.5),
            Direction.UP, below, false
        );
        mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit);
        mc.player.getInventory().selected = prevSlot;
    }
}
