package com.adryd.ceramic.command;

import carpet.settings.SettingsManager;
import carpet.utils.CommandHelper;
import com.adryd.ceramic.Ceramic;
import com.adryd.ceramic.CeramicSettings;
import com.adryd.ceramic.mixin.SpawnLocatingAccessor;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Random;

import static net.minecraft.server.command.CommandManager.literal;

public class HomeCommand {
    private static int calculateSpawnOffsetMultiplier(int horizontalSpawnArea) {
        return horizontalSpawnArea <= 16 ? horizontalSpawnArea - 1 : 17;
    }

    private static void teleport(ServerPlayerEntity player, ServerWorld targetWorld, double x, double y, double z, float yaw, float pitch) {
        ChunkPos chunkPos = new ChunkPos(new BlockPos((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z)));
        targetWorld.getChunkManager().addTicket(ChunkTicketType.POST_TELEPORT, chunkPos, 1, player.getId());
        player.stopRiding();
        if (player.isSleeping()) {
            player.wakeUp(true, true);
        }
        if (targetWorld == player.getWorld()) {
            player.networkHandler.requestTeleport(x, y, z, yaw, pitch, EnumSet.noneOf(PositionFlag.class));
        } else {
            player.teleport(targetWorld, x, y, z, yaw, pitch);
        }
        player.setHeadYaw(yaw);
    }

    private static boolean teleportHome(MinecraftServer server, ServerPlayerEntity player) {
        // So funny enough, there's not any simple function to, find the players spawn position
        // Guess we'll do it ourselves
        ServerWorld playerSpawnWorld = server.getWorld(player.getSpawnPointDimension());
        float playerSpawnAngle = player.getSpawnAngle();
        boolean playerSpawnSet = player.isSpawnForced();
        BlockPos playerSpawnPosition = player.getSpawnPointPosition();

        if (playerSpawnPosition != null) {
            assert playerSpawnWorld != null;
            Optional<Vec3d> playerSuggestedRespawnPosition = PlayerEntity.findRespawnPosition(playerSpawnWorld, playerSpawnPosition, playerSpawnAngle, playerSpawnSet, true);

            // If player has a bed, respawn anchor or has run /spawnpoint
            if (playerSuggestedRespawnPosition.isPresent()) {
                Vec3d playerRespawnPosition = playerSuggestedRespawnPosition.get();
                float yaw = playerSpawnAngle;

                // Face bed or respawn anchor
                BlockState blockState = playerSpawnWorld.getBlockState(playerSpawnPosition);
                if (blockState.isIn(BlockTags.BEDS) || blockState.isOf(Blocks.RESPAWN_ANCHOR)) {
                    // from PlayerManager.java
                    Vec3d playerRespawnBlock = Vec3d.ofBottomCenter(playerSpawnPosition).subtract(playerRespawnPosition).normalize();
                    yaw = (float) MathHelper.wrapDegrees(MathHelper.atan2(playerRespawnBlock.z, playerRespawnBlock.x) * 57.2957763671875D - 90.0D);
                }

                // Finally, teleport
                teleport(player, playerSpawnWorld, playerRespawnPosition.getX(), playerRespawnPosition.getY(), playerRespawnPosition.getZ(), yaw, 0);
                return true;
            }
        }
        return false;
    }

    private static void teleportSpawn(MinecraftServer server, ServerPlayerEntity player) {
        // Can we use the vanilla function?
        // No We Can't! because it doesn't set teleportation state or send teleport packet
        // a (bad) copy of vanilla code
        ServerWorld world = server.getOverworld();
        BlockPos blockPos = world.getSpawnPos();
        int spawnRadius = Math.max(0, server.getSpawnRadius(world));
        int worldBorderDistance = MathHelper.floor(world.getWorldBorder().getDistanceInsideBorder(blockPos.getX(), blockPos.getZ()));
        if (worldBorderDistance < spawnRadius) {
            spawnRadius = worldBorderDistance;
        }
        if (worldBorderDistance <= 1) {
            spawnRadius = 1;
        }

        // If spawn radius times 2 + 1 squared is over the max int
        // horizontalSpawnArea = max int
        // else
        // horizontalSpawnArea = (spawnRadius*2+1)**2
        long l = spawnRadius * 2L + 1;
        long spawnAreaUnsafe = l * l;
        int horizontalSpawnArea = spawnAreaUnsafe > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) spawnAreaUnsafe;
        int spawnOffsetMultiplier = calculateSpawnOffsetMultiplier(horizontalSpawnArea);
        int o = new Random().nextInt(horizontalSpawnArea);
        for (int iterator = 0; iterator < horizontalSpawnArea; ++iterator) {
            int q = (o + spawnOffsetMultiplier * iterator) % horizontalSpawnArea;
            int r = q % (spawnRadius * 2 + 1);
            int s = q / (spawnRadius * 2 + 1);
            BlockPos blockPos2 = SpawnLocatingAccessor.accessOverworldSpawn(world, blockPos.getX() + r - spawnRadius, blockPos.getZ() + s - spawnRadius);
            if (blockPos2 != null) {
                teleport(player, world, blockPos2.getX() + 0.5, blockPos2.getY(), blockPos2.getZ() + 0.5, 0.0f, 0.0f);
                player.refreshPositionAndAngles(blockPos2, 0.0F, 0.0F);
                if (world.isSpaceEmpty(player)) {
                    return;
                }
            }
        }
        player.teleport(world,blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5, 0.0F, 0.0F);
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> command = literal("home")
                .requires((player) -> CommandHelper.canUseCommand(player, CeramicSettings.commandHome))
                .executes((context) -> execute(context.getSource()));
        dispatcher.register(command);
    }

    public static int execute(ServerCommandSource source) throws CommandSyntaxException {
        MinecraftServer server = source.getServer();
        ServerPlayerEntity player = source.getPlayer();

        // yeah, i mean, it works
        if (!teleportHome(server, player)) {
            teleportSpawn(server, player);
        }

        source.sendFeedback(() -> Text.literal("Teleported you home!"), false);
        return 1;
    }
}
