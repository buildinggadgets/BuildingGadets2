package com.direwolf20.core.properties;

import com.direwolf20.core.DireCore20;
import net.minecraft.nbt.CompoundNBT;

import java.util.*;

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

    public static final class Builder {
        private Map<Property<?>, Object> properties;
        private Map<String, Property<?>> propertyByName;

        public Builder() {
            this.properties = new IdentityHashMap<>();
            this.propertyByName = new HashMap<>();
        }

        public <T> Builder putProperty(Property<T> prop, T value) {
            if (propertyByName.containsKey(prop.getName()) && propertyByName.get(prop.getName()) != prop) {
                DireCore20.LOG.error("Caught ambiguous {} during PropertyContainer-Construction. " +
                        "Each Property in the container must have a unique serialisation name! This will not be added to the container!", prop);
                return this;
            }
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
