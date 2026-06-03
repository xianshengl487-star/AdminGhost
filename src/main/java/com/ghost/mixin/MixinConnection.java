package com.ghost.mixin;

import com.ghost.AdminGhost;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class MixinConnection {
    @Inject(method = "send", at = @At("HEAD"), cancellable = false, require = 0)
    private void onSend(Packet<?> packet, CallbackInfo ci) {
        if (AdminGhost.SCANNER != null) {
            AdminGhost.SCANNER.onPacket(packet, true);
        }
    }

    @Inject(method = "genericsFtw", at = @At("HEAD"), cancellable = false, require = 0)
    private static void onReceive(Packet<?> packet, PacketListener listener, CallbackInfo ci) {
        if (AdminGhost.SCANNER != null) {
            AdminGhost.SCANNER.onPacket(packet, false);
        }
    }
}