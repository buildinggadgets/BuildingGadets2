package com.direwolf20.core.inventory;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import net.minecraft.item.ItemStack;

import java.util.Objects;

public final class ItemExtractionCache {
    private final Multiset<IndexKey> cache;

    public ItemExtractionCache(Multiset<IndexKey> cache) {
        this.cache = Objects.requireNonNull(cache);
    }

    public static ItemExtractionCache createMerged(ItemExtractionCache cache1, ItemExtractionCache cache2) {
        HashMultiset<IndexKey> cache = HashMultiset.create(cache1.cache);
        cache.addAll(cache2.cache);
        return new ItemExtractionCache(cache);
    }

    public int simulateExtract(ItemStack stack) {
        return simulateExtract(IndexKey.ofStack(stack), stack.getCount());
    }

    public int simulateExtract(IndexKey key, int count) {
        return Math.max(cache.remove(key, count) - count, 0);
    }
}
