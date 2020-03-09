package com.direwolf20.core.inventory;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public interface IItemCache {
    default int extractItem(ItemStack stack, boolean simulate) {
        return extractItem(stack.getItem(), IndexKey.getIndexKeyNBT(stack), stack.getCount(), simulate);
    }

    default int extractItem(Item item, CompoundNBT itemNBT, int count, boolean simulate) {
        return extractItem(new IndexKey(item, itemNBT), count, simulate);
    }

    int extractItem(IndexKey key, int count, boolean simulate);

    default int insertItem(ItemStack stack, boolean simulate) {
        return insertItem(stack.getItem(), IndexKey.getIndexKeyNBT(stack), stack.getCount(), simulate);
    }

    default int insertItem(Item item, CompoundNBT itemNBT, int count, boolean simulate) {
        return insertItem(new IndexKey(item, itemNBT), count, simulate);
    }

    int insertItem(IndexKey key, int count, boolean simulate);

    IItemCache copyCache();
}
