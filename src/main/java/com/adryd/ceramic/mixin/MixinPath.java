package com.adryd.ceramic.mixin;

import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.TargetPathNode;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Set;

@Mixin(Path.class)
public class MixinPath {
    @Shadow
    @Final
    private boolean reachesTarget;

    @Shadow
    private int currentNodeIndex;

    @Shadow
    private Set<TargetPathNode> debugTargetNodes;

    @Shadow
    @Final
    private BlockPos target;

    @Shadow
    @Final
    private List<PathNode> nodes;

    @Shadow
    private PathNode[] debugNodes;

    @Shadow
    private PathNode[] debugSecondNodes;

    @Inject(method = "toBuffer", at = @At("HEAD"), cancellable = true)
    private void pathToBuffer(PacketByteBuf buf, CallbackInfo ci) {
        buf.writeBoolean(reachesTarget);
        buf.writeInt(currentNodeIndex);
        if (debugTargetNodes != null) {
            buf.writeInt(debugTargetNodes.size());
            debugTargetNodes.forEach(target -> target.write(buf));
        } else {
            buf.writeInt(0);
        }
        buf.writeInt(target.getX());
        buf.writeInt(target.getY());
        buf.writeInt(target.getZ());
        buf.writeInt(nodes.size());
        for (PathNode pathNode : nodes) {
            pathNode.write(buf);
        }
        buf.writeInt(debugNodes.length);
        for (PathNode pathNode2 : debugNodes) {
            pathNode2.write(buf);
        }
        buf.writeInt(debugSecondNodes.length);
        for (PathNode pathNode2 : this.debugSecondNodes) {
            pathNode2.write(buf);
        }
        ci.cancel();
    }
}

