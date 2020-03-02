package com.direwolf20.core.properties;

import com.google.common.base.Preconditions;
import net.minecraft.nbt.CompoundNBT;

import java.util.*;

/**
 * The default {@link IPropertyContainer} which is created using a {@link Builder} to add the {@link Property Properties}
 * represented by this container.
 * @see IPropertyContainer
 * @see Property
 */
public final class PropertyContainer implements IPropertyContainer{
    public static Builder builder() {
        return new Builder();
    }

    private final Map<Property<?>, Object> properties;
    private final Map<String, Property<?>> propertyByName;

    private PropertyContainer(Map<Property<?>, Object> properties, Map<String, Property<?>> propertyByName) {
        this.properties = properties;
        this.propertyByName = propertyByName;
    }

    @Override
    public <T> Optional<T> getProperty(Property<T> property) {
        return Optional.ofNullable(properties.get(property))
                .map(property::cast);
    }

    @Override
    public <T> boolean setProperty(Property<T> property, T value) {
        if (properties.containsKey(property)) {
            properties.put(property, value);
            return true;
        }
        return false;
    }

    @Override
    public Set<Property<?>> listProperties() {
        return Collections.unmodifiableSet(properties.keySet());
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        for (Map.Entry<Property<?>, Object> entry : properties.entrySet())
            if (entry.getValue() != null)
                nbt.put(entry.getKey().getName(), entry.getKey().serializeValue(entry.getValue()));
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        for (String key : nbt.keySet()) {
            Property<?> prop = propertyByName.get(key);
            if (prop != null) //This implicitly also checks whether the property is already in here...
                properties.put(prop, prop.deserialize(nbt.get(key)));
        }
    }

    /**
     * A simple build for the {@link PropertyContainer}. Notice that it enforces the container to only contain properties with
     * distinc {@link Property#getName() names}, as per contract of {@link IPropertyContainer}!
     */
    public static final class Builder {
        private Map<Property<?>, Object> properties;
        private Map<String, Property<?>> propertyByName;

        public Builder() {
            this.properties = new IdentityHashMap<>();
            this.propertyByName = new HashMap<>();
        }

        /**
         * Sets the given Property to the given value / adds it with the given default, if not present.
         * @param prop The Property to set/add
         * @param value The value to set it to
         * @param <T> The type of the value
         * @return The Builder instance
         * @throws IllegalArgumentException if a different Property, with the same name, was already in this Builder. (See the contract of {@link IPropertyContainer}.)
         */
        public <T> Builder putProperty(Property<T> prop, T value) {
            Preconditions.checkArgument(propertyByName.containsKey(prop.getName()) && propertyByName.get(prop.getName()) != prop,
                    "Caught ambiguous %s during PropertyContainer-Construction. Each Property in the container must have a unique serialisation name! This will not be added to the container!"
                    , prop);
            properties.put(prop, value);
            propertyByName.put(prop.getName(), prop);
            return this;
        }

        public PropertyContainer build() {
            //do not convert to ImmutableMap here, as string key's are not well-behaved and this would therefore be slower
            return new PropertyContainer(properties, propertyByName);
        }
    }
}
