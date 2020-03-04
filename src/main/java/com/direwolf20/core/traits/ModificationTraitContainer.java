package com.direwolf20.core.traits;

import com.direwolf20.core.traits.upgrade.TieredUpgrade;
import com.direwolf20.core.traits.upgrade.Upgrade;
import net.minecraft.nbt.CompoundNBT;

import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

public final class ModificationTraitContainer implements ITraitContainer{
    private final ITraitContainer delegate;
    private final BiConsumer<TieredUpgrade, Boolean> upgradeCallback;

    public ModificationTraitContainer(ITraitContainer delegate, BiConsumer<TieredUpgrade, Boolean> upgradeCallback) {
        this.delegate = delegate;
        this.upgradeCallback = upgradeCallback;
    }

    @Override
    public <T> Optional<T> getTrait(Trait<T> trait) {
        return delegate.getTrait(trait);
    }

    @Override
    public Set<TieredUpgrade> listTiers() {
        return delegate.listTiers();
    }

    @Override
    public Set<Trait<?>> listTraits() {
        return delegate.listTraits();
    }

    @Override
    public Set<Upgrade> listUpgrades() {
        return delegate.listUpgrades();
    }

    @Override
    public boolean installUpgrade(TieredUpgrade upgrade) {
        if (delegate.installUpgrade(upgrade)) {
            this.upgradeCallback.accept(upgrade, true);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeUpgrade(TieredUpgrade upgrade) {
        if (delegate.removeUpgrade(upgrade)) {
            this.upgradeCallback.accept(upgrade, false);
            return true;
        }
        return false;
    }

    @Override
    public CompoundNBT serializeNBT() {
        return delegate.serializeNBT();
    }

    @Override
    public CompoundNBT serializeNBT(boolean persistend) {
        return delegate.serializeNBT(persistend);
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        delegate.deserializeNBT(nbt);
    }
}
