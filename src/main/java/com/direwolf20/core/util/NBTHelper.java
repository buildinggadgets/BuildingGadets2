package com.direwolf20.core.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public final class NBTHelper {
    private NBTHelper () {
        throw new AssertionError();
    }

    public static CompoundNBT getOrCreateTag(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        if (tag == null) {
            tag = new CompoundNBT();
            stack.setTag(tag);
        }
        return tag;
    }
}
