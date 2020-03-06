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

/**
 * An intentionally subclassable {@link ICapabilityProvider} implementation which gives access to both an {@link ITraitContainer}
 * and an {@link IPropertyContainer}. This Provider is intended to be used with an {@link ItemStack} and will update a
 * {@link #KEY_CHANGE_COUNT change count} property on the stack's {@link ItemStack#getTag() NBT-Tag}, every time a
 * {@link com.direwolf20.core.properties.Property Property} is modified or an {@link com.direwolf20.core.traits.upgrade.TieredUpgrade Upgrade}
 * is installed. As a result it is possible to implement syncing by reading and writing the nbt data of both caps to
 * the {@link net.minecraft.item.Item#getShareTag(ItemStack) ItemStacks Share Tag}. This change count is intentionally only a byte
 * and will overflow from time to time - this won't hurt us, as we only need it to change.
 * <p>
 * Subclasses who intend on implementing similar syncing mechanisms, may just call {@link #onValueModified()} any time a value was modified.
 * The {@link ItemStack#getTag() Stack-Tag} will be modified as described above.
 * <p>
 * Notice that this {@link ICapabilityProvider} implements {@link INBTSerializable} and therefore enables the resulting caps to
 * be saved to the regular cap storage.
 */
public class PropertyTraitCapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundNBT> {
    //the key is longer then the actual value... I'm tempted to use just 'c' or even the empty string as key for this
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
        nbt.putByte(KEY_CHANGE_COUNT, (byte) (nbt.getByte(KEY_CHANGE_COUNT) + 1));
    }

}
