package com.direwolf20.buildinggadgets.core.characteristics;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.Objects;
import java.util.function.UnaryOperator;

public abstract class Upgrade extends ForgeRegistryEntry<Upgrade> {
    private final ImmutableSet<Characteristic<?>> modifcations;
    private final Range<Integer> validTiers;

    public Upgrade(ImmutableSet<Characteristic<?>> modifcations, Range<Integer> validTiers) {
        this.modifcations = modifcations;
        this.validTiers = Objects.requireNonNull(validTiers);
    }

    public final ImmutableSet<Characteristic<?>> getAppliedModifications() {
        return modifcations;
    }

    boolean isValidTier(int tier) {
        return validTiers.contains(tier);
    }

    public abstract <T> UnaryOperator<T> getModificatorFor(Characteristic<T> characteristic, int tier);
}
