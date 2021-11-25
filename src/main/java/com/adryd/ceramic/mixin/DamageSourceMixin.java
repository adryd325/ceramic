package com.adryd.ceramic.mixin;

import com.adryd.ceramic.CeramicSettings;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DamageSource.class)
public class DamageSourceMixin {
    /**
     * @author adryd
     * @reason Prevent creative players from killing invulnerable mobs
     */
    @Inject(method = "isSourceCreativePlayer", at=@At(value = "HEAD"), cancellable = true)
    private void cancelCreativePlayerAttack(CallbackInfoReturnable<Boolean> cir) {
        if (CeramicSettings.invulnerableAreImmuneToCreativePlayers) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
