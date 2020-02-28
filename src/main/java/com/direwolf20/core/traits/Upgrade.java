package com.direwolf20.core.traits;

import com.google.common.collect.ImmutableSet;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.function.UnaryOperator;

public abstract class Upgrade extends ForgeRegistryEntry<Upgrade> {
    public static final Upgrade BLANK = new Upgrade(ImmutableSet.of()) {
        @Override
        protected boolean isValidTier(int tier) {
            return tier>=0;
        }

        @Override
        public <T> UnaryOperator<T> getModificatorFor(Trait<T> characteristic, int tier) {
            return null;
        }
    };
    private final ImmutableSet<Trait<?>> modifcations;

    public Upgrade(ImmutableSet<Trait<?>> modifcations) {
        this.modifcations = modifcations;
    }

    public final ImmutableSet<Trait<?>> getAppliedModifications() {
        return modifcations;
    }

    protected abstract boolean isValidTier(int tier);


    public abstract <T> UnaryOperator<T> getModificatorFor(Trait<T> characteristic, int tier);
}
