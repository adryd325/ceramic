package com.adryd.ceramic.mixin;

import com.adryd.ceramic.CeramicSettings;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    /**
     * @author adryd
     * @reason Hide that the server is modded when needed.
     */
    @DontObfuscate
    @Overwrite(remap = false)
    public String getServerModName() {
        return CeramicSettings.hideServerBrand ? "vanilla" : "fabric";
    }
}
