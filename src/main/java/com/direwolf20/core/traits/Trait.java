package com.direwolf20.core.traits;


import java.util.Objects;

public final class Trait<T> {
    public static final Trait<Boolean> SILK_TOUCH = new Trait<>(Boolean.class);
    public static final Trait<Integer> MAX_POWER = new Trait<>(Integer.class);
    public static final Trait<Integer> MAX_SIZE = new Trait<>(Integer.class);
    public static final Trait<Integer> MAX_RANGE = new Trait<>(Integer.class);
    public static final Trait<Integer> ACTION_COST = new Trait<>(Integer.class);
    private final Class<T> type;

    public Trait(Class<T> type) {
        this.type = type;
    }

    T cast(Object v) {
        return type.cast(Objects.requireNonNull(v));
    }
}
