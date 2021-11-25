package com.adryd.ceramic.mixin;

import com.adryd.ceramic.CeramicSettings;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.EnchantCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collection;

@Mixin(EnchantCommand.class)
public class EnchantCommandMixin {
    /**
     * @author adryd
     * @reason Remove maximum level check
     */
    @Redirect(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/Enchantment;getMaxLevel()I"))
    private static int removeLevelRestriction(Enchantment enchantment) {
        if (CeramicSettings.enchantCommandDisableRestrictions) {
            return Integer.MAX_VALUE;
        }
        return enchantment.getMaxLevel();
    }

    /**
     * @author adryd
     * @reason Remove item type check (e.g. Sharpness on a helmet)
     */
    @Redirect(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/Enchantment;isAcceptableItem(Lnet/minecraft/item/ItemStack;)Z"))
    private static boolean removeAcceptableCheck(Enchantment enchantment, ItemStack stack) {
        if (CeramicSettings.enchantCommandDisableRestrictions) {
            return true;
        }
        return enchantment.isAcceptableItem(stack);
    }

    /**
     * @author adryd
     * @reason Remove compatibility check (e.g. Infinity + Mending)
     */
    @Redirect(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;isCompatible(Ljava/util/Collection;Lnet/minecraft/enchantment/Enchantment;)Z"))
    private static boolean removeCompatibilityCheck(Collection<Enchantment> existing, Enchantment enchantment) {
        if (CeramicSettings.enchantCommandDisableRestrictions) {
            return true;
        }

        return EnchantmentHelper.isCompatible(existing, enchantment);
    }
}
