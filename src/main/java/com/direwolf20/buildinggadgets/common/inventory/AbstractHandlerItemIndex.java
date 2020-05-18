package com.direwolf20.buildinggadgets.common.inventory;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import java.util.*;

public abstract class AbstractHandlerItemIndex implements IItemIndex {
    private Set<IBulkExtraction> extractions;

    public AbstractHandlerItemIndex() {
        extractions = new LinkedHashSet<>();
    }

    @Override
    public final IBulkExtraction bulkTransaction(PlayerEntity player) {
        IBulkExtraction extraction = createExtraction(player);
        extractions.add(extraction);
        return extraction;
    }

    @Override
    public ItemExtractionCache createExtractionSimulation(PlayerEntity player) {
        ItemExtractionCache cache = new ItemExtractionCache(HashMultiset.create(getCache(player).getIndices()));
        return getBoundIndex(player)
                .map(index -> ItemExtractionCache.createMerged(index.createExtractionSimulation(player), cache))
                .orElse(cache);
    }

    @Override
    public int insertItem(PlayerEntity player, ItemStack stack, boolean simulate) {
        return getHandler(player)
                .map(handler -> insertStackIntoHandler(player, handler, stack, stack.getCount(), simulate))
                .orElse(0);
    }

    @Override
    public int insertItem(PlayerEntity player, IndexKey key, int count, boolean simulate) {
        return getHandler(player)
                .map(handler -> insertStackIntoHandler(player, handler, key.createStack(count), count, simulate))
                .orElse(0);
    }

    @Override
    public boolean reIndex(PlayerEntity player) {
        getBoundIndex(player).ifPresent(index -> reIndex(player));
        return performIndex(player, 0, Integer.MAX_VALUE) >= 0;
    }

    protected abstract Optional<IItemHandler> getHandler(PlayerEntity player);

    protected abstract IndexCacheObject getCache(PlayerEntity player);

    protected abstract Optional<IItemIndex> getBoundIndex(PlayerEntity player);

    protected IBulkExtraction createExtraction(PlayerEntity player) {
        return new HandlerBulkExtraction(player);
    }

    protected int performIndex(PlayerEntity player, int startIndex, int maxIndexed) {
        return getHandler(player).map(handler -> {
            commitAll();

            IndexCacheObject cache = getCache(player);
            int slots = handler.getSlots();
            int max = startIndex + Math.min(maxIndexed, slots);
            int i = startIndex;

            for (; i < max; i++) {
                int pos = i % slots;
                ItemStack stack = handler.getStackInSlot(pos);

                cache.addStackToCache(stack, pos);
            }

            return i % slots;
        }).orElse(- 1);
    }

    protected void commitAll() {
        for (IBulkExtraction extraction : new ArrayList<>(extractions))
            //copy int a new array list to allow concurrent modification
            //remember that commit will remove this extraction from extractions!
            extraction.commit();
    }

    private int insertStackIntoHandler(PlayerEntity player, IItemHandler handler, ItemStack stack, int count, boolean simulate) {
        for (int i = 0; i < handler.getSlots(); i++) {
            stack = handler.insertItem(i, stack, simulate);
            if (stack.isEmpty())
                return count;
        }
        ItemStack fStack = stack;
        return getBoundIndex(player)
                .map(index -> index.insertItem(player, fStack, simulate))
                .orElseGet(() -> count - fStack.getCount());
    }

    public class HandlerBulkExtraction implements IBulkExtraction {
        private boolean isCommitted;
        private Multiset<IndexKey> pendingExtractTransactions;
        private PlayerEntity player;
        private Optional<IBulkExtraction> subTransaction;

        public HandlerBulkExtraction(PlayerEntity player) {
            this.pendingExtractTransactions = HashMultiset.create();
            this.subTransaction = getBoundIndex(player).map(index -> index.bulkTransaction(player));
            this.player = player;
            this.isCommitted = false;
        }

