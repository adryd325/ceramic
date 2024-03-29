package com.adryd.ceramic.mixin;

import com.adryd.ceramic.CeramicSettings;
import io.netty.buffer.Unpooled;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.raid.Raid;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.event.PositionSourceType;
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

    // mojmaps: sendPoiPacketsForChunk
    @Inject(method = "sendChunkWatchingChange", at = @At("TAIL"))
    private static void sendPoiPacketsForChunk(ServerWorld world, ChunkPos pos, CallbackInfo ci) {
        // I guess iterate over the chunk and look for POIs??
    }

    // mojmaps: sendPoiAddedPacket
    @Inject(method = "sendPoiAddition", at = @At("TAIL"))
    private static void sendPoiAddition(ServerWorld world, BlockPos pos, CallbackInfo ci) {
        if (CeramicSettings.sendServerDebugInfo) {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeBlockPos(pos); // pos
            buf.writeString(pos.toShortString()); // unknown
            buf.writeInt(world.getPointOfInterestStorage().getFreeTickets(pos)); // freeTicketCount
            sendToNearby(world, buf, DEBUG_POI_ADDED, pos, 30);
        }
    }

    // mojmaps: sendPoiRemovedPacket
    @Inject(method = "sendPoiRemoval", at = @At("TAIL"))
    private static void sendPoiRemoval(ServerWorld world, BlockPos pos, CallbackInfo ci) {
        if (CeramicSettings.sendServerDebugInfo) {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeBlockPos(pos);
            sendToNearby(world, buf, DEBUG_POI_REMOVED, pos, 30);
        }
    }

    // mojmaps: sendPoiTicketCountPacket
    @Inject(method = "sendPointOfInterest", at = @At("TAIL"))
    private static void sendPoiTicketCount(ServerWorld world, BlockPos pos, CallbackInfo ci) {
        if (CeramicSettings.sendServerDebugInfo) {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeBlockPos(pos); // pos
            buf.writeInt(world.getPointOfInterestStorage().getFreeTickets(pos)); // freeTicketCount
            sendToNearby(world, buf, DEBUG_POI_TICKET_COUNT, pos, 30);
        }
    }

    // mojmaps: sendVillageSectionsPacket
    @Inject(method = "sendPoi", at = @At("TAIL"))
    private static void sendVillageSections(ServerWorld world, BlockPos pos, CallbackInfo ci) {
        if (CeramicSettings.sendServerDebugInfo) {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeInt(0); // sectionsAdded
            // for sectionsAdded writeChunkSectionPos
            buf.writeInt(0); // sectionsRemoved
            // for sectionsRemoved writeChunkSectionPos
            sendToNearby(world, buf, DEBUG_VILLAGE_SECTIONS, pos, 30);
        }
    }

    // mojmaps: sendPathFindingPacket
    @Inject(method = "sendPathfindingData", at = @At("TAIL"))
    private static void sendPathfindingData(World world, MobEntity mob, Path path, float nodeReachProximity, CallbackInfo ci) {
        if (CeramicSettings.sendServerDebugInfo && path != null) {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeInt(mob.getId()); // id
            buf.writeFloat(nodeReachProximity); // nodeSize
            path.toBuffer(buf); // path
            // range from renderer, it checks distance between pos and camerapos <= 80.0f
            sendToNearby((ServerWorld) world, buf, DEBUG_PATH, mob.getBlockPos(), 80);
        }
    }

    // mojmaps: sendNeighborsUpdatePacket
    @Inject(method = "sendNeighborUpdate", at = @At("TAIL"))
    private static void sendNeighborUpdate(World world, BlockPos pos, CallbackInfo ci) {
        if (CeramicSettings.sendServerDebugInfo) {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeVarLong(world.getTime()); // time
            buf.writeBlockPos(pos); // pos
            sendToNearby((ServerWorld) world, buf, DEBUG_NEIGHBORS_UPDATE, pos);
        }
    }

    // mojmaps: sendStructurePacket
    @Inject(method = "sendStructureStart", at = @At("TAIL"))
    private static void sendStructureStart(StructureWorldAccess world, StructureStart structureStart, CallbackInfo ci) {
        if (CeramicSettings.sendServerDebugInfo) {
            ServerWorld serverWorld = world.toServerWorld();
            List<StructurePiece> children = structureStart.getChildren();

            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

            // Funny naming in yarn but I think this is what we want
            buf.writeIdentifier(serverWorld.getDimension().effects());

            // BlockBox
            buf.writeInt(structureStart.getBoundingBox().getMinX());
            buf.writeInt(structureStart.getBoundingBox().getMinY());
            buf.writeInt(structureStart.getBoundingBox().getMinZ());
            buf.writeInt(structureStart.getBoundingBox().getMaxX());
            buf.writeInt(structureStart.getBoundingBox().getMaxY());
            buf.writeInt(structureStart.getBoundingBox().getMaxZ());
            // length
            buf.writeInt(children.size());
            for (StructurePiece childPiece : children) {
                // ArrayList<BlockBox>
                buf.writeInt(childPiece.getBoundingBox().getMinX());
                buf.writeInt(childPiece.getBoundingBox().getMinY());
                buf.writeInt(childPiece.getBoundingBox().getMinZ());
                buf.writeInt(childPiece.getBoundingBox().getMaxX());
                buf.writeInt(childPiece.getBoundingBox().getMaxY());
                buf.writeInt(childPiece.getBoundingBox().getMaxZ());

                // ArrayList<Boolean>; true renders as green, false renders as blue
                buf.writeBoolean(false);
            }
            // range from const in structureDebugRenderer
            sendToNearby(serverWorld, buf, DEBUG_STRUCTURES, structureStart.getBoundingBox().getCenter(), 500);
        }
    }

    // mojmaps: sendGoalSelector
    @Inject(method = "sendGoalSelector", at = @At("TAIL"))
    private static void sendGoalSelector(World world, MobEntity mob, GoalSelector goalSelector, CallbackInfo ci) {
        if (CeramicSettings.sendServerDebugInfo && world instanceof ServerWorld) {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeBlockPos(mob.getBlockPos());
            buf.writeInt(mob.getId());
            buf.writeInt(goalSelector.getGoals().size());
            for (PrioritizedGoal goal : goalSelector.getGoals()) {
                buf.writeInt(goal.getPriority());
                buf.writeBoolean(goal.isRunning());
                buf.writeString(goal.getGoal().getClass().getSimpleName());
            }
            sendToNearby((ServerWorld) world, buf, DEBUG_GOAL_SELECTOR, mob.getBlockPos(), 160);
        }
    }

    // mojmaps: sendRaids
    @Inject(method = "sendRaids", at = @At("TAIL"))
    private static void sendRaids(ServerWorld world, Collection<Raid> raids, CallbackInfo ci) {
        if (CeramicSettings.sendServerDebugInfo) {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeInt(raids.size());
            raids.forEach(raid -> {
                buf.writeBlockPos(raid.getCenter());
            });
            // sent to everybody as its every raid in the world
            // gets sent every tick
            sendToAll(world, buf, DEBUG_RAIDS);
        }
    }

    // mojmaps: sendEntityBrain
    @Inject(method = "sendBrainDebugData", at = @At("TAIL"))
    private static void sendBrainDebugData(LivingEntity entity, CallbackInfo ci) {
        if (CeramicSettings.sendServerDebugInfo) {
            // Despite many entities calling sendBrainDebugData, it seems as if it would cause problems, writeBrain also seems to be tuned to only villagers
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeDouble(entity.getX());
            buf.writeDouble(entity.getY());
            buf.writeDouble(entity.getZ());
            buf.writeUuid(entity.getUuid());
            buf.writeInt(entity.getId()); // entityId
            buf.writeString(entity.getUuid().toString()); // name
            if (entity instanceof VillagerEntity) {
                buf.writeString(((VillagerEntity) entity).getVillagerData().getProfession().toString()); // profession
                buf.writeInt(((VillagerEntity) entity).getExperience()); // profession
            } else {
                buf.writeString("");
                buf.writeInt(0);
            }
            buf.writeFloat(entity.getHealth()); // health
            buf.writeFloat(entity.getMaxHealth()); // maxHealth
            writeBrain(entity, buf);
            sendToNearby((ServerWorld) entity.getWorld(), buf, DEBUG_BRAIN, entity.getBlockPos(), 30);
        }
    }

    // mojmaps: sendBeeInfo
    @Inject(method = "sendBeeDebugData", at = @At("TAIL"))
    private static void sendBeeDebugData(BeeEntity bee, CallbackInfo ci) {
        World world = bee.getWorld();
        if (CeramicSettings.sendServerDebugInfo) {
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
            sendToNearby((ServerWorld) world, buf, DEBUG_BEE, new BlockPos((int) bee.getX(), (int) bee.getY(), (int) bee.getZ()), 30);
        }
    }

    // mojmaps: sendGameEventInfo
