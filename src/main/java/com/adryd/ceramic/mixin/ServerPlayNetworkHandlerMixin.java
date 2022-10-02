package com.adryd.ceramic.mixin;

import com.adryd.ceramic.CeramicSettings;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow
    private int messageCooldown;

    @Shadow
    private int creativeItemDropThreshold;

    /**
     * @author adryd
     * @reason Prevent players from being kicked from command spam
     */
    @Inject(method = "checkForSpam", at = @At(value = "HEAD"), cancellable = true)
    private void handleMessage(CallbackInfo ci) {
        if (CeramicSettings.disableMessageCooldown) {
            this.messageCooldown = 0;
            ci.cancel();
        }
    }

    /**
     * @author adryd
     * @reason Allow creative players to drop from creative inventory at whatever speed they like
     */
    @Inject(method = "onCreativeInventoryAction", at = @At(value = "TAIL"))
    private void onCreativeInventoryAction(CallbackInfo ci) {
        if (CeramicSettings.disableCreativeItemDropCooldown) {
            this.creativeItemDropThreshold = 0;
        }
    }
}