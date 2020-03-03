package com.direwolf20.core.properties;

import com.google.common.base.MoreObjects;
import net.minecraft.nbt.INBT;

/**
 * A wrapper around {@link Property} to allow for
 * @param <T>
 */
public final class MutableProperty<T> {
    MutableProperty(Property<T> delegate) {
        this.delegate = delegate;
    }

    private final Property<T> delegate;

    public T cast(Object value) {
        return delegate.cast(value);
    }

    public String getName() {
        return delegate.getName();
    }

    public INBT serializeValue(Object value) {
        return delegate.serializeValue(value);
    }

    public INBT serialize(T value) {
        return delegate.serialize(value);
    }

    public T deserialize(INBT serialized) {
        return delegate.deserialize(serialized);
    }

    public Property<T> getProperty() {
        return delegate;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("property", delegate)
                .toString();
    }
}
