package com.adryd.ceramic.mixin;

import com.adryd.ceramic.CeramicSettings;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    /**
     * @author adryd, NotNite
     * @reason Enable whitelist when all OPs have left.
     */
    @Inject(method = "remove", at = @At("TAIL"))
    private void enableWhitelistOnPlayerRemove(ServerPlayerEntity player, CallbackInfo ci) {
        if (CeramicSettings.opsMonitorWhitelist) {
            PlayerManager playerManager = (PlayerManager) (Object) this;
            // note: confirm if the disconnected player isn't in this stream, and if it contains any disconnected players at all
            long opsOnline = ((PlayerManagerAccessor) playerManager).getPlayers().stream().filter(x -> x.hasPermissionLevel(4)).count();
            if (opsOnline < 1) {
                playerManager.setWhitelistEnabled(true);
            }
        }
    }
}
