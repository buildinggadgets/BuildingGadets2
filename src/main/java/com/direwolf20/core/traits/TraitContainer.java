package com.direwolf20.core.traits;

import com.direwolf20.core.traits.upgrade.TieredUpgrade;
import com.direwolf20.core.traits.upgrade.Upgrade;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants.NBT;

import java.util.*;
import java.util.function.Supplier;

/**
 * The default implementation for an {@link ITraitContainer} which is created via it's own {@link Builder}, to add
 * the {@link Trait Trait's} which are available from this container.
 */
public final class TraitContainer implements ITraitContainer {
    private static final String KEY_INSTALLED_UPGRADES = "installed_upgrades";
    private Map<Trait<?>, TraitValue<?>> traits;
    //for ease of lookup
    private Set<Upgrade> installedUpgrades;
    private Set<TieredUpgrade> installedTiers;
    private TraitContainer(Map<Trait<?>, TraitValue<?>> traits) {
        this.traits = traits;
        this.installedTiers = new LinkedHashSet<>();
        this.installedUpgrades = new HashSet<>();
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public <T> Optional<T> getTrait(Trait<T> trait) {
        return Optional.ofNullable(traits.get(trait))
                .map(TraitValue::getValue)
                .map(trait::cast);
    }

    @Override
    public Set<TieredUpgrade> listTiers() {
        return Collections.unmodifiableSet(installedTiers);
    }

    @Override
    public Set<Upgrade> listUpgrades() {
        return Collections.unmodifiableSet(installedUpgrades);
    }


    @Override
    public Set<Trait<?>> listTraits() {
        return Collections.unmodifiableSet(traits.keySet());
    }

    @Override
    public boolean installUpgrade(TieredUpgrade upgrade) {
        if (installedUpgrades.contains(upgrade.getUpgrade()) || installedTiers.contains(upgrade))
            return false;
        if (!traits.keySet().containsAll(upgrade.getAppliedModifications()))
            return false;
        for (Trait<?> characteristic : upgrade.getAppliedModifications()) {
            if (!applyModificator(characteristic, traits.get(characteristic), upgrade))
                throw new RuntimeException("Found inconsistency in registered upgrades and known upgrades by traits. This is a bug!");
        }
        return installedTiers.add(upgrade) && installedUpgrades.add(upgrade.getUpgrade());
    }

    @SuppressWarnings("unchecked")
    private <T> boolean applyModificator(Trait<T> characteristic, TraitValue<?> value, TieredUpgrade upgrade) {
        return ((TraitValue<T>) value).addModificator(upgrade, upgrade.getModificatorFor(characteristic));
    }

    @Override
    public boolean removeUpgrade(TieredUpgrade upgrade) {
        if (!installedTiers.contains(upgrade) || !installedUpgrades.contains(upgrade.getUpgrade()))
            return false;
        for (Trait<?> characteristic : upgrade.getAppliedModifications()) {
            if (traits.get(characteristic).removeModificator(upgrade))
                throw new RuntimeException("Found inconsistency in registered upgrades and known upgrades by traits. This is a bug!");
        }
        return installedTiers.remove(upgrade) && installedUpgrades.remove(upgrade.getUpgrade());
    }

    @Override
    public CompoundNBT serializeNBT(boolean persistent) {
        CompoundNBT compound = new CompoundNBT();
        ListNBT installedUpgrades = new ListNBT();
        for (TieredUpgrade upgrade : installedTiers)
            installedUpgrades.add(upgrade.serializeNBT(persistent));
        compound.put(KEY_INSTALLED_UPGRADES, installedUpgrades);
        return compound;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        if (! nbt.contains(KEY_INSTALLED_UPGRADES, NBT.TAG_LIST))
            return;
        ListNBT list = (ListNBT) nbt.get(KEY_INSTALLED_UPGRADES);
        assert list != null;
        if (! installedTiers.isEmpty()) { //shortcut the common case of no upgrade being installed
            installedUpgrades.clear();
            installedTiers.clear();
            for (TraitValue<?> val : traits.values())
                val.clearModificators();
        }
        for (INBT serializedTier : list) {
            TieredUpgrade upgrade = TieredUpgrade.deserialize((CompoundNBT) serializedTier);
            installUpgrade(upgrade);
        }
    }

    /**
     * A very minimalistic builder which allows specifying of {@link Trait Traits} for the {@link TraitContainer}.
     * Notice how it defines the backing map for the container as an {@link IdentityHashMap}...
     */
    public static final class Builder {
        private final Map<Trait<?>, TraitValue<?>> traits;

        private Builder() {
            //Traits don't override hashcode or equals... IdentityHashMap for the win!
            //We don't care about the slightly larger memory footprint compared to ImmutableMap
            // - the performance is more important here
            this.traits = new IdentityHashMap<>();
        }

        /**
         * Add/Replace a trait in this builder.
         *
         * @param trait           The {@link Trait} to add or replace
         * @param defaultSupplier The default value supplier for the trait
         * @param <T>             The type of the trait and it's corresponding values
         * @return The builder instance
         * @throws NullPointerException if trait or defaultSupplier are null
         */
        public <T> Builder putTrait(Trait<T> trait, Supplier<T> defaultSupplier) {
            this.traits.put(Objects.requireNonNull(trait), new TraitValue<>(Objects.requireNonNull(defaultSupplier)));
            return this;
        }

        public TraitContainer build() {
            return new TraitContainer(traits);
        }
    }
}
