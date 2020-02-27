package com.direwolf20.buildinggadgets.core.characteristics;


import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import java.util.function.UnaryOperator;

public final class TieredUpgrade {
    private final Upgrade upgrade;
    private final int tier;

    public TieredUpgrade(Upgrade upgrade, int tier) {
        Preconditions.checkNotNull(upgrade);
        Preconditions.checkArgument(upgrade.isValidTier(tier), "Cannot have tier "+tier+" for upgrade "+upgrade.getRegistryName());
        this.upgrade = upgrade;
        this.tier = tier;
    }

    public Upgrade getUpgrade() {
        return upgrade;
    }

    public <T> UnaryOperator<T> getModificatorFor(Characteristic<T> characteristic) {
        return upgrade.getModificatorFor(characteristic, tier);
    }

    public ImmutableSet<Characteristic<?>> getAppliedModifications() {
        return getUpgrade().getAppliedModifications();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (! (o instanceof TieredUpgrade)) return false;

        final TieredUpgrade that = (TieredUpgrade) o;

        if (tier != that.tier) return false;
        return getUpgrade().equals(that.getUpgrade());
    }

    @Override
    public int hashCode() {
        int result = getUpgrade().hashCode();
        result = 31 * result + tier;
        return result;
    }
}
