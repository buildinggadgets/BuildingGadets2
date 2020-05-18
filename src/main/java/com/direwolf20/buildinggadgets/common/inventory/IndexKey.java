package com.direwolf20.buildinggadgets.common.inventory;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public final class IndexKey {
    private static final IndexKey EMPTY = new IndexKey(ItemStack.EMPTY.getItem(), getIndexKeyNBT(ItemStack.EMPTY));
    private final Item item;
    private final CompoundNBT nbt;
    private int hashCode;

    private IndexKey(Item item, CompoundNBT nbt) {
        this.hashCode = 31 * item.hashCode() + nbt.hashCode();
        this.item = item;
        this.nbt = nbt;
    }

    public static IndexKey empty() {
        return EMPTY;
    }

    public static IndexKey ofStack(ItemStack stack) {
        if (stack.isEmpty())
            return empty();
        return new IndexKey(stack.getItem(), getIndexKeyNBT(stack));
    }

    private static CompoundNBT getIndexKeyNBT(ItemStack stack) {
        CompoundNBT nbt = stack.serializeNBT();
        nbt.remove("Count");
        return nbt;
    }

    public ItemStack createStack(int count) {
        ItemStack res = ItemStack.read(nbt); //notice that this
        res.setCount(count); //if count is bigger then 128...
        return res;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o.getClass() != IndexKey.class)
            return false;

        final IndexKey indexKey = (IndexKey) o;

        return indexKey.item == item &&
                //first test the hash - if we are lucky, this rules out some more, before we have to test nbt.equals
                indexKey.hashCode == hashCode &&
                indexKey.nbt.equals(nbt);
    }
}