        @Override
        public void commit() {
            if (isCommitted) {
                BuildingGadgets.LOG.warn("Attempted to recommit {}. Ignoring!", this);
                return;
            }
            this.isCommitted = true;
            extractions.remove(this);
            if (pendingExtractTransactions.isEmpty()) {
                subTransaction.ifPresent(IBulkExtraction::commit);
                return;
            }
            getHandler(player).ifPresent(handler -> {
                for (Entry<IndexKey> entry : pendingExtractTransactions.entrySet()) {
                    int count = entry.getCount();
                    IndexCacheObject cache = getCache(player);
                    Iterator<Integer> it = cache.getSlotByItem().get(entry.getElement()).iterator();
                    while (it.hasNext()) {
                        int i = it.next();
                        count = handler.extractItem(i, count, false).getCount();
                        ItemStack inSlot = handler.getStackInSlot(i);

                        if (inSlot.isEmpty()) {
                            //only need to clear it out, if it's empty...
                            //the index has already been updated, when extract happened
                            cache.removeKeyFromSlot(i);
                            cache.removeItemCountFromSlot(i);
                            it.remove();
                        } else
                            cache.putItemCountFromSlot(i, inSlot.getCount());

                        if (count == 0)
                            break;
                    }
                    if (count != 0) { //buhoo, something went wrong with the cache - let's hope for the best
                        final int fCount = count;
                        subTransaction.ifPresent(index -> index.extractItem(entry.getElement(), fCount));
                    }
                }
            });
            subTransaction.ifPresent(IBulkExtraction::commit);
        }

        @Override
        public int extractItem(IndexKey key, int count) {
            if (isCommitted) {
                BuildingGadgets.LOG.warn("Attempted to extract Item From {} after it was already committed! Ignoring extract of {}*{}!", this, count, key);
                return 0;
            }
            int available = getCache(player).getIndices().remove(key, count);
            int remainingCount = count;
            if (available < count) {
                //subtract here, so that at the end we don't need to handle remainingCount possibly being negative
                //just keeps the invariant of it being >=0
                remainingCount -= available;

                pendingExtractTransactions.add(key, available);

                //try to fetch the rest from the link
                int fRemaining = remainingCount;
                remainingCount = subTransaction
                        .map(index -> index.extractItem(key, fRemaining))
                        .orElse(remainingCount);
            } else
                pendingExtractTransactions.add(key, count);
            return count - remainingCount;
        }
    }

    public static class IndexCacheObject {
        private final Multiset<IndexKey> indices;
        private final Int2IntMap itemCountBySlot;
        private final Int2ObjectMap<IndexKey> keyBySlot;
        private final Multimap<IndexKey, Integer> slotByItem;

        public IndexCacheObject() {
            this.indices = HashMultiset.create();
            this.slotByItem = ArrayListMultimap.create();
            this.itemCountBySlot = new Int2IntOpenHashMap();
            this.keyBySlot = new Int2ObjectOpenHashMap<>();
        }

        public Multiset<IndexKey> getIndices() {
            return indices;
        }

        public Multimap<IndexKey, Integer> getSlotByItem() {
            return slotByItem;
        }

        public Int2IntMap getItemCountBySlot() {
            return itemCountBySlot;
        }

        protected Int2ObjectMap<IndexKey> getKeyBySlot() {
            return keyBySlot;
        }

        public int removeItemCountFromSlot(int slot) {
            return getItemCountBySlot().remove(slot);
        }

        public void putItemCountFromSlot(int slot, int count) {
            getItemCountBySlot().put(slot, count);
        }

        public IndexKey removeKeyFromSlot(int slot) {
            return getKeyBySlot().remove(slot);
        }

        public void putKeyFromSlot(int slot, IndexKey key) {
            getKeyBySlot().put(slot, key);
        }

        public void addStackToCache(ItemStack stack, int slot) {
            IndexKey oldKey = removeKeyFromSlot(slot);

            if (oldKey != null)
                getIndices().remove(oldKey, removeItemCountFromSlot(slot));

            if (! stack.isEmpty()) {
                IndexKey key = IndexKey.ofStack(stack);
                getIndices().add(key, stack.getCount());
                getSlotByItem().put(key, slot);
                putKeyFromSlot(slot, key);
                putItemCountFromSlot(slot, stack.getCount());
            }
        }
    }
}
