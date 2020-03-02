package com.direwolf20.core.properties;

import com.google.common.base.MoreObjects;
import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.util.ResourceLocation;

import java.util.Objects;
import java.util.function.Function;

public final class Property<T> {
    public static <T> Builder<T> builder(Class<T> type) {
        return new Builder<>(type);
    }

    public static Builder<Integer> intBuilder() {
        return builder(Integer.class).serializer(IntNBT::valueOf).deserializer(inbt -> ((IntNBT) inbt).getInt());
    }

    public static Builder<Boolean> booleanBuilder() {
        return builder(Boolean.class).serializer(ByteNBT::valueOf).deserializer(inbt -> ((ByteNBT) inbt).getByte() != 0);
    }

    public static Builder<Float> floatBuilder() {
        return builder(Float.class).serializer(FloatNBT::valueOf).deserializer(inbt -> ((FloatNBT) inbt).getFloat());
    }

    private final Class<T> type;
    private final String name;
    private final Function<T, INBT> serializer;
    private final Function<INBT, T> deserializer;

    private Property(Class<T> type, String name, Function<T, INBT> serializer, Function<INBT, T> deserializer) {
        this.type = Objects.requireNonNull(type, "Cannot have a property without a type!");
        this.name = Objects.requireNonNull(name, "Cannot have a property without a name!");
        this.serializer = Objects.requireNonNull(serializer);
        this.deserializer = Objects.requireNonNull(deserializer);
    }

    T cast(Object value) {
        return type.cast(Objects.requireNonNull(value));
    }

    public String getName() {
        return name;
    }

    public INBT serializeValue(Object value) {
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

    public static final class Builder<T> {
        private Class<T> type;
        private String name;
        private Function<T, INBT> serializer;
        private Function<INBT, T> deserializer;

        private Builder(Class<T> type) {
            this.type = Objects.requireNonNull(type);
        }

        public Builder<T> name(ResourceLocation resourceLocation) {
            return name(resourceLocation.toString());
        }

        public Builder<T> name(String modid, String path) {
            return name(modid+":"+path);
        }

        public Builder<T> name(String name) {
            this.name = Objects.requireNonNull(name);
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

        public Property<T> build(String name) {
            name(name);
            return build();
        }

        public Property<T> build() {
            return new Property<>(type, name, serializer, deserializer);
        }
    }
}
