package com.direwolf20.core.capability;

import com.direwolf20.core.properties.IPropertyContainer;
import com.direwolf20.core.properties.MutableProperty;
import com.direwolf20.core.properties.Property;
import com.direwolf20.core.traits.ITraitContainer;
import com.direwolf20.core.traits.Trait;
import com.direwolf20.core.traits.upgrade.TieredUpgrade;
import com.direwolf20.core.traits.upgrade.Upgrade;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;

public class PropertyTraitCapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundNBT> {
    private static final String KEY_CHANGE_COUNT = "change_count";
    private static final String KEY_TRAITS = "traits";
    private static final String KEY_PROPERTIES = "properties";
    private final ModificationSyncTraitContainer traitContainer;
    private final ModificationSyncPropertyContainer propertyContainer;
    private final LazyOptional<ITraitContainer> traitContainerOpt;
    private final LazyOptional<IPropertyContainer> propertyContainerOpt;
    private final ItemStack stack;

    public PropertyTraitCapabilityProvider(ItemStack stack, ITraitContainer traitContainer, IPropertyContainer propertyContainer) {
        this.traitContainer = new ModificationSyncTraitContainer(traitContainer);
        this.traitContainerOpt = LazyOptional.of(this::getTraitContainer);
        this.propertyContainer = new ModificationSyncPropertyContainer(propertyContainer);
        this.propertyContainerOpt = LazyOptional.of(this::getPropertyContainer);
        this.stack = stack;
    }

    @Nonnull
    public ITraitContainer getTraitContainer() {
        return traitContainer;
    }

    @Nonnull
    public IPropertyContainer getPropertyContainer() {
        return propertyContainer;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
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

    protected void onValueModified() {
        CompoundNBT nbt = stack.getOrCreateTag();
        nbt.putInt(KEY_CHANGE_COUNT, nbt.getInt(KEY_CHANGE_COUNT)+1);
    }

    private final class ModificationSyncTraitContainer implements ITraitContainer{
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
        public Set<Upgrade> listUpgrades() {
            return delegate.listUpgrades();
        }

        @Override
        public boolean installUpgrade(TieredUpgrade upgrade) {
            if (delegate.installUpgrade(upgrade)) {
                onValueModified();
                return true;
            }
            return false;
        }

        @Override
        public boolean removeUpgrade(TieredUpgrade upgrade) {
            if (delegate.removeUpgrade(upgrade)) {
                onValueModified();
                return true;
            }
            return false;
        }

        @Override
        public CompoundNBT serializeNBT() {
            return delegate.serializeNBT();
        }

        @Override
        public CompoundNBT serializeNBT(boolean persistend) {
            return delegate.serializeNBT(persistend);
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            delegate.deserializeNBT(nbt);
        }
    }

    private final class ModificationSyncPropertyContainer implements IPropertyContainer {
        private final IPropertyContainer delegate;

        public ModificationSyncPropertyContainer(IPropertyContainer delegate) {
            this.delegate = delegate;
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
                onValueModified();
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
    }
}
