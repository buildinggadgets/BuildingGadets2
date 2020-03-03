package com.direwolf20.core.capability.itemsync;

import com.google.common.collect.HashBiMap;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public final class CapabilityModificationSyncHelper implements ICapabilitySyncHelper {
    private CompoundNBT nbtForSync;
    private Map<String, Consumer<ListNBT>> objectKeyMap;

    public CapabilityModificationSyncHelper() {
        this.nbtForSync = null;
        objectKeyMap = HashBiMap.create();
    }

    @Override
    @Nonnull
    public Optional<CompoundNBT> getNBTForSync(boolean clear) {
        Optional<CompoundNBT> res = Optional.ofNullable(nbtForSync);

        if (clear)
            nbtForSync = null;

        return res;
    }

    @Override
    public void readNBTFromSync(CompoundNBT nbt) {
        for (String s : nbt.keySet()) {
            objectKeyMap.getOrDefault(s, n -> {
            }).accept((ListNBT) nbt.get(s));
        }
        if (nbtForSync != null) //we are on the client and just received syncs... This class is not intended for bidirectional syncing
            nbtForSync = null;
    }

    public void registerSyncConsumer(String key, Consumer<ListNBT> nbtConsumer) {
        objectKeyMap.put(key, nbtConsumer);
    }

    public void onValueModified(String key, INBT value) {
        if (nbtForSync == null)
            nbtForSync = new CompoundNBT();

        if (!nbtForSync.contains(key, NBT.TAG_LIST))
            nbtForSync.put(key, new ListNBT());

        ListNBT list = (ListNBT) nbtForSync.get(key);
        assert list != null;
        list.add(value);
    }
}
