package com.direwolf20.core.traits;

import com.direwolf20.core.traits.upgrade.Upgrade;
import com.direwolf20.core.traits.upgrade.UpgradeStack;
import net.minecraft.nbt.CompoundNBT;

import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
/**
 * A delegating {@link ITraitContainer} implementation, which allows users to add a callback every time an Upgrade
 * is installed or uninstalled.
 * Used for example by {@link com.direwolf20.core.capability.PropertyTraitCapabilityProvider} to keep track of changes made
 * to Traits.
 */
public final class ModificationTraitContainer implements ITraitContainer{
    private final ITraitContainer delegate;
    private final BiConsumer<UpgradeStack, Boolean> upgradeCallback;

    public ModificationTraitContainer(ITraitContainer delegate, BiConsumer<UpgradeStack, Boolean> upgradeCallback) {
        this.delegate = delegate;
        this.upgradeCallback = upgradeCallback;
    }

    @Override
    public <T> Optional<T> getTrait(Trait<T> trait) {
        return delegate.getTrait(trait);
    }

    @Override
    public Set<UpgradeStack> listTiers() {
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
    public boolean installUpgrade(UpgradeStack upgrade) {
        if (delegate.installUpgrade(upgrade)) {
            this.upgradeCallback.accept(upgrade, true);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeUpgrade(UpgradeStack upgrade) {
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
