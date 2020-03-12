package com.direwolf20.buildinggadgets.common.inventory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Objects;

public final class ItemExtractionCache {
    private final List<Multiset<IndexKey>> caches;

    public ItemExtractionCache(List<Multiset<IndexKey>> cache) {
        this.caches = Objects.requireNonNull(cache);
    }

    public ItemExtractionCache(Multiset<IndexKey> cache) {
        this(ImmutableList.of(cache));
    }

    public static ItemExtractionCache createMerged(ItemExtractionCache... caches) {
        ImmutableList.Builder<Multiset<IndexKey>> builder = ImmutableList.builder();
        for (ItemExtractionCache cache : caches)
            builder.addAll(cache.caches);
        return new ItemExtractionCache(builder.build());
    }

    public int simulateExtract(ItemStack stack) {
        return simulateExtract(IndexKey.ofStack(stack), stack.getCount());
    }

    public int simulateExtract(IndexKey key, int count) {
        for (Multiset<IndexKey> cache : caches) {
            int available = cache.remove(key, count);
            count = available > count ? 0 : count - available;
            if (count <= 0)
                return 0;
        }
        return count;
    }
}
