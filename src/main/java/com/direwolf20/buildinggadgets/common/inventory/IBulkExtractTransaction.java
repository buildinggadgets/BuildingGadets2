package com.direwolf20.buildinggadgets.common.inventory;

import net.minecraft.item.ItemStack;

public interface IBulkExtractTransaction {
    void commit();

    default int extractItem(ItemStack stack, int count) {
        return extractItem(IndexKey.ofStack(stack), count);
    }

    int extractItem(IndexKey key, int count);


}
