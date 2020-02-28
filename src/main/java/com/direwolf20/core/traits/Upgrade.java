package com.direwolf20.core.traits;

import com.direwolf20.core.DireCore20;
import com.google.common.collect.ImmutableSet;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.function.UnaryOperator;

public abstract class Upgrade extends ForgeRegistryEntry<Upgrade> {
    public static final ResourceLocation UPGRADE_BLANK_RL = new ResourceLocation(DireCore20.MODID+":blank");

    private final ImmutableSet<Trait<?>> modifcations;

    public Upgrade(ImmutableSet<Trait<?>> modifcations) {
        this.modifcations = modifcations;
    }

    public final ImmutableSet<Trait<?>> getAppliedModifications() {
        return modifcations;
    }

    protected abstract boolean isValidTier(int tier);


    public abstract <T> UnaryOperator<T> getModificatorFor(Trait<T> trait, int tier);
}
