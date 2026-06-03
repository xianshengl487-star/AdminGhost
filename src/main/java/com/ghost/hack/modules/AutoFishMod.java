package com.ghost.hack.modules;

import com.ghost.hack.HackModule;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionHand;

public class AutoFishMod extends HackModule {
    private int delay = 0;

    public AutoFishMod() { super("AutoFish", "\u81ea\u52a8\u9493\u9c7c - \u81ea\u52a8\u62c9\u7aff", "\u73a9\u5bb6"); }

    @Override public void onTick() {
        if (mc.player == null) return;
        delay++;
        if (delay < 20) return;
        delay = 0;

        // If holding fishing rod, recast
        if (mc.player.getMainHandItem().getItem() == Items.FISHING_ROD) {
            if (mc.player.fishing != null) {
                // Fish is on hook, recast
                mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
                // Small delay then recast
                mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
            }
        }
    }
}
