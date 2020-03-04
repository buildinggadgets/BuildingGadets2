package com.direwolf20.core.capability;

import com.direwolf20.core.properties.IPropertyContainer;
import com.direwolf20.core.properties.ModificationPropertyContainer;
import com.direwolf20.core.traits.ITraitContainer;
import com.direwolf20.core.traits.ModificationTraitContainer;
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

public class PropertyTraitCapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundNBT> {
    private static final String KEY_CHANGE_COUNT = "change_count";
    private static final String KEY_TRAITS = "traits";
    private static final String KEY_PROPERTIES = "properties";

    private final ModificationTraitContainer traitContainer;
    private final ModificationPropertyContainer propertyContainer;
    private final ItemStack stack;

    private final LazyOptional<ITraitContainer> traitContainerOpt;
    private final LazyOptional<IPropertyContainer> propertyContainerOpt;

    public PropertyTraitCapabilityProvider(ItemStack stack, ITraitContainer traitContainer, IPropertyContainer propertyContainer) {
        this.traitContainer = new ModificationTraitContainer(traitContainer, (u, b) -> onValueModified());
        this.traitContainerOpt = LazyOptional.of(this::getTraitContainer);
        this.propertyContainer = new ModificationPropertyContainer(propertyContainer, (p, v) -> onValueModified());
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

}
