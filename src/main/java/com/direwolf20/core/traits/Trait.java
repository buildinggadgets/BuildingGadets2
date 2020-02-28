package com.direwolf20.core.traits;


import com.direwolf20.core.DireCore20;
import com.google.common.base.MoreObjects;

import javax.annotation.Nullable;
import java.util.Objects;

public final class Trait<T> {
    public static final Trait<Boolean> SILK_TOUCH = new Trait<>(Boolean.class, DireCore20.MODID+":silk_touch");
    public static final Trait<Integer> MAX_POWER = new Trait<>(Integer.class, DireCore20.MODID+":max_power");
    public static final Trait<Integer> MAX_SIZE = new Trait<>(Integer.class, DireCore20.MODID+":max_size");
    public static final Trait<Integer> MAX_RANGE = new Trait<>(Integer.class, DireCore20.MODID+":max_range");
    public static final Trait<Integer> ACTION_COST = new Trait<>(Integer.class, DireCore20.MODID+":action_cost");
    private final Class<T> type;
    @Nullable
    private final String name;

    public Trait(Class<T> type, @Nullable String name) {
        this.type = Objects.requireNonNull(type);
        this.name = name;
    }

    T cast(Object v) {
        return type.cast(Objects.requireNonNull(v));
    }

    @Nullable
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("type", type)
                .add("name", name)
                .toString();
    }
}
