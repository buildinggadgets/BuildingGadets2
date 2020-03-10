package com.direwolf20.core.properties;

import com.google.common.base.MoreObjects;
import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.util.ResourceLocation;

import java.util.Objects;
import java.util.function.Function;

/**
 * A Property is the key to some mutable and serializable value within an {@link IPropertyContainer}. The value may be retrieved via
 * {@link IPropertyContainer#getProperty(Property)} or set via {@link IPropertyContainer#setProperty(MutableProperty, Object)} (if a write-access Property
 * has been obtained) in a type safe way. The latter is achieved, by casting the results to they type passed into the builder.
 * <p>
 * Notice that as of this writing a Property must be capable of serializing and deserializing it's values, as well as having a
 * {@link IPropertyContainer container} wide unique name.
 *
 * @param <T> The type of the values represented by this Property
 * @see IPropertyContainer
 */
public final class Property<T> {
    private final Class<T> type;
    private final String name;
    private final Function<T, INBT> serializer;
    private final Function<INBT, T> deserializer;

    private Property(Class<T> type, String name, Function<T, INBT> serializer, Function<INBT, T> deserializer) {
        this.type = Objects.requireNonNull(type, "Cannot have a property without a type!");
        this.name = Objects.requireNonNull(name, "Cannot have a property without a name!");
        this.serializer = Objects.requireNonNull(serializer, "Cannot have a property without a serializer!");
        this.deserializer = Objects.requireNonNull(deserializer, "Cannot have a property without a deserializer!");
    }

    /**
     * @param type The class of the values represented by the resulting {@link Property}
     * @param <T>  The type of the values represented by the resulting {@link Property}
     * @return a new {@link Builder}
     */
    public static <T> Builder<T> builder(Class<T> type) {
        return new Builder<>(type);
    }

    /**
     * @return A Builder pre-configured to create serializable Integer Properties. Only the name is missing.
     */
    public static Builder<Integer> intBuilder() {
        return builder(Integer.class).serializer(IntNBT::valueOf).deserializer(inbt -> ((IntNBT) inbt).getInt());
    }

    /**
     * @return A Builder pre-configured to create serializable Boolean Properties. Only the name is missing.
     */
    public static Builder<Boolean> booleanBuilder() {
        return builder(Boolean.class).serializer(ByteNBT::valueOf).deserializer(inbt -> ((ByteNBT) inbt).getByte() != 0);
    }

    /**
     * @return A Builder pre-configured to create serializable Float Properties. Only the name is missing.
     */
    public static Builder<Float> floatBuilder() {
        return builder(Float.class).serializer(FloatNBT::valueOf).deserializer(inbt -> ((FloatNBT) inbt).getFloat());
    }

    T cast(Object value) {
        return type.cast(Objects.requireNonNull(value));
    }

    public String getName() {
        return name;
    }

    public INBT serializeValue(Object value) {
        if (value == null)
            return serialize(null);
        return serialize(cast(value));
    }

    public INBT serialize(T value) {
        return serializer.apply(value);
    }

    public T deserialize(INBT serialized) {
        return deserializer.apply(serialized);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("type", type)
                .add("name", name)
                .toString();
    }

    /**
     * A simple builder to allow chaining of the required values, instead of being required to pass values to a lengthy factory Method.
     * It also offers some utility overloads.
     *
     * @param <T> The type of the resulting {@link Property}
     */
    public static final class Builder<T> {
        private Class<T> type;
        private String name;
        private Function<T, INBT> serializer;
        private Function<INBT, T> deserializer;

        private Builder(Class<T> type) {
            this.type = Objects.requireNonNull(type);
        }

        public Builder<T> name(ResourceLocation resourceLocation) {
            this.name = resourceLocation.toString();
            return this;
        }

        public Builder<T> name(String modid, String path) {
            this.name = Objects.requireNonNull(modid) + ":" + Objects.requireNonNull(path);
            return this;
        }

        public Builder<T> serializer(Function<T, INBT> serializer) {
            this.serializer = Objects.requireNonNull(serializer);
            return this;
        }

        public Builder<T> deserializer(Function<INBT, T> deserializer) {
            this.deserializer = Objects.requireNonNull(deserializer);
            return this;
        }

        public Property<T> build(ResourceLocation resourceLocation) {
            name(resourceLocation);
            return build();
        }

        public Property<T> build(String modid, String path) {
            name(modid, path);
            return build();
        }

        public Property<T> build() {
            return new Property<>(type, name, serializer, deserializer);
        }

        public MutableProperty<T> buildMutable(ResourceLocation resourceLocation) {
            name(resourceLocation);
            return buildMutable();
        }

        public MutableProperty<T> buildMutable(String modid, String path) {
            name(modid, path);
            return buildMutable();
        }

        public MutableProperty<T> buildMutable() {
            return new MutableProperty<>(build());
        }
    }
}
