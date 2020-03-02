package com.direwolf20.core.traits;


import com.direwolf20.core.DireCore20;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import net.minecraft.util.ResourceLocation;

import java.util.Objects;

/**
 * A Trait is some intrinsic upgradable characteristic of an {@link net.minecraftforge.common.capabilities.ICapabilityProvider Capability Provider}.
 * The value of an Trait may be queried from {@link ITraitContainer Trait Containers}, if the trait is present. It specifically represents only
 * a (possibly named) key for some value, which may be retrieved in a type safe way by casting it to the type passed into the constructor.
 * @param <T> The type of value represented by the Trait
 */
public final class Trait<T> {
    public static final Trait<Boolean> SILK_TOUCH = createNamespaced(Boolean.class, DireCore20.MOD_ID , "silk_touch");
    public static final Trait<Integer> MAX_POWER = createNamespaced(Integer.class, DireCore20.MOD_ID ,"max_power");
    public static final Trait<Integer> MAX_SIZE = createNamespaced(Integer.class, DireCore20.MOD_ID, "max_size");
    public static final Trait<Integer> MAX_RANGE = createNamespaced(Integer.class, DireCore20.MOD_ID, "max_range");
    public static final Trait<Integer> ACTION_COST = createNamespaced(Integer.class, DireCore20.MOD_ID, "action_cost");
    public static <T> Trait<T> create(Class<T> type, String name) {
        return new Trait<>(type, name);
    }

    public static <T> Trait<T> createNamespaced(Class<T> type, String namespace, String path) {
        return create(type, namespace+":"+path);
    }

    public static <T> Trait<T> createNamespaced(Class<T> type, ResourceLocation name) {
        return create(type, name.toString());
    }

    private final Class<T> type;
    private final String name;
    private Trait(Class<T> type, String name) {
        Preconditions.checkNotNull(name);
        Preconditions.checkArgument(!name.isEmpty());
        this.type = Objects.requireNonNull(type);
        this.name = name;
    }

    T cast(Object v) {
        return type.cast(Objects.requireNonNull(v));
    }

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
