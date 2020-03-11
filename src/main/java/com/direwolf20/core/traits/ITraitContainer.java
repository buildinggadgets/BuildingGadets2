package com.direwolf20.core.traits;

import com.direwolf20.core.traits.upgrade.Upgrade;
import com.direwolf20.core.traits.upgrade.UpgradeStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A serializable container mapping {@link Trait Traits} to their values as well as handling upgrades.
 * <p>
 * Retrieving the value for a specific trait can be done via {@link #getTrait(Trait)}, if the {@link Trait} is present. It is not
 * possible to add or remove Traits to the container after it has been created and subclasses should not try to add this
 * behaviour. This limitation is imposed as the logic handling some {@link Trait Trait's} value would not know about new Traits
 * (or expect others to be present) and therefore might not work as expected. Furthermore this class does intentionally not
 * function as a generic container for values - {@link Trait Traits} are characteristic Properties of some Object and should only
 * be upgraded, not set to some arbitrary value.
 * <p>
 * Installing and removing upgrades can be done via {@link #installUpgrade(UpgradeStack)} and {@link #removeUpgrade(UpgradeStack)}
 * respectively. The {@link UpgradeStack upgrades} will directly reflect on the returned {@link Trait} values and no post-processing
 * needed. For example instead of checking the base cost to mine some block and then adding the cost for every installed upgrade,
 * one can directly query {@code container.getTrait(Trait.ACTION_COST)} and get the appropriate value.
 * <p>
 * Serialisation will only serialize and deserialize the installed upgrades. Traits will not be affected (as they aren't even serializable).
 */
public interface ITraitContainer extends INBTSerializable<CompoundNBT> {
    <T> Optional<T> getTrait(Trait<T> trait);

    Set<UpgradeStack> listTiers();

    Set<Trait<?>> listTraits();

    default Set<Upgrade> listUpgrades() {
        return listTiers().stream().map(UpgradeStack::getUpgrade).collect(Collectors.toSet());
    }

    boolean installUpgrade(UpgradeStack upgrade);

    boolean removeUpgrade(UpgradeStack upgrade);

    CompoundNBT serializeNBT(boolean persistend);

    @Override
    default CompoundNBT serializeNBT() {
        return serializeNBT(true);
    }
}
