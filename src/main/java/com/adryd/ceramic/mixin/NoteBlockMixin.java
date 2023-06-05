package com.adryd.ceramic.mixin;

import com.adryd.ceramic.CeramicSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.NoteBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.stat.Stats;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoteBlock.class)
public abstract class NoteBlockMixin extends Block {
    public NoteBlockMixin(Settings settings) {
        super(settings);
    }

    @Shadow
    @Final
    public static IntProperty NOTE;

    @Shadow protected abstract void playNote(@Nullable Entity entity, BlockState state, World world, BlockPos pos);

    // Can't figure out how to get these properly so Whoops you're hardcoded now.
    public final int MAX_NOTE = 24;
    public final int MIN_NOTE = 0;

    /**
     * @author adryd
     * @reason Fabrication port of note block features
     */
    @Inject(method = "onUse", at = @At(value = "HEAD"), cancellable = true)
    public void onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (CeramicSettings.noteBlocksTuneWithSticks && player.isHolding(Items.STICK)) {
            int note = player.getStackInHand(hand).getCount() - 1;
            if (note <= MAX_NOTE && note >= MIN_NOTE) {
                BlockState newState = state.with(NOTE, note);
                world.setBlockState(pos, newState, Block.NOTIFY_ALL);
                this.playNote(player, newState, world, pos);
                player.incrementStat(Stats.TUNE_NOTEBLOCK);
                cir.setReturnValue(ActionResult.CONSUME);
                cir.cancel();
            }
        }
        if (CeramicSettings.noteBlocksTuneBackwards && player.isSneaking()) {
            int newNote = state.get(NOTE) - 1;
            if (newNote < MIN_NOTE) newNote = MAX_NOTE;
            BlockState newState = state.with(NOTE, newNote);
            world.setBlockState(pos, newState, Block.NOTIFY_ALL);
            this.playNote(player, newState, world, pos);
            player.incrementStat(Stats.TUNE_NOTEBLOCK);
            cir.setReturnValue(ActionResult.CONSUME);
            cir.cancel();
        }
    }

    /**
     * @author adryd
     */
    @Override
    public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if (CeramicSettings.noteBlocksPlayOnLanding && entity instanceof PlayerEntity) {
            this.playNote(entity, state, world, pos);
        }
        super.onLandedUpon(world, state, pos, entity, fallDistance);
    }
}
