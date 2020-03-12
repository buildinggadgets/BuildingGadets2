package com.direwolf20.buildinggadgets.common.inventory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.lang.ref.WeakReference;
import java.util.*;

public class PlayerItemIndex implements IItemIndex, INBTSerializable<CompoundNBT> {
    private static final String KEY_BOUND_INV = "bound_inv";
    private final WeakReference<PlayerEntity> player;
    private InventoryLink boundInv;
    private final Multiset<IndexKey> indices;
    private final Multimap<IndexKey, Integer> slotByItem;
    private PlayerBulkExtraction transaction;

    public PlayerItemIndex(PlayerEntity player) {
        this.player = new WeakReference<>(Objects.requireNonNull(player, "A player Item Index always requires a player to start with!"));
        this.transaction = new PlayerBulkExtraction();
        this.boundInv = null;
        this.indices = HashMultiset.create();
        this.slotByItem = ArrayListMultimap.create();
    }

    public PlayerItemIndex(PlayerEntity player, InventoryLink boundInv) {
        this(player);
        this.boundInv = boundInv;
    }

    public PlayerItemIndex(PlayerEntity player, List<InventoryLink> boundInv) {
        this(player, boundInv.isEmpty() ? null : boundInv.get(boundInv.size() - 1));
    }

    private Optional<IItemIndex> getBoundIndex() {
        PlayerEntity thePlayer = player.get();
        return Optional.ofNullable(boundInv)
                .filter(b -> thePlayer != null)
                .flatMap(b -> b.getIndex(thePlayer.world));
    }

    private LazyOptional<IItemHandler> getPlayerHandler() {
        PlayerEntity thePlayer = player.get();
        return thePlayer != null ? thePlayer.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) : LazyOptional.empty();
    }

    @Override
    public IBulkExtraction bulkTransaction() {
        return transaction;
    }

    @Override
    public ItemExtractionCache createExtractionSimulation() {
        ItemExtractionCache cache = new ItemExtractionCache(HashMultiset.create(indices));
        return getBoundIndex()
                .map(index -> ItemExtractionCache.createMerged(index.createExtractionSimulation(), cache))
                .orElse(cache);
    }

    @Override
    public int insertItem(ItemStack stack, boolean simulate) {
        //code copied from below, to avoid unnnessecary key creation
        return getPlayerHandler()
                .map(handler -> insertStackIntoHandler(handler, stack, stack.getCount(), simulate))
                .orElse(0);
    }

    @Override
    public int insertItem(IndexKey key, int count, boolean simulate) {
        return getPlayerHandler()
                .map(handler -> insertStackIntoHandler(handler, key.createStack(count), count, simulate))
                .orElse(0);
    }

    @Override
    public boolean reIndex() {
        getBoundIndex().ifPresent(IItemIndex::reIndex);
        performIndex();
        return true;
    }

    @Override
    public boolean updateIndex() {
        getBoundIndex().ifPresent(IItemIndex::updateIndex);
        //player inventory is so small - always re-Index...
        performIndex();
        return true;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        if (boundInv != null)
            nbt.put(KEY_BOUND_INV, boundInv.serializeNBT());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        if (nbt.contains(KEY_BOUND_INV, NBT.TAG_COMPOUND))
            boundInv = InventoryLink.deserialize(nbt.getCompound(KEY_BOUND_INV));
    }

    private int insertStackIntoHandler(IItemHandler handler, ItemStack stack, int count, boolean simulate) {
        for (int i = 0; i < handler.getSlots(); i++) {
            stack = handler.insertItem(i, stack, simulate);
            if (stack.isEmpty())
                return count;
        }
        ItemStack fStack = stack;
        return getBoundIndex()
                .map(index -> index.insertItem(fStack, simulate))
                .orElseGet(() -> count - fStack.getCount());
    }

    private void performIndex() {
        getPlayerHandler().ifPresent(handler -> {
            transaction.commit();
            indices.clear();
            slotByItem.clear();
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stack = handler.getStackInSlot(i);
                if (! stack.isEmpty()) {
                    IndexKey key = IndexKey.ofStack(stack);
                    indices.add(key, stack.getCount());
                    slotByItem.put(key, i);
                }
            }
        });
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

    private final class PlayerBulkExtraction implements IBulkExtraction {
        private Multiset<IndexKey> pendingExtractTransactions;

        public PlayerBulkExtraction() {
            this.pendingExtractTransactions = null;
        }

        @Override
        public void commit() {
            if (pendingExtractTransactions == null || pendingExtractTransactions.isEmpty())
                return;
            getPlayerHandler().ifPresent(handler -> {
                Optional<IBulkExtraction> link = getBoundIndex().map(IItemIndex::bulkTransaction);
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
                    if (count != 0) { //buhoo, something went wrong with the cache - let's hope for the best
                        final int fCount = count;
                        link.ifPresent(index -> index.extractItem(entry.getElement(), fCount));
                    }
                }
                link.ifPresent(IBulkExtraction::commit);
                pendingExtractTransactions = null;
            });
        }

        @Override
        public int extractItem(IndexKey key, int count) {
            if (pendingExtractTransactions == null)
                pendingExtractTransactions = HashMultiset.create();
            int available = indices.remove(key, count);
            int remainingCount = count;
            if (available < count) {
                //subtract here, so that at the end we don't need to handle remainingCount possibly being negative
                //just keeps the invariant of it being >=0
                remainingCount -= available;

                pendingExtractTransactions.add(key, available);

                //try to fetch the rest from the link
                int fRemaining = remainingCount;
                remainingCount = getBoundIndex()
                        .map(IItemIndex::bulkTransaction)
                        .map(index -> index.extractItem(key, fRemaining))
                        .orElse(remainingCount);
            } else
                pendingExtractTransactions.add(key, count);
            return count - remainingCount;
        }
    }
}