//    @Inject(method = "sendGameEvent", at = @At("TAIL"))
//    private static void sendGameEvent(World world, GameEvent event, Vec3d pos, CallbackInfo ci) {
//        if (CeramicSettings.sendServerDebugInfo) {
//            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
//            buf.writeIdentifier(Registry.GAME_EVENT.getId(event));
//            buf.write(pos);
//            sendToNearby((ServerWorld) world, buf, DEBUG_GAME_EVENT, pos, event.getRange());
//        }
//    }

    // mojmaps: sendGameEventListenerInfo
        @Inject(method = "sendGameEventListener", at = @At("TAIL"))
    private static void sendGameEventListener(World world, GameEventListener listener, CallbackInfo ci) {
        if (CeramicSettings.sendServerDebugInfo) {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeIdentifier(Registries.POSITION_SOURCE_TYPE.getId(listener.getPositionSource().getType()));
            PositionSourceType.write(listener.getPositionSource(), buf);
            sendToAll((ServerWorld) world, buf, DEBUG_GAME_EVENT_LISTENERS);
        }
    }

    // mojmaps: sendHiveInfo
    @Inject(method = "sendBeehiveDebugData", at = @At("TAIL"))
    private static void sendBeehiveDebugData(World world, BlockPos pos, BlockState state, BeehiveBlockEntity hive, CallbackInfo ci) {
        if (CeramicSettings.sendServerDebugInfo) {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeBlockPos(pos); // hivePos
            // This is likely not vanilla behavior, but I don't know where I would get a name for a hive
            // It doesnt have a UUID so /shrug
            buf.writeString(hive.getPos().toShortString());
            buf.writeInt(hive.getBeeCount());
            buf.writeInt(BeehiveBlockEntity.getHoneyLevel(hive.getCachedState()));
            buf.writeBoolean(hive.isSmoked());
            sendToNearby((ServerWorld) world, buf, DEBUG_HIVE, pos, 30);
        }
    }

    @Shadow
    private static void writeBrain(LivingEntity entity, PacketByteBuf buf) {
    }

    @Shadow
    private static List<String> listMemories(LivingEntity entity, long currentTime) {
        throw new AssertionError();
    }

    @Shadow
    private static void sendToAll(ServerWorld world, PacketByteBuf buf, Identifier channel) {
    }

    @Unique
    private static void sendToNearby(ServerWorld world, PacketByteBuf buf, Identifier channel, BlockPos pos) {
        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(channel, buf);
        for (ServerPlayerEntity playerEntity : world.toServerWorld().getPlayers()) {
            if (playerEntity.squaredDistanceTo(Vec3d.ofCenter(pos)) < (CeramicSettings.sendServerDebugInfoDistance * CeramicSettings.sendServerDebugInfoDistance) || world.isClient() || CeramicSettings.sendServerDebugInfoUnlimitedDistance) {
                playerEntity.networkHandler.sendPacket(packet);
            }
        }
    }

    @Unique
    private static void sendToNearby(ServerWorld world, PacketByteBuf buf, Identifier channel, BlockPos pos, int range) {
        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(channel, buf);
        for (ServerPlayerEntity playerEntity : world.toServerWorld().getPlayers()) {
            if (playerEntity.squaredDistanceTo(Vec3d.ofCenter(pos)) < Math.min(range, CeramicSettings.sendServerDebugInfoDistance * CeramicSettings.sendServerDebugInfoDistance) || world.isClient() || CeramicSettings.sendServerDebugInfoUnlimitedDistance) {
                playerEntity.networkHandler.sendPacket(packet);
            }
        }
    }

}
