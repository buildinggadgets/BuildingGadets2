package com.direwolf20.core.traits;


import com.direwolf20.core.DireCore20;
import com.google.common.base.MoreObjects;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * A Trait is some intrinsic upgradable characteristic of an {@link net.minecraftforge.common.capabilities.ICapabilityProvider Capability Provider}.
 * The value of an Trait may be queried from {@link ITraitContainer Trait Containers}, if the trait is present. It specifically represents only
 * a (possibly named) key for some value, which may be retrieved in a type safe way by casting it to the type passed into the constructor.
 * @param <T> The type of value represented by the Trait
 */
public final class Trait<T> {
    public static final Trait<Boolean> SILK_TOUCH = new Trait<>(Boolean.class, DireCore20.MOD_ID +":silk_touch");
    public static final Trait<Integer> MAX_POWER = new Trait<>(Integer.class, DireCore20.MOD_ID +":max_power");
    public static final Trait<Integer> MAX_SIZE = new Trait<>(Integer.class, DireCore20.MOD_ID +":max_size");
    public static final Trait<Integer> MAX_RANGE = new Trait<>(Integer.class, DireCore20.MOD_ID +":max_range");
    public static final Trait<Integer> ACTION_COST = new Trait<>(Integer.class, DireCore20.MOD_ID +":action_cost");
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
