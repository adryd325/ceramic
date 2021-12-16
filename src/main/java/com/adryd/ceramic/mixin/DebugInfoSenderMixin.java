package com.adryd.ceramic.mixin;

import com.adryd.ceramic.CeramicSettings;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.raid.Raid;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.listener.GameEventListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.List;

import static net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket.*;

@Mixin(DebugInfoSender.class)
public abstract class DebugInfoSenderMixin {
    @Shadow
    private static void sendToAll(ServerWorld world, PacketByteBuf buf, Identifier channel) {
        throw new AssertionError();
    }

    @Shadow private static List<String> listMemories(LivingEntity entity, long currentTime) {
        throw new AssertionError();
    }

    @Unique
    private static void sendToNearby(ServerWorld world, PacketByteBuf buf, Identifier channel, BlockPos pos) {
        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(channel, buf);
        for (ServerPlayerEntity playerEntity : world.toServerWorld().getPlayers()) {
            if (playerEntity.squaredDistanceTo(Vec3d.ofCenter(pos)) < (CeramicSettings.sendServerDebugInfoDistance * CeramicSettings.sendServerDebugInfoDistance)) {
                playerEntity.networkHandler.sendPacket(packet);
            }
        }
    }

    @Unique
    private static void sendPoi(ServerWorld world, BlockPos pos, Identifier identifier) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(pos);
        sendToNearby(world, buf, identifier, pos);
    }

    @Inject(method = "sendPoiAddition", at = @At("HEAD"), cancellable = true)
    private static void sendPoiAddition(ServerWorld world, BlockPos pos, CallbackInfo ci) {
        if (CeramicSettings.sendServerDebugInfo) {
            sendPoi(world, pos, DEBUG_POI_ADDED);
            ci.cancel();
        }
    }

    @Inject(method = "sendPoiRemoval", at = @At("HEAD"), cancellable = true)
    private static void sendPoiRemoval(ServerWorld world, BlockPos pos, CallbackInfo ci) {
        if (CeramicSettings.sendServerDebugInfo) {
            sendPoi(world, pos, DEBUG_POI_REMOVED);
            ci.cancel();
        }
    }

    @Inject(method = "sendPathfindingData", at = @At("HEAD"), cancellable = true)
    private static void sendPathfindingData(World world, MobEntity mob, Path path, float nodeReachProximity, CallbackInfo ci) {
        if (world instanceof ServerWorld && CeramicSettings.sendServerDebugInfo && path != null) {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeInt(mob.getId());
            buf.writeFloat(nodeReachProximity);
            path.toBuffer(buf);
            sendToNearby((ServerWorld) world, buf, DEBUG_PATH, mob.getBlockPos());
            ci.cancel();
        }
    }

    @Inject(method = "sendNeighborUpdate", at = @At("HEAD"), cancellable = true)
    private static void sendNeighborUpdate(World world, BlockPos pos, CallbackInfo ci) {
        if (world instanceof ServerWorld && CeramicSettings.sendServerDebugInfo) {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeVarLong(world.getTime());
            buf.writeBlockPos(pos);
            sendToNearby((ServerWorld) world, buf, DEBUG_NEIGHBORS_UPDATE, pos);
            ci.cancel();
        }
    }

    @Inject(method = "sendStructureStart", at = @At("HEAD"), cancellable = true)
    private static void sendStructureStart(StructureWorldAccess world, StructureStart<?> structureStart, CallbackInfo ci) {
        if (CeramicSettings.sendServerDebugInfo) {
            ServerWorld serverWorld = world.toServerWorld();
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            List<StructurePiece> children = structureStart.getChildren();
            buf.writeIdentifier(serverWorld.getRegistryKey().getValue());
            buf.writeInt(structureStart.getBoundingBox().getMinX());
            buf.writeInt(structureStart.getBoundingBox().getMinY());
            buf.writeInt(structureStart.getBoundingBox().getMinZ());
            buf.writeInt(structureStart.getBoundingBox().getMaxX());
            buf.writeInt(structureStart.getBoundingBox().getMaxY());
            buf.writeInt(structureStart.getBoundingBox().getMaxZ());
            buf.writeInt(children.size());
            for (StructurePiece childPiece : children) {
                buf.writeInt(childPiece.getBoundingBox().getMinX());
                buf.writeInt(childPiece.getBoundingBox().getMinY());
                buf.writeInt(childPiece.getBoundingBox().getMinZ());
                buf.writeInt(childPiece.getBoundingBox().getMaxX());
                buf.writeInt(childPiece.getBoundingBox().getMaxY());
                buf.writeInt(childPiece.getBoundingBox().getMaxZ());
                buf.writeBoolean(true);
            }
            sendToNearby(serverWorld, buf, DEBUG_STRUCTURES, structureStart.getBoundingBox().getCenter());
            ci.cancel();
        }
    }

    @Inject(method = "sendGoalSelector", at = @At("HEAD"), cancellable = true)
    private static void sendGoalSelector(World world, MobEntity mob, GoalSelector goalSelector, CallbackInfo ci) {
        if (CeramicSettings.sendServerDebugInfo && world instanceof ServerWorld) {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeBlockPos(mob.getBlockPos());
            buf.writeInt(mob.getId()); // "index"
            buf.writeInt(goalSelector.getGoals().size());
            for (PrioritizedGoal goal : goalSelector.getGoals()) {
                buf.writeInt(goal.getPriority());
                buf.writeBoolean(goal.isRunning());
                buf.writeString(goal.getGoal().getClass().getSimpleName());
            }
            sendToNearby((ServerWorld) world, buf, DEBUG_GOAL_SELECTOR, mob.getBlockPos());
            ci.cancel();
        }
    }

    @Inject(method = "sendRaids", at = @At("HEAD"), cancellable = true)
    private static void sendRaids(ServerWorld world, Collection<Raid> raids, CallbackInfo ci) {
        if (CeramicSettings.sendServerDebugInfo) {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeInt(raids.size());
            raids.forEach(raid -> {
                buf.writeBlockPos(raid.getCenter());
            });
            sendToAll(world, buf, DEBUG_RAIDS);
            ci.cancel();
        }
    }

    @Inject(method = "sendBrainDebugData", at = @At("HEAD"), cancellable = true)
    private static void sendBrainDebugData(LivingEntity living, CallbackInfo ci) {

    }

    @Inject(method = "sendBeeDebugData", at = @At("HEAD"), cancellable = true)
    private static void sendBeeDebugData(BeeEntity bee, CallbackInfo ci) {
        World world = bee.getWorld();
        if (CeramicSettings.sendServerDebugInfo && world instanceof ServerWorld) {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeDouble(bee.getX());
            buf.writeDouble(bee.getY());
            buf.writeDouble(bee.getZ());
            buf.writeUuid(bee.getUuid());
            buf.writeInt(bee.getId());
            if (bee.hasHive()) {
                buf.writeBoolean(true);
                buf.writeBlockPos(bee.getHivePos());
            } else {
                buf.writeBoolean(false);
            }
            if (bee.hasFlower()) {
                buf.writeBoolean(true);
                buf.writeBlockPos(bee.getFlowerPos());
            }
            buf.writeInt(bee.getMoveGoalTicks());
            Path path = bee.getNavigation().getCurrentPath();
            if (path != null) {
                buf.writeBoolean(true);
                path.toBuffer(buf);
            } else {
                buf.writeBoolean(false);
            }
            List<String> memories = listMemories(bee, world.getTime());
            buf.writeVarInt(memories.size());
            for (String memory : memories) {
                buf.writeString(memory);
            }
            buf.writeVarInt(0);
            sendToNearby((ServerWorld) world, buf, DEBUG_BEE, new BlockPos(bee.getX(), bee.getY(), bee.getZ()));
            ci.cancel();
        }
    }

    @Inject(method = "sendGameEvent", at = @At("HEAD"), cancellable = true)
    private static void sendGameEvent(World world, GameEvent event, BlockPos pos, CallbackInfo ci) {

    }

    @Inject(method = "sendGameEventListener", at = @At("HEAD"), cancellable = true)
    private static void sendGameEventListener(World world, GameEventListener eventListener, CallbackInfo ci) {

    }

    @Inject(method = "sendBeeDebugData", at = @At("HEAD"), cancellable = true)
    private static void sendBeehiveDebugData(BeeEntity bee, CallbackInfo ci) {
    }
}
