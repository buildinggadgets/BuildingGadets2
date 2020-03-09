package com.direwolf20.core.inventory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.items.CapabilityItemHandler;

import java.lang.ref.WeakReference;
import java.util.*;

public class PlayerItemIndex implements IItemIndex {
    private final WeakReference<PlayerEntity> player;
    private InventoryLink boundInv;
    private PlayerItemTransaction transaction;

    public PlayerItemIndex(PlayerEntity player) {
        this.player = new WeakReference<>(Objects.requireNonNull(player, "A player Item Index always requires a player to start with!"));
        this.transaction = new PlayerItemTransaction();
        this.boundInv = null;
    }

    public PlayerItemIndex(PlayerEntity player, InventoryLink boundInv) {
        this(player);
        this.boundInv = boundInv;
    }

    public PlayerItemIndex(PlayerEntity player, List<InventoryLink> boundInv) {
        this(player, boundInv.isEmpty() ? null : boundInv.get(boundInv.size() - 1));
    }

    private Optional<IItemIndex> getBoundIndex() {
        return boundInv != null ? boundInv.getIndex() : Optional.empty();
    }

    @Override
    public IBulkItemTransaction bulkTransaction() {
        return transaction;
    }

    @Override
    public boolean reIndex() {
        if (boundInv != null)
            boundInv.getIndex().ifPresent(IItemIndex::reIndex);

        return false;
    }

    @Override
    public boolean updateIndex() {
        return false;
    }

    @Override
    public BindingResult bind(InventoryLink other) {
        BindingResult res = boundInv == null ? BindingResult.BIND : BindingResult.REPLACE;
        boundInv = other;
        return res;
    }

    @Override
    public boolean unbind(InventoryLink other) {
        if (boundInv != null && boundInv.equals(other)) {
            boundInv = null;
            return true;
        }
        return false;
    }

    @Override
    public List<InventoryLink> boundLinks() {
        return boundInv != null ? Collections.singletonList(boundInv) : Collections.emptyList();
    }

    @Override
    public IItemCache copyCache() {
        return transaction.copyCache();
    }

    private final class PlayerItemTransaction implements IBulkItemTransaction {
        private final Multiset<IndexKey> indices;
        private final Int2ObjectMap<IndexKey> itemBySlot;
        private final Multimap<IndexKey, Integer> slotByItem;
        private Multiset<IndexKey> pendingExtractTransactions;
        private Multiset<IndexKey> pendingInsertTransactions;

        public PlayerItemTransaction() {
            this.indices = HashMultiset.create();
            this.slotByItem = ArrayListMultimap.create();
            this.itemBySlot = new Int2ObjectOpenHashMap<>();
            this.pendingInsertTransactions = null;
            this.pendingExtractTransactions = null;
        }

        @Override
        public CommitResult commit() {
            if (pendingInsertTransactions == null && pendingExtractTransactions == null)
                return CommitResult.NO_ACTION;
            PlayerEntity thePlayer = player.get();
            if (thePlayer == null)
                return CommitResult.NO_ACTION;
            return thePlayer.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).map(handler -> {
                Multiset<IndexKey> missing = HashMultiset.create();
                Multiset<IndexKey> notInserted = HashMultiset.create();
                Optional<IBulkItemTransaction> link = getBoundIndex().map(IItemIndex::bulkTransaction);
                for (Entry<IndexKey> entry : pendingExtractTransactions.entrySet()) {
                    int count = entry.getCount();
                    Iterator<Integer> it = slotByItem.get(entry.getElement()).iterator();
                    while (it.hasNext()) {
                        int i = it.next();
                        count = handler.extractItem(i, count, false).getCount();
                        if (handler.getStackInSlot(i).isEmpty())
                            //only need to clear it out, if it's empty...
                            //the index has already been updated, when extract happened
                            it.remove();
                        if (count == 0)
                            break;
                    }
                    if (count != 0) {
                        final int fCount = count;
                        count = link.map(index -> index.extractItem(entry.getElement(), fCount, false)).orElse(count);
                        if (! link.isPresent())
                            missing.add(entry.getElement(), count);
                    }
                }
                for (Entry<IndexKey> entry : pendingInsertTransactions.entrySet()) {
                    int count = entry.getCount();
                    for (int i : slotByItem.get(entry.getElement())) {
                        count = handler.insertItem(i, entry.getElement().createStack(count), false).getCount();
                        if (count == 0)
                            break;
                    }
                    if (count != 0) {
                        Iterator<Integer> it = slotByItem.get(IndexKey.EMPTY).iterator();
                        IntList addedSlots = new IntArrayList();
                        while (it.hasNext()) {
                            int i = it.next();
                            count = handler.insertItem(i, entry.getElement().createStack(count), false).getCount();
                            if (! handler.getStackInSlot(i).isEmpty()) {
                                addedSlots.add(i);
                                it.remove();
                            }
                            if (count == 0)
                                break;
                        }
                        slotByItem.putAll(entry.getElement(), addedSlots);
                        if (count != 0) {
                            final int fCount = count;
                            count = link.map(index -> index.insertItem(entry.getElement(), fCount, false)).orElse(count);
                            if (! link.isPresent())
                                notInserted.add(entry.getElement(), count);
                        }
                    }
                }
                CommitResult subCommit = link.map(IBulkItemTransaction::commit).orElse(CommitResult.NO_ACTION);
                missing.addAll(subCommit.getMissing());
                notInserted.addAll(subCommit.getNotInserted());
                return new CommitResult(true, missing, notInserted);
            }).orElse(CommitResult.NO_ACTION);

        }

        @Override
        public int extractItem(IndexKey key, int count, boolean simulate) {
            int available = indices.count(key);
            int remainingCount = count;
            if (available < count) {
                remainingCount = remainingCount - available;
                int fRemaining = remainingCount;
                remainingCount = getBoundIndex()
                        .map(IItemIndex::bulkTransaction)
                        .map(index -> index.extractItem(key, fRemaining, simulate))
                        .orElse(remainingCount);
                if (! simulate) {
                    pendingExtractTransactions.add(key, available);
                    indices.remove(key, available);
                }
            }
            return 0;
        }

        @Override
        public int insertItem(IndexKey key, int count, boolean simulate) {
            return 0;
        }

        @Override
        public IItemCache copyCache() {
            return null;
        }
    }
}
