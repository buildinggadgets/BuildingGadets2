package com.direwolf20.core.items;

import com.direwolf20.core.capability.PropertyContainerCapability;
import com.direwolf20.core.capability.PropertyTraitCapabilityProvider;
import com.direwolf20.core.capability.TraitContainerCapability;
import com.direwolf20.core.capability.TraitEnergyStorage;
import com.direwolf20.core.properties.IPropertyContainer;
import com.direwolf20.core.properties.PropertyContainer;
import com.direwolf20.core.traits.ITraitContainer;
import com.direwolf20.core.traits.Trait;
import com.direwolf20.core.traits.TraitContainer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class EnergizedGadget extends Item {
    private static final String KEY_ENERGY = "energy";
    private static final String KEY_PROPERTIES = "properties";
    private static final String KEY_TRAITS = "traits";

    public EnergizedGadget(Properties properties) {
        super(properties);
    }

    @Override
    @SuppressWarnings("unchecked")
    public final ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        ITraitContainer traitContainer = onAttachTraits(TraitContainer.builder()).build();
        IPropertyContainer propertyContainer = onAttachProperties(PropertyContainer.builder()).build();
        ICapabilityProvider provider = createCapabilities(stack, traitContainer, propertyContainer);

        if (nbt != null)
            ((INBTSerializable<CompoundNBT>) provider).deserializeNBT(nbt);

        return provider;
    }

    protected ICapabilityProvider createCapabilities(ItemStack stack, ITraitContainer traitContainer, IPropertyContainer propertyContainer) {
        return new EnergyCapabilityProvider(stack, traitContainer, propertyContainer);
    }

    protected PropertyContainer.Builder onAttachProperties(PropertyContainer.Builder builder) {
        return builder;
    }

    protected TraitContainer.Builder onAttachTraits(TraitContainer.Builder builder) {
        return builder
                .putTrait(Trait.MAX_ENERGY, this::getMaxEnergy)
                .putTrait(Trait.MAX_EXTRACT, this::getMaxExtract)
                .putTrait(Trait.MAX_RECEIVE, this::getMaxReceive);
    }

    protected abstract int getMaxEnergy();

    protected abstract int getMaxReceive();

    protected abstract int getMaxExtract();

    @Nullable
    @Override
    public CompoundNBT getShareTag(ItemStack stack) {
        CompoundNBT nbt = stack.getOrCreateTag().copy();

        stack.getCapability(PropertyContainerCapability.PROPERTY_CONTAINER_CAPABILITY)
                .ifPresent(container -> nbt.put(KEY_PROPERTIES, container.serializeNBT()));

        stack.getCapability(TraitContainerCapability.TRAIT_CONTAINER_CAPABILITY)
                .ifPresent(container -> nbt.put(KEY_TRAITS, container.serializeNBT(false)));

        stack.getCapability(CapabilityEnergy.ENERGY)
                .ifPresent(energyStorage -> {
                    @SuppressWarnings("unchecked") //we know the implementation class
                            INBTSerializable<INBT> serializable = (INBTSerializable<INBT>) energyStorage;
                    nbt.put(KEY_ENERGY, serializable.serializeNBT());
                });

        return nbt;
    }

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundNBT nbt) {
        if (nbt != null) {
            if (nbt.contains(KEY_PROPERTIES, NBT.TAG_COMPOUND))
                stack.getCapability(PropertyContainerCapability.PROPERTY_CONTAINER_CAPABILITY)
                        .ifPresent(container ->  {
                            container.deserializeNBT(nbt.getCompound(KEY_PROPERTIES));
                            nbt.remove(KEY_PROPERTIES);
                        });

            if (nbt.contains(KEY_TRAITS, NBT.TAG_COMPOUND))
                stack.getCapability(TraitContainerCapability.TRAIT_CONTAINER_CAPABILITY)
                        .ifPresent(container ->  {
                            container.deserializeNBT(nbt.getCompound(KEY_TRAITS));
                            nbt.remove(KEY_TRAITS);
                        });

            if (nbt.contains(KEY_ENERGY))
                stack.getCapability(CapabilityEnergy.ENERGY)
                        .ifPresent(energyStorage -> {
                            @SuppressWarnings("unchecked") //we know the implementation class
                                    INBTSerializable<INBT> serializable = (INBTSerializable<INBT>) energyStorage;
                            serializable.deserializeNBT(nbt.get(KEY_ENERGY));
                            nbt.remove(KEY_ENERGY);
                        });
        }
        stack.setTag(nbt);
    }

    @Override
    public boolean isDamageable() {
        return false;
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        IEnergyStorage storage = stack.getCapability(CapabilityEnergy.ENERGY).orElseThrow(RuntimeException::new);
        return 1 - (((double) storage.getEnergyStored()) / ((double) storage.getMaxEnergyStored()));
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        return 255 << 16;//red for energy
    }

    protected static class EnergyCapabilityProvider extends PropertyTraitCapabilityProvider {
        private final TraitEnergyStorage energyStorage;
        private final LazyOptional<IEnergyStorage> energyStorageOpt;

        public EnergyCapabilityProvider(ItemStack stack, ITraitContainer traitContainer, IPropertyContainer propertyContainer) {
            super(stack, traitContainer, propertyContainer);
            energyStorage = TraitEnergyStorage.createWithDefaultTraits(traitContainer, this::onValueModified);
            energyStorageOpt = LazyOptional.of(this::getEnergyStorage);
        }

        @Nonnull
        public TraitEnergyStorage getEnergyStorage() {
            return energyStorage;
        }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            if (cap == CapabilityEnergy.ENERGY)
                return energyStorageOpt.cast();

            return super.getCapability(cap, side);
        }

        @Override
        public CompoundNBT serializeNBT() {
            CompoundNBT nbt = super.serializeNBT();
            nbt.put(KEY_ENERGY, getEnergyStorage().serializeNBT());

            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            super.deserializeNBT(nbt);

            if (nbt.contains(KEY_ENERGY))
                getEnergyStorage().deserializeNBT(nbt.get(KEY_ENERGY));
        }
    }
}
