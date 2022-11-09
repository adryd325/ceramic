package com.adryd.ceramic.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow public abstract NbtCompound getOrCreateNbt();

    @Shadow @Nullable private NbtCompound nbt;

    /**
     * @author adryd
     * @reason bypass thing
     */
    @Overwrite
    public void addEnchantment(Enchantment enchantment, int level) {
        this.getOrCreateNbt();
        if (!this.nbt.contains("Enchantments", 9)) {
            this.nbt.put("Enchantments", new NbtList());
        }

        NbtList nbtList = this.nbt.getList("Enchantments", 10);
        nbtList.add(EnchantmentHelper.createNbt(EnchantmentHelper.getEnchantmentId(enchantment), level));
    }
}
