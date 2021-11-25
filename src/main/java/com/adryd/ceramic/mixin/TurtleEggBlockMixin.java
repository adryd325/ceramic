package com.adryd.ceramic.mixin;

import com.adryd.ceramic.CeramicSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.TurtleEggBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TurtleEggBlock.class)
public class TurtleEggBlockMixin {
    /**
     * @author adryd
     * @reason Prevent players or mobs from accidentally trampling turtle eggs
     */
    @Inject(method = "tryBreakEgg", at = @At("HEAD"), cancellable = true)
    private void cancelBreakEgg(World world, BlockState state, BlockPos pos, Entity entity, int inverseChance, CallbackInfo ci) {
        if (CeramicSettings.disableBlockTrampling) {
            ci.cancel();
        }
    }

}
