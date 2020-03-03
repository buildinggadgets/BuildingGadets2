package com.direwolf20.core.items;

import com.direwolf20.core.capability.PropertyTraitBackedEnergyStorage;
import com.direwolf20.core.capability.PropertyTraitCapabilityProvider;
import com.direwolf20.core.capability.SyncHelperCapability;
import com.direwolf20.core.capability.itemsync.ICapabilitySyncHelper;
import com.direwolf20.core.properties.IPropertyContainer;
import com.direwolf20.core.properties.MutableProperty;
import com.direwolf20.core.properties.Property;
import com.direwolf20.core.properties.PropertyContainer;
import com.direwolf20.core.traits.ITraitContainer;
import com.direwolf20.core.traits.Trait;
import com.direwolf20.core.traits.TraitContainer;
import com.google.common.collect.ImmutableSet;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
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
import java.util.Optional;
import java.util.Set;

public abstract class EnergizedItem extends Item {
    private static final String KEY_SYNC = "cap_sync";
    protected static final MutableProperty<Integer> ENERGY = Property.intBuilder().buildMutable("energy");
    public EnergizedItem(Properties properties) {
        super(properties);
    }

    @Override
    @SuppressWarnings("unchecked")
    public final ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        ImmutableSet.Builder<MutableProperty<?>> mutableProps = ImmutableSet.builder();
        ITraitContainer traitContainer = onAttachTraits(TraitContainer.builder()).build();
        IPropertyContainer propertyContainer = onAttachProperties(PropertyContainer.builder(), mutableProps).build();
        ICapabilityProvider provider = createCapabilities(stack, traitContainer, propertyContainer, mutableProps.build());
        if (nbt!=null)
            ((INBTSerializable<CompoundNBT>) provider).deserializeNBT(nbt);

        return provider;
    }

    protected ICapabilityProvider createCapabilities(ItemStack stack, ITraitContainer traitContainer, IPropertyContainer propertyContainer, ImmutableSet<MutableProperty<?>> mutableProperties) {
        return new EnergyCapabilityProvider(traitContainer, propertyContainer, mutableProperties);
    }

    protected PropertyContainer.Builder onAttachProperties(PropertyContainer.Builder builder, ImmutableSet.Builder<MutableProperty<?>> mutableProps) {
        mutableProps.add(ENERGY);
        return builder.putProperty(ENERGY, 0);
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
        Optional<CompoundNBT> opt = stack.getCapability(SyncHelperCapability.SYNC_HELPER_CAPABILITY)
                .map(ICapabilitySyncHelper::getNBTForSync)
                .orElse(Optional.empty());
        if (opt.isPresent()) {
            //make sure not to modify the actual tag
            CompoundNBT nbt = stack.getOrCreateTag().copy();
            nbt.put(KEY_SYNC, opt.get());
            return nbt;
        }
        return stack.getTag();
    }

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundNBT nbt) {
        if (nbt != null && nbt.contains(KEY_SYNC, NBT.TAG_COMPOUND)) {
            stack.getCapability(SyncHelperCapability.SYNC_HELPER_CAPABILITY).ifPresent(syncHelper -> {
                CompoundNBT sync = nbt.getCompound(KEY_SYNC);
                nbt.remove(KEY_SYNC);
                syncHelper.readNBTFromSync(sync);
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
        private final IEnergyStorage energyStorage;
        private final LazyOptional<IEnergyStorage> energyStorageOpt;
        public EnergyCapabilityProvider(ITraitContainer traitContainer, IPropertyContainer propertyContainer, Set<MutableProperty<?>> mutableProperties) {
            super(traitContainer, propertyContainer, mutableProperties);
            energyStorage = new PropertyTraitBackedEnergyStorage(propertyContainer, traitContainer, ENERGY);
            energyStorageOpt = LazyOptional.of(this::getEnergyStorage);
        }

        @Nonnull
        public IEnergyStorage getEnergyStorage() {
            return energyStorage;
        }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            if (cap == CapabilityEnergy.ENERGY)
                return energyStorageOpt.cast();

            return super.getCapability(cap, side);
        }
    }
}
