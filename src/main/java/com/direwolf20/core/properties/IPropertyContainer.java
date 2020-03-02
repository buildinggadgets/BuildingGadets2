package com.direwolf20.core.properties;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Optional;
import java.util.Set;

/**
 * A serializable container handling {@link Property Properties}.
 * <p>
 * Retrieving the value for a specific {@link Property} can be done via {@link #getProperty(Property)} and it can be set via
 * {@link #setProperty(MutableProperty, Object)}. Notice that it is not possible to add new Properties to the container, after it has been constructed.
 * This class intentionally does not function as a generic container for attaching mutable values to
 * {@link net.minecraftforge.common.capabilities.ICapabilityProvider CapProviders}, but instead is meant as a type safe container
 * for the {@link net.minecraftforge.common.capabilities.ICapabilityProvider Providers} themselves. A Provider can limit the scope of access for
 * Properties by limiting the access to the {@link Property} key. In order to give even more fine-grained access, one may restrict access
 * to the {@link MutableProperty}, but give free access to the {@link Property} - which simulates a read-only Property to the public!
 * <p>
 * Serialisation will write the values of <b>all</b> Properties into the resulting {@link CompoundNBT}, keyed by {@link Property#getName()}.
 * This results in all {@link Property Properties} within one container being forced to have unique names! It is up to the implementor
 * of the Property and the user of the container to ensure that migration stays possible...
 */
public interface IPropertyContainer extends INBTSerializable<CompoundNBT> {
    default <T> Optional<T> getProperty(MutableProperty<T> property) {
        return getProperty(property.getProperty());
    }

    <T> Optional<T> getProperty(Property<T> property);

    <T> boolean setProperty(MutableProperty<T> property, T value);

    Set<Property<?>> listProperties();
}
