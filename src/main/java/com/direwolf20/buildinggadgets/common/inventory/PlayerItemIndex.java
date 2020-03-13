package com.direwolf20.buildinggadgets.common.inventory;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.inventory.link.InventoryLink;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class PlayerItemIndex extends AbstractHandlerItemIndex implements IItemIndex, INBTSerializable<CompoundNBT> {
    private static final String KEY_BOUND_INV = "bound_inv";
    private InventoryLink boundInv;
    private Cache<PlayerEntity, IndexCacheObject> cache;

    public PlayerItemIndex() {
        this.boundInv = null;
        this.cache = CacheBuilder.newBuilder()
                .expireAfterAccess(1, TimeUnit.MINUTES)
                //this represents the amount of Threads allowed to access this concurrently - only ever accessed on Server or client
                .concurrencyLevel(1)
                .build();
    }

    public PlayerItemIndex(InventoryLink boundInv) {
        this.boundInv = boundInv;
    }

    @Override
    public boolean updateIndex(PlayerEntity player, int maxIndex) {
        reIndex(player); //player inventory is really small
        if (maxIndex > player.inventory.getSizeInventory())
            getBoundIndex(player).ifPresent(index -> index.updateIndex(player, maxIndex - player.inventory.getSizeInventory()));
        return true;
    }

    @Override
    protected Optional<IItemHandler> getHandler(PlayerEntity player) {
        LazyOptional<IItemHandler> opt = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
        return opt.isPresent() ? Optional.of(opt.orElseThrow(RuntimeException::new)) : Optional.empty();
    }

    @Override
    protected IndexCacheObject getCache(PlayerEntity player) {
        try {
            return cache.get(player, IndexCacheObject::new);
        } catch (ExecutionException e) {
            BuildingGadgets.LOG.error("Caught exception during cache Access! This however should never throw an exception - Please report to developers!", e);
            return new IndexCacheObject();
        }
    }

    @Override
    protected Optional<IItemIndex> getBoundIndex(PlayerEntity player) {
        return Optional.ofNullable(boundInv)
                .filter(b -> player != null)
                .flatMap(b -> b.getIndex(player.world));
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
}
