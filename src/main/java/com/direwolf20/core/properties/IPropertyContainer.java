package com.direwolf20.core.properties;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Optional;
import java.util.Set;

public interface IPropertyContainer extends INBTSerializable<CompoundNBT> {
    <T> Optional<T> getProperty(Property<T> property);

    <T> boolean setProperty(Property<T> property, T value);

    Set<Property<?>> listProperties();
}
