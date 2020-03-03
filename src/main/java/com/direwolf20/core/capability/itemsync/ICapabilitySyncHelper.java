package com.direwolf20.core.capability.itemsync;

import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nonnull;
import java.util.Optional;

public interface ICapabilitySyncHelper {
    @Nonnull
    default Optional<CompoundNBT> getNBTForSync() {
        return getNBTForSync(true);
    }

    @Nonnull
    Optional<CompoundNBT> getNBTForSync(boolean clear);

    void readNBTFromSync(CompoundNBT nbt);
}
