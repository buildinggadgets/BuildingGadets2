package com.direwolf20.buildinggadgets.core.traits;

import net.minecraft.nbt.CompoundNBT;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class SimpleTraitContainer implements ITraitContainer {
    private Map<Trait<?>, TraitValue<?>> traits;
    //for ease of lookup
    private Set<Upgrade> installedUpgrades;
    private Set<TieredUpgrade> installedTiers;

    private SimpleTraitContainer(Map<Trait<?>, TraitValue<?>> traits, Set<TieredUpgrade> installedTiers) {
        this.traits = traits;
        this.installedTiers = installedTiers;
        this.installedUpgrades = installedTiers.stream()
                .map(TieredUpgrade::getUpgrade)
                .collect(Collectors.toSet());
    }

    @Override
    public <T> Optional<T> getCharacteristic(Trait<T> characteristic) {
        return Optional.ofNullable(traits.get(characteristic))
                .map(TraitValue::getValue)
                .map(characteristic::cast);
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
    public boolean installUpgrade(TieredUpgrade upgrade) {
        if (installedUpgrades.contains(upgrade.getUpgrade()) || installedTiers.contains(upgrade))
            return false;
        if (! traits.keySet().containsAll(upgrade.getAppliedModifications()))
            return false;
        for (Trait<?> characteristic : upgrade.getAppliedModifications()) {
            if (!applyModificator(characteristic, traits.get(characteristic), upgrade))
                throw new RuntimeException("Found inconsistency in registered upgrades and known upgrades by charactersitics. This is a bug!");
        }
        return installedTiers.add(upgrade) && installedUpgrades.add(upgrade.getUpgrade());
    }

    @SuppressWarnings("unchecked")
    private <T> boolean applyModificator(Trait<T> characteristic, TraitValue<?> value, TieredUpgrade upgrade) {
        return ((TraitValue<T>)value).addModificator(upgrade, upgrade.getModificatorFor(characteristic));
    }

    @Override
    public boolean removeUpgrade(TieredUpgrade upgrade) {
        if (!installedTiers.contains(upgrade) || !installedUpgrades.contains(upgrade.getUpgrade()))
            return false;
        for (Trait<?> characteristic : upgrade.getAppliedModifications()) {
            if (traits.get(characteristic).removeModificator(upgrade))
                throw new RuntimeException("Found inconsistency in registered upgrades and known upgrades by charactersitics. This is a bug!");
        }
        return installedTiers.remove(upgrade) && installedUpgrades.remove(upgrade.getUpgrade());
    }

    @Override
    public CompoundNBT serializeNBT() {
        return null;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {

    }
}
