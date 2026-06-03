package com.ghost.hack.modules;

import com.ghost.hack.HackModule;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;

public class AutoTotemMod extends HackModule {
    private int tickDelay = 0;

    public AutoTotemMod() { super("AutoTotem", "\u81ea\u52a8\u56fe\u817e - \u81ea\u52a8\u88c5\u5907\u4e0d\u6b7b\u56fe\u817e", "\u6218\u6597"); }

    @Override public void onTick() {
        if (mc.player == null) return;
        tickDelay++;
        if (tickDelay < 4) return;
        tickDelay = 0;

        ItemStack offhand = mc.player.getOffhandItem();
        if (offhand.getItem() == Items.TOTEM_OF_UNDYING) return;

        // Find totem in inventory
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                // Swap with offhand (slot 45)
                int windowId = mc.player.containerMenu.containerId;
                // Click totem, then click offhand
                mc.gameMode.handleInventoryMouseClick(windowId, i < 9 ? i + 36 : i, 0, net.minecraft.world.inventory.ClickType.PICKUP, mc.player);
                mc.gameMode.handleInventoryMouseClick(windowId, 45, 0, net.minecraft.world.inventory.ClickType.PICKUP, mc.player);
                break;
            }
        }
    }
}
