package com.adryd.ceramic.mixin;

import com.adryd.ceramic.CeramicSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CreeperEntity.class)
public class MixinCreeperEntity {
    // I really wish I didn't have to redirect here, but I can't figure out @ModifyVariable for the life of me
    @Redirect(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;createExplosion(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/world/World$ExplosionSourceType;)Lnet/minecraft/world/explosion/Explosion;"))
    private Explosion modifyDestroyType(World instance, Entity entity, double x, double y, double z, float power, World.ExplosionSourceType explosionSourceType) {
        if (CeramicSettings.creepersHealthExplosionStrength) {
            power *= ((LivingEntity) entity).getHealth() / ((LivingEntity) entity).getMaxHealth() ;
        }
        if (CeramicSettings.creepersDropBlocks && explosionSourceType == World.ExplosionSourceType.MOB) {
            explosionSourceType = World.ExplosionSourceType.TNT;
        }

        return instance.createExplosion(entity,x,y,z,power,explosionSourceType);
    }
}
