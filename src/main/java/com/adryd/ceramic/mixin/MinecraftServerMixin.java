package com.adryd.ceramic.mixin;

import com.adryd.ceramic.CeramicSettings;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    Random random = new Random();
    @Shadow private String resourcePackUrl;

    /**
     * @author adryd
     * @reason Hide that the server is modded when needed.
     */
    @DontObfuscate
    @Overwrite(remap = false)
    public String getServerModName() {
        return CeramicSettings.hideServerBrand ? "vanilla" : "fabric";
    }

    /**
     * @author adryd
     * @reason Hacky always have clients update their resource pack
     */
    @Inject(method="getResourcePackUrl", at=@At("HEAD"), cancellable = true)
    public void getResourcePackUrl(CallbackInfoReturnable<String> cir) {
        if (CeramicSettings.alwaysRefreshResourcePack && !this.resourcePackUrl.equals("")) {
            cir.setReturnValue(this.resourcePackUrl + String.format("?t=%d", random.nextInt()));
            cir.cancel();
        }
    }
}
