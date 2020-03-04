package com.direwolf20.core.capability;

import com.direwolf20.core.traits.ITraitContainer;
import com.direwolf20.core.traits.Trait;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.energy.IEnergyStorage;

public final class TraitEnergyStorage implements IEnergyStorage, INBTSerializable<CompoundNBT> {
    public static final String KEY_ENERGY = "energy";
    private final ITraitContainer traitContainer;
    private int energyStored;
    private final Runnable onChangeCallback;

    public TraitEnergyStorage(ITraitContainer traitContainer, Runnable onChangeCallback) {
        this.traitContainer = traitContainer;
        this.energyStored = 0;
        this.onChangeCallback = onChangeCallback;
    }

    public TraitEnergyStorage(ITraitContainer traitContainer) {
        this(traitContainer, () -> {});
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        maxReceive = MathHelper.clamp(maxReceive, 0, getMaxReceive());
        int energy = getEnergyStored();
        int maxEnergy = getMaxEnergyStored();
        int newEnergy = Math.min(energy + maxReceive, maxEnergy);

        if (!simulate) {
            energyStored = newEnergy;
            onChangeCallback.run();
        }

        return newEnergy - energy;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        maxExtract = MathHelper.clamp(maxExtract, 0, getMaxExtract());
        int energy = getEnergyStored();
        int newEnergy = Math.max(energy - maxExtract, 0);

        if (!simulate) {
            energyStored = newEnergy;
            onChangeCallback.run();
        }

        return newEnergy - energy;
    }

    @Override
    public int getEnergyStored() {
        return energyStored;
    }

    @Override
    public int getMaxEnergyStored() {
        return traitContainer.getTrait(Trait.MAX_ENERGY).get();
    }

    public int getMaxExtract() {
        return traitContainer.getTrait(Trait.MAX_EXTRACT).get();
    }

    public int getMaxReceive() {
        return traitContainer.getTrait(Trait.MAX_RECEIVE).get();
    }

    @Override
    public boolean canExtract() {
        return getMaxExtract() > 0;
    }

    @Override
    public boolean canReceive() {
        return getMaxReceive() > 0;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt(KEY_ENERGY, energyStored);

        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        if (nbt.contains(KEY_ENERGY, NBT.TAG_INT))
            energyStored = nbt.getInt(KEY_ENERGY);
    }
}
