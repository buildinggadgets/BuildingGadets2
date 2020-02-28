package com.direwolf20.core.traits;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants.NBT;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class SimpleTraitContainer implements ITraitContainer {
    private static final String KEY_INSTALLED_UPGRADES = "installed_upgrades";
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
        CompoundNBT compound = new CompoundNBT();
        ListNBT installedUpgrades = new ListNBT();
        for (TieredUpgrade upgrade:installedTiers)
            installedUpgrades.add(upgrade.serializeNBT());
        compound.put(KEY_INSTALLED_UPGRADES, installedUpgrades);
        return compound;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        if (!nbt.contains(KEY_INSTALLED_UPGRADES, NBT.TAG_LIST))
            return;
        ListNBT list = (ListNBT) nbt.get(KEY_INSTALLED_UPGRADES);
        assert list!=null;
        installedUpgrades.clear();
        installedTiers.clear();
        for (TraitValue<?> val:traits.values())
            val.clearModificators();
        for (INBT serializedTier: list) {
            TieredUpgrade upgrade = TieredUpgrade.deserialize((CompoundNBT) serializedTier);
            installUpgrade(upgrade);
        }
    }
}
