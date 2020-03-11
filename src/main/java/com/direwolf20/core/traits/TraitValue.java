package com.direwolf20.core.traits;

import com.direwolf20.core.traits.upgrade.UpgradeStack;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Utility class for managing the value represented by a {@link Trait} in some {@link ITraitContainer}.
 *
 * @param <T> The type of value
 */
final class TraitValue<T> {
    private final Supplier<T> defaultValue;
    private final Map<UpgradeStack, UnaryOperator<T>> modificators;

    TraitValue(Supplier<T> defaultValue) {
        this.defaultValue = defaultValue;
        this.modificators = new LinkedHashMap<>();
    }

    T getValue() {
        T base = defaultValue.get();
        for (UnaryOperator<T> op : modificators.values())
            base = op.apply(base);
        return base;
    }

    boolean addModificator(UpgradeStack upgrade, UnaryOperator<T> unaryOperator) {
        if (modificators.containsKey(upgrade))
            return false;
        modificators.put(upgrade, unaryOperator);
        return true;
    }

    boolean removeModificator(UpgradeStack upgrade) {
        return modificators.remove(upgrade) != null;
    }

    void clearModificators() {
        modificators.clear();
    }
}
