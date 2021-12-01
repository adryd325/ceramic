package com.adryd.ceramic.mixin;

import net.minecraft.server.network.SpawnLocating;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SpawnLocating.class)
public interface SpawnLocatingAccessor {
     @Invoker("findOverworldSpawn")
     static BlockPos accessOverworldSpawn(ServerWorld world, int x, int z) { throw new AssertionError(""); };
}
