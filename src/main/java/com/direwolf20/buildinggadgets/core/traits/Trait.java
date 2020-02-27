package com.direwolf20.buildinggadgets.core.traits;


import java.util.Objects;

public final class Trait<T> {
    private final Class<T> type;

    public Trait(Class<T> type) {
        this.type = type;
    }

    T cast(Object v) {
        return type.cast(Objects.requireNonNull(v));
    }
}
