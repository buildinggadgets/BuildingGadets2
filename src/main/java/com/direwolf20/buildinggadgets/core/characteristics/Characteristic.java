package com.direwolf20.buildinggadgets.core.characteristics;


import java.util.Objects;

public final class Characteristic<T> {
    private final Class<T> type;

    public Characteristic(Class<T> type) {
        this.type = type;
    }

    T cast(Object v) {
        return type.cast(Objects.requireNonNull(v));
    }
}
