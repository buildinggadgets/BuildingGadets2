package com.direwolf20.buildinggadgets.core.characteristics;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

final class CharacteristicValue<T> {
    private final Supplier<T> defaultValue;
    private final Map<TieredUpgrade, UnaryOperator<T>> modificators;

    CharacteristicValue(Supplier<T> defaultValue) {
        this.defaultValue = defaultValue;
        this.modificators = new LinkedHashMap<>();
    }

    T getValue() {
        T base = defaultValue.get();
        for (UnaryOperator<T> op: modificators.values())
            base = op.apply(base);
        return base;
    }

    boolean addModificator(TieredUpgrade upgrade, UnaryOperator<T> unaryOperator) {
        if (modificators.containsKey(upgrade))
            return false;
        modificators.put(upgrade, unaryOperator);
        return true;
    }

    boolean removeModificator(TieredUpgrade upgrade) {
        return modificators.remove(upgrade)!=null;
    }
}
