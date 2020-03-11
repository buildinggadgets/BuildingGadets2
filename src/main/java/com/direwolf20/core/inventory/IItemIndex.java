package com.direwolf20.core.inventory;

import net.minecraft.item.ItemStack;

import java.util.List;

public interface IItemIndex {
    enum BindingResult {
        NO_BIND,
        REPLACE,
        BIND
    }

    IBulkExtractTransaction bulkTransaction();

    default int extractItem(ItemStack stack) {
        return extractItem(IndexKey.ofStack(stack), stack.getCount());
    }

    default int extractItem(IndexKey key, int count) {
        IBulkExtractTransaction transaction = bulkTransaction();
        count = transaction.extractItem(key, count);
        transaction.commit();
        return count;
    }

    ItemExtractionCache createExtractionSimulation();

    default int insertItem(ItemStack stack, boolean simulate) {
        return insertItem(IndexKey.ofStack(stack), stack.getCount(), simulate);
    }

    int insertItem(IndexKey key, int count, boolean simulate);

    /**
     * Calling this Method will ensure that the index is accurate. Any sub-sequent extract and insert calls will reflect exactly
     * the environments state (until the env. is changed of course). Notice that calling this Method may result in a high computational cost!
     * <p>
     * Use this Method in favor of {@link #updateIndex()}, if you need the index to be accurate right now.
     *
     * @return Whether or not the index is up-to-date after this Method call, or not.
     */
    boolean reIndex();

    /**
     * Perform some update action on the index. This is not guaranteed to provide a fully accurate index, nor is it
     * guaranteed to perform any update at all. How much is updated (if at all), is entirely up to the implementation.
     * It is recommended that an implementation ensures however, that repeated calls to this Method (how many is unspecified)
     * have the same result as {@link #reIndex()}
     * <p>
     * Use this Method for in favor of {@link #reIndex()}, if you don't need the index to be accurate and can live with it catching a change
     * only after a few calls to this Methods (for example some ticks later).
     *
     * @return Whether anything was updated or not
     */
    boolean updateIndex();

    BindingResult bind(InventoryLink other);

    boolean unbind(InventoryLink other);

    List<InventoryLink> boundLinks();
}
