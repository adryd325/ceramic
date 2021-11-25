package com.adryd.ceramic.command;

import carpet.settings.SettingsManager;
import com.adryd.ceramic.CeramicSettings;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SideShapeType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

import static net.minecraft.server.command.CommandManager.literal;

public class HomeCommand {
    private static boolean isSpawnable(ServerWorld world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();
        boolean canMobSpawnInside = block.canMobSpawnInside();
        boolean canMobSpawnAbove = world.getBlockState(pos.up()).getBlock().canMobSpawnInside();
        boolean isSolidBelow = world.getBlockState(pos.down()).isSideSolid(world, pos.down(), Direction.DOWN, SideShapeType.FULL);
        return canMobSpawnInside && canMobSpawnAbove && isSolidBelow;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> command = literal("home")
                .requires((player) -> SettingsManager.canUseCommand(player, CeramicSettings.commandHome))
                .executes((context) -> execute(context.getSource()));
        dispatcher.register(command);
    }

    public static int execute(ServerCommandSource source) throws CommandSyntaxException {
        MinecraftServer server = source.getServer();
        ServerPlayerEntity player = source.getPlayer();

        // So funny enough, there's not any simple function to, find the players spawn position
        // Guess we'll do it ourselves
        ServerWorld playerSpawnWorld = server.getWorld(player.getSpawnPointDimension());
        float playerSpawnAngle = player.getSpawnAngle();
        boolean playerSpawnSet = player.isSpawnPointSet();
        BlockPos playerSpawnPosition = player.getSpawnPointPosition();

        boolean success = false;
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
                player.teleport(playerSpawnWorld, playerRespawnPosition.getX(), playerRespawnPosition.getY(), playerRespawnPosition.getZ(), yaw, 0);
                success = true;
            }
        }
        if (!success) {
            // If we don't have a bed
            ServerWorld spawnWorld = server.getOverworld();
            BlockPos spawnPosition = spawnWorld.getSpawnPos();
            float spawnAngle = spawnWorld.getSpawnAngle();

            // Find spawnable space above
            BlockPos safePosition = spawnPosition;
            while (!spawnWorld.isOutOfHeightLimit(safePosition)) {
                if (isSpawnable(spawnWorld, safePosition)) {
                    player.teleport(spawnWorld, safePosition.getX() + 0.5D, safePosition.getY() + 0.1D, safePosition.getZ() + 0.5D, spawnAngle, 0);
                    success = true;
                    break;
                }
                safePosition = safePosition.up();
            }
            if (!success) {
                // Find spawnable space below
                // Someone please optimize this im lazy af
                safePosition = spawnPosition;
                while (!spawnWorld.isOutOfHeightLimit(safePosition)) {
                    if (isSpawnable(spawnWorld, safePosition)) {
                        player.teleport(spawnWorld, safePosition.getX() + 0.5D, safePosition.getY() + 0.1D, safePosition.getZ() + 0.5D, spawnAngle, 0);
                        success = true;
                        break;
                    }
                    safePosition = safePosition.down();
                }
            }
        }
        if (success) {
            source.sendFeedback(new LiteralText("Teleported to your spawn point!"), false);
        } else {
            source.sendError(new LiteralText("Could not find suitable spawn position"));
        }
        return success ? 1 : 0;
    }
}
