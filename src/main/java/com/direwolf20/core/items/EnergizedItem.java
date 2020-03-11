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
import com.direwolf20.core.traits.TraitContainer.Builder;
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
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Abstract Base class for an FE using Gadget. The {@link IEnergyStorage} implementation used by this Gadget is guaranteed to
 * be an instance of {@link INBTSerializable<INBT>}, which will be synced from Server to Client automatically. For the {@link TraitEnergyStorage}
 * to be backed by appropriate default values, you can pass the default supplier for max energy to the constructor.
 * This supplier is meant to return the corresponding config value or some function of it.
 * <p>
 * Notice that this class also adds both the {@link IPropertyContainer} and {@link ITraitContainer} capabilities and enables
 * subclasses to add their own {@link Trait Traits} and {@link com.direwolf20.core.properties.Property Properties} by
 * overriding {@link #onAttachTraits(Builder)} and {@link #onAttachProperties(PropertyContainer.Builder)}. These will be synced
 * alongside the {@link IEnergyStorage} capability.
 */
public abstract class EnergizedItem extends Item {
    private static final String KEY_ENERGY = "energy";
    private static final String KEY_PROPERTIES = "properties";
    private static final String KEY_TRAITS = "traits";
    private final Supplier<Integer> maxEnergyDefault;

    public EnergizedItem(Properties properties, Supplier<Integer> maxEnergyDefault) {
        super(properties);
        this.maxEnergyDefault = Objects.requireNonNull(maxEnergyDefault);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Notice that this class expects every {@link ICapabilityProvider} returned by this Method to implement
     * {@link INBTSerializable<CompoundNBT>}. Furthermore this Method is responsible for attaching Properties
     * and Traits.
     */
    @Override
    @SuppressWarnings("unchecked")
    public final ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        ITraitContainer traitContainer = onAttachTraits(TraitContainer.builder()).build();
        IPropertyContainer propertyContainer = onAttachProperties(PropertyContainer.builder()).build();
        ICapabilityProvider provider = createCapabilities(stack, traitContainer, propertyContainer);

        if (nbt != null) //The contract states that it will be INBTSerializable<CompoundNBT> - safe cast
            ((INBTSerializable<CompoundNBT>) provider).deserializeNBT(nbt);

        return provider;
    }

    /**
     * Notice that {@link #readShareTag(ItemStack, CompoundNBT)} and {@link #getShareTag(ItemStack)} assumes that the
     * {@link IEnergyStorage} implementation returned by the cap provider, returned from this Method is an instance of
     * {@link INBTSerializable<INBT>} - subclasses must ensure that this remains true, or override the share tag Methods!
     *
     * @param stack             The {@link ItemStack}
     * @param traitContainer    The {@link ITraitContainer} which was constructed for this {@link Item}
     * @param propertyContainer The {@link IPropertyContainer} which was constructed for this {@link Item}
     * @return An {@link ICapabilityProvider} which will be returned from {@link #initCapabilities(ItemStack, CompoundNBT)}.
     * It is required that this instance implements {@link INBTSerializable<CompoundNBT>}, as deserialize may be called
     * immediately after this Method returns!
     */
    protected ICapabilityProvider createCapabilities(ItemStack stack, ITraitContainer traitContainer, IPropertyContainer propertyContainer) {
        return new EnergyCapabilityProvider(stack, traitContainer, propertyContainer);
    }

    /**
     * Override this Method to add your own Properties to the resulting Gadget.
     *
     * @param builder The {@link PropertyContainer.Builder} used for adding Properties
     * @return the passed in builder instance, to allow for Method chaining
     */
    protected PropertyContainer.Builder onAttachProperties(PropertyContainer.Builder builder) {
        return builder;
    }

    /**
     * Override this Method to add your own Traits to the resulting Gadget. By default {@link Trait#MAX_ENERGY} will be added
     * using the default supplier passed to the constructor, whilst the {@link Trait#MAX_EXTRACT}, {@link Trait#MAX_RECEIVE} will
     * be set to a supplier returning {@link Integer#MAX_VALUE}, as per @Direwolf20's request.
     * If you want to use different Traits for your {@link IEnergyStorage} (for example because you want to use different upgrades),
     * just don't call super and adapt the {@link EnergyCapabilityProvider} accordingly.
     *
     * @param builder The {@link PropertyContainer.Builder} used for adding Properties
     * @return the passed in builder instance, to allow for Method chaining
     */
    protected TraitContainer.Builder onAttachTraits(TraitContainer.Builder builder) {
        return builder
                .putTrait(Trait.MAX_ENERGY, maxEnergyDefault)
                .putTrait(Trait.MAX_EXTRACT, () -> Integer.MAX_VALUE)
                .putTrait(Trait.MAX_RECEIVE, () -> Integer.MAX_VALUE);
    }

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

    /**
     * A subclass of {@link PropertyTraitCapabilityProvider} adding the {@link IEnergyStorage} capability.
     */
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
