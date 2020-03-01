package com.direwolf20.core.traits.upgrade;

import com.direwolf20.core.traits.Trait;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * A utility class for building upgrades for the most common use cases.
 * The resulting upgrades manage the modification functions in a {@link ImmutableMap map} and the valid Tier check via
 * a {@link Range} object.
 */
public final class UpgradeBuilder {
    public static UpgradeBuilder create() {
        return new UpgradeBuilder();
    }

    private final ImmutableMap.Builder<Trait<?>, Function<TieredUpgrade, UnaryOperator<?>>> modifications;

    private UpgradeBuilder() {
        modifications = ImmutableMap.builder();
    }

    /**
     * All other modification adding functions delegate through to this Method!
     * @param trait The trait to add or replace
     * @param factory Factory function, for mapping a {@link TieredUpgrade} to a corresponding operator
     * @param <T> The value type
     * @return The builder instance
     * @throws NullPointerException if either trait or factory are null
     */
    public <T> UpgradeBuilder putModification(Trait<T> trait, Function<TieredUpgrade, UnaryOperator<?>> factory) {
        modifications.put(Objects.requireNonNull(trait), Objects.requireNonNull(factory));
        return this;
    }

    public UpgradeBuilder sumModifier(Trait<Integer> trait, Function<TieredUpgrade, Integer> additionFactory) {
        return putModification(trait, tieredUpgrade -> ((Integer i) -> (additionFactory.apply(tieredUpgrade) + i)));
    }

    public UpgradeBuilder floatSumModifier(Trait<Double> trait, Function<TieredUpgrade, Double> additionFactory) {
        return putModification(trait, tieredUpgrade -> ((Double d) -> (additionFactory.apply(tieredUpgrade) + d)));
    }

    public UpgradeBuilder multiplicationModifier(Trait<Integer> trait, Function<TieredUpgrade, Integer> additionFactory) {
        return putModification(trait, tieredUpgrade -> ((Integer i) -> (additionFactory.apply(tieredUpgrade) * i)));
    }

    public UpgradeBuilder floatMultiplicationModifier(Trait<Double> trait, Function<TieredUpgrade, Double> additionFactory) {
        return putModification(trait, tieredUpgrade -> ((Double d) -> (additionFactory.apply(tieredUpgrade) * d)));
    }

    public UpgradeBuilder activationModifier(Trait<Boolean> trait) {
        return putModification(trait, tieredUpgrade -> b -> Boolean.TRUE);
    }

    public UpgradeBuilder deactivationModifier(Trait<Boolean> trait) {
        return putModification(trait, tieredUpgrade -> b -> Boolean.FALSE);
    }

    public Upgrade build(Range<Integer> validTiers) {
        return new BuiltUpgrade(modifications.build(), Objects.requireNonNull(validTiers));
    }

    private static final class BuiltUpgrade extends Upgrade{
        private final ImmutableMap<Trait<?>, Function<TieredUpgrade, UnaryOperator<?>>> modifications;
        private final Range<Integer> validTiers;

        public BuiltUpgrade(ImmutableMap<Trait<?>, Function<TieredUpgrade, UnaryOperator<?>>> modifications, Range<Integer> validTiers) {
            super(modifications.keySet());
            this.modifications = modifications;
            this.validTiers = validTiers;
        }

        @Override
        protected boolean isValidLevel(int level) {
            return validTiers.contains(level);
        }

        @Override
        @SuppressWarnings("unchecked") //This is a safe cast, as only type safe operators have been added to the map
        public <T> UnaryOperator<T> getModificatorFor(Trait<T> trait, TieredUpgrade tier) {
            Function<TieredUpgrade, UnaryOperator<?>> factory = modifications.get(trait); //Kotlin, where are you, when one needs you... :(
            return factory!=null? (UnaryOperator<T>) factory.apply(tier) : null;
        }
    }
}
