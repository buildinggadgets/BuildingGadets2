package com.direwolf20.buildinggadgets.common.inventory;

import com.direwolf20.buildinggadgets.common.inventory.link.InventoryLink;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.List;

public interface IItemIndex {
    enum BindingResult {
        NO_BIND,
        REPLACE,
        BIND
    }

    IBulkExtraction bulkTransaction(PlayerEntity player);

    default int extractItem(PlayerEntity player, ItemStack stack) {
        return extractItem(player, IndexKey.ofStack(stack), stack.getCount());
    }

    default int extractItem(PlayerEntity player, IndexKey key, int count) {
        IBulkExtraction transaction = bulkTransaction(player);
        count = transaction.extractItem(key, count);
        transaction.commit();
        return count;
    }

    ItemExtractionCache createExtractionSimulation(PlayerEntity player);

    default int insertItem(PlayerEntity player, ItemStack stack, boolean simulate) {
        return insertItem(player, IndexKey.ofStack(stack), stack.getCount(), simulate);
    }

    int insertItem(PlayerEntity player, IndexKey key, int count, boolean simulate);

    /**
     * Calling this Method will ensure that the index is accurate. Any sub-sequent extract and insert calls will reflect exactly
     * the environments state (until the env. is changed of course). Notice that calling this Method may result in a high computational cost!
     * <p>
     * Use this Method in favor of {@link #updateIndex(PlayerEntity, int)} )}, if you need the index to be accurate right now.
     *
     * @return Whether or not the index is up-to-date after this Method call, or not.
     */
    boolean reIndex(PlayerEntity player);

    /**
     * Perform some update action on the index. This is not guaranteed to provide a fully accurate index, nor is it
     * guaranteed to perform any update at all. How much is updated (if at all), is entirely up to the implementation.
     * It is recommended that an implementation ensures however, that repeated calls to this Method with maxIndex>=1
     * (how many is unspecified) have the same result as {@link #reIndex(PlayerEntity)}.
     * <p>
     * Use this Method for in favor of {@link #reIndex(PlayerEntity)}, if you don't need the index to be accurate and can live with it catching a change
     * only after a few calls to this Methods (for example some ticks later).
     *
     * @return Whether anything was updated or not
     */
    boolean updateIndex(PlayerEntity player, int maxIndex);

    BindingResult bind(InventoryLink other);

    boolean unbind(InventoryLink other);

    List<InventoryLink> boundLinks();
}
