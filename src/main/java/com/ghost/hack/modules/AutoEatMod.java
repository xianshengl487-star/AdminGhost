package com.ghost.hack.modules;

import com.ghost.hack.HackModule;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;

public class AutoEatMod extends HackModule {
    private int tickDelay = 0;

    public AutoEatMod() { super("AutoEat", "\u81ea\u52a8\u8fdb\u98df - \u81ea\u52a8\u5403\u98df\u7269", "\u73a9\u5bb6"); }

    @Override public void onTick() {
        if (mc.player == null || mc.gameMode == null) return;
        if (mc.player.getFoodData().getFoodLevel() >= 20) {
            if (mc.player.isUsingItem()) mc.player.releaseUsingItem();
            return;
        }
        tickDelay++;
        if (tickDelay < 8) return;
        tickDelay = 0;

        if (mc.player.getFoodData().getFoodLevel() < 16) {
            for (int i = 0; i < 9; i++) {
                ItemStack stack = mc.player.getInventory().getItem(i);
                if (stack.getItem().getFoodProperties() != null) {
                    int prev = mc.player.getInventory().selected;
                    mc.player.getInventory().selected = i;
                    mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
                    mc.player.getInventory().selected = prev;
                    break;
                }
            }
        }
    }
}