package com.adryd.ceramic.mixin;

import com.adryd.ceramic.CeramicSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FarmlandBlock.class)
public abstract class FarmlandBlockMixin extends Block {
    // Learned this trick from https://github.com/unascribed/Fabrication/blob/1.17/src/main/java/com/unascribed/fabrication/mixin/c_tweaks/no_trample/MixinFarmBlock.java
    public FarmlandBlockMixin(Settings settings) {
        super(settings);
    }

    /**
     * @author adryd
     * @reason Prevent players or mobs from accidentally trampling farmland
     */
    // Had originally done this with @Redirect on the random function but then looked at how fabrication does it
    // this is much, much less cursed...
    @Inject(method = "onLandedUpon", at = @At("HEAD"), cancellable = true)
    private void cancelLandedUppon(World world, BlockState state, BlockPos pos, Entity entity, float fallDistance, CallbackInfo ci) {
        if (CeramicSettings.disableBlockTrampling) {
            super.onLandedUpon(world, state, pos, entity, fallDistance);
            ci.cancel();
        }
    }
}
