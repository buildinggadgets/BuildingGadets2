package com.direwolf20.core.properties;

import net.minecraft.nbt.CompoundNBT;

import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

public final class ModificationPropertyContainer implements IPropertyContainer {
    private final IPropertyContainer delegate;
    private final BiConsumer<MutableProperty<?>, Object> onModificationCallback;

    public ModificationPropertyContainer(IPropertyContainer delegate, BiConsumer<MutableProperty<?>, Object> onModificationCallback) {
        this.delegate = delegate;
        this.onModificationCallback = onModificationCallback;
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
            onModificationCallback.accept(property, value);
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
