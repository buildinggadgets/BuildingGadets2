package com.direwolf20.core.capability;

import com.direwolf20.core.DireCore20;
import com.direwolf20.core.capability.itemsync.CapabilityModificationSyncHelper;
import com.direwolf20.core.capability.itemsync.ICapabilitySyncHelper;
import com.direwolf20.core.properties.IPropertyContainer;
import com.direwolf20.core.properties.MutableProperty;
import com.direwolf20.core.properties.Property;
import com.direwolf20.core.traits.ITraitContainer;
import com.direwolf20.core.traits.Trait;
import com.direwolf20.core.traits.upgrade.TieredUpgrade;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PropertyTraitCapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundNBT> {
    public PropertyTraitCapabilityProvider(ITraitContainer traitContainer, IPropertyContainer propertyContainer, Set<MutableProperty<?>> mutableProperties) {
        this.syncHelper = new CapabilityModificationSyncHelper();
        this.syncHelperOpt = LazyOptional.of(this::getSyncHelper);
        this.traitContainer = new ModificationSyncTraitContainer(traitContainer);
        this.traitContainerOpt = LazyOptional.of(this::getTraitContainer);
        this.propertyContainer = new ModificationSyncPropertyContainer(propertyContainer, mutableProperties);
        this.propertyContainerOpt = LazyOptional.of(this::getPropertyContainer);
        syncHelper.registerSyncConsumer(KEY_TRAITS, this.traitContainer);
        syncHelper.registerSyncConsumer(KEY_PROPERTIES, this.propertyContainer);
    }
    private static final String KEY_TRAITS = "traits";
    private static final String KEY_PROPERTIES = "properties";

    private final ModificationSyncTraitContainer traitContainer;
    private final ModificationSyncPropertyContainer propertyContainer;
    private final CapabilityModificationSyncHelper syncHelper;

    private final LazyOptional<ITraitContainer> traitContainerOpt;
    private final LazyOptional<IPropertyContainer> propertyContainerOpt;
    private final LazyOptional<ICapabilitySyncHelper> syncHelperOpt;

    @Nonnull
    public ITraitContainer getTraitContainer() {
        return traitContainer;
    }

    @Nonnull
    public IPropertyContainer getPropertyContainer() {
        return propertyContainer;
    }

    @Nonnull
    protected CapabilityModificationSyncHelper getSyncHelper() {
        return syncHelper;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == SyncHelperCapability.SYNC_HELPER_CAPABILITY)
            return syncHelperOpt.cast();

        if (cap == PropertyContainerCapability.PROPERTY_CONTAINER_CAPABILITY)
            return propertyContainerOpt.cast();

        if (cap == TraitContainerCapability.TRAIT_CONTAINER_CAPABILITY)
            return traitContainerOpt.cast();

        return LazyOptional.empty();
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();

        nbt.put(KEY_TRAITS, traitContainer.serializeNBT());
        nbt.put(KEY_PROPERTIES, propertyContainer.serializeNBT());

        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        if (nbt.contains(KEY_TRAITS, NBT.TAG_COMPOUND))
            traitContainer.deserializeNBT(nbt.getCompound(KEY_TRAITS));

        if (nbt.contains(KEY_PROPERTIES, NBT.TAG_COMPOUND))
            propertyContainer.deserializeNBT(nbt.getCompound(KEY_PROPERTIES));
    }

    private final class ModificationSyncTraitContainer implements ITraitContainer, Consumer<ListNBT> {
        private static final String KEY_IS_INSTALL = "is_install";
        private static final String KEY_UPGRADE = "upgrade";
        private final ITraitContainer delegate;

        public ModificationSyncTraitContainer(ITraitContainer delegate) {
            this.delegate = delegate;
        }

        @Override
        public <T> Optional<T> getTrait(Trait<T> trait) {
            return delegate.getTrait(trait);
        }

        @Override
        public Set<TieredUpgrade> listTiers() {
            return delegate.listTiers();
        }

        @Override
        public Set<Trait<?>> listTraits() {
            return delegate.listTraits();
        }

        @Override
        public boolean installUpgrade(TieredUpgrade upgrade) {
            if (delegate.installUpgrade(upgrade)) {
                CompoundNBT nbt = new CompoundNBT();
                nbt.put(KEY_UPGRADE, upgrade.serializeNBT());
                nbt.putBoolean(KEY_IS_INSTALL, true);
                getSyncHelper().onValueModified(KEY_TRAITS, nbt);
                return true;
            }
            return false;
        }

        @Override
        public boolean removeUpgrade(TieredUpgrade upgrade) {
            if (delegate.removeUpgrade(upgrade)) {
                CompoundNBT nbt = new CompoundNBT();
                nbt.put(KEY_UPGRADE, upgrade.serializeNBT());
                nbt.putBoolean(KEY_IS_INSTALL, false);
                getSyncHelper().onValueModified(KEY_TRAITS, nbt);
                return true;
            }
            return false;
        }

        @Override
        public CompoundNBT serializeNBT() {
            return delegate.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            delegate.deserializeNBT(nbt);
        }

        @Override
        public void accept(ListNBT list) {
            for (INBT nbt :list) {
                CompoundNBT compound = (CompoundNBT) nbt;
                boolean isInstall = compound.getBoolean(KEY_IS_INSTALL);
                TieredUpgrade upgrade = TieredUpgrade.deserialize(compound.getCompound(KEY_UPGRADE));

                if (isInstall && !delegate.installUpgrade(upgrade))
                    DireCore20.LOG.error("Failed to handle sync, which was installing {}!", upgrade);
                else if (!isInstall && !delegate.removeUpgrade(upgrade))
                    DireCore20.LOG.error("Failed to handle sync, which was removing {}!", upgrade);
            }
        }
    }

    private final class ModificationSyncPropertyContainer implements IPropertyContainer, Consumer<ListNBT> {
        private static final String KEY_PROP = "property";
        private static final String KEY_VALUE = "value";
        private final IPropertyContainer delegate;
        private final Map<String, MutableProperty<?>> nameToProperty;

        public ModificationSyncPropertyContainer(IPropertyContainer delegate, Set<MutableProperty<?>> mutableProperties) {
            this.delegate = delegate;
            nameToProperty = mutableProperties.stream()
                    .collect(Collectors.toMap(MutableProperty::getName, Function.identity()));
        }

        @Override
        public <T> Optional<T> getProperty(MutableProperty<T> property) {
            return delegate.getProperty(property);
        }

        @Override
        public <T> Optional<T> getProperty(Property<T> property) {
            return delegate.getProperty(property);
        }

        @Override
        public <T> boolean setProperty(MutableProperty<T> property, T value) {
            if (delegate.setProperty(property, value)) {
                CompoundNBT nbt = new CompoundNBT();
                nbt.putString(KEY_PROP, property.getName());
                nbt.put(KEY_VALUE, property.serialize(value));
                getSyncHelper().onValueModified(KEY_PROPERTIES, nbt);
                return true;
            }
            return false;
        }

        @Override
        public Set<Property<?>> listProperties() {
            return delegate.listProperties();
        }

        @Override
        public CompoundNBT serializeNBT() {
            return delegate.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            delegate.deserializeNBT(nbt);
        }

        //called when a sync is received
        @Override
        public void accept(ListNBT list) {
            for (INBT nbt :list) {
                CompoundNBT compound = (CompoundNBT) nbt;

                @SuppressWarnings("unchecked") //we will only pass the correct objects, created by the Prop itself to the delegate => safe
                MutableProperty<Object> property = (MutableProperty<Object>) nameToProperty.get(compound.getString(KEY_PROP));
                if (property == null) {
                    DireCore20.LOG.error("Attempted to handle sync from unkown Property with name {}, but this does not seem to be known!", compound.getString(KEY_PROP));
                    continue;
                }

                Object deserialized = property.deserialize(compound.get(KEY_VALUE));
                if (!delegate.setProperty(property, deserialized))
                    DireCore20.LOG.error("Attempted to handle sync from {} , but could not set value to {} which was given as {}!",
                            property, deserialized, compound.get(KEY_VALUE));
            }
        }
    }
}
