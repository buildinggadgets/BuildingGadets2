package com.direwolf20.core.capability;

import com.direwolf20.core.properties.IPropertyContainer;
import com.direwolf20.core.properties.MutableProperty;
import com.direwolf20.core.traits.ITraitContainer;
import com.direwolf20.core.traits.Trait;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.energy.IEnergyStorage;

public final class PropertyTraitBackedEnergyStorage implements IEnergyStorage {
    private final IPropertyContainer propertyContainer;
    private final ITraitContainer traitContainer;
    private final MutableProperty<Integer> energyProp;

    public PropertyTraitBackedEnergyStorage(IPropertyContainer propertyContainer, ITraitContainer traitContainer, MutableProperty<Integer> energyProp) {
        this.propertyContainer = propertyContainer;
        this.traitContainer = traitContainer;
        this.energyProp = energyProp;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        maxReceive = MathHelper.clamp(maxReceive, 0, getMaxReceive());
        int energy = getEnergyStored();
        int maxEnergy = getMaxEnergyStored();
        int newEnergy = Math.min(energy + maxReceive, maxEnergy);

        if (!simulate)
            propertyContainer.setProperty(energyProp, newEnergy);

        return newEnergy - energy;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        maxExtract = MathHelper.clamp(maxExtract, 0, getMaxExtract());
        int energy = getEnergyStored();
        int newEnergy = Math.max(energy - maxExtract, 0);

        if (!simulate)
            propertyContainer.setProperty(energyProp, newEnergy);

        return newEnergy - energy;
    }

    @Override
    public int getEnergyStored() {
        return propertyContainer.getProperty(energyProp).get();
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
}
