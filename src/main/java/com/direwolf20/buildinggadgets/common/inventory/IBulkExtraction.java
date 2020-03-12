package com.direwolf20.buildinggadgets.common.inventory;

import net.minecraft.item.ItemStack;

public interface IBulkExtraction {
    void commit();

    default int extractItem(ItemStack stack) {
        return extractItem(IndexKey.ofStack(stack), stack.getCount());
    }

    int extractItem(IndexKey key, int count);
}
