package com.adryd.ceramic.mixin;

import com.adryd.ceramic.CeramicSettings;
import net.minecraft.server.dedicated.DedicatedPlayerManager;
import com.mojang.authlib.GameProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(DedicatedPlayerManager.class)
public class DedicatedPlayerManagerMixin {
    /**
     * @author NotNite
     * @reason Removes ability for ops to bypass whitelist.
     */
    @Overwrite
    public boolean isWhitelisted(GameProfile profile) {
        DedicatedPlayerManager playerManager = (DedicatedPlayerManager) (Object) this;
        if (CeramicSettings.disableOpsBypassWhitelist) {
            return !playerManager.isWhitelistEnabled() || playerManager.getWhitelist().isAllowed(profile);
        }
        return !playerManager.isWhitelistEnabled() || playerManager.isOperator(profile) || playerManager.getWhitelist().isAllowed(profile);
    }
}
