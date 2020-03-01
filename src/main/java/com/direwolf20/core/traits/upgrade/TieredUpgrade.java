package com.direwolf20.core.traits.upgrade;


import com.direwolf20.core.registry.Registries;
import com.direwolf20.core.traits.Trait;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nonnull;
import java.util.function.UnaryOperator;

/**
 * Data class providing a level to an {@link Upgrade} as well as serialisation Methods.
 * @see Upgrade
 */
public final class TieredUpgrade  {
    private static final String KEY_UPGRADE_ID = "upgrade";
    private static final String KEY_TIER = "tier";
    @Nonnull
    private final Upgrade upgrade;
    private final int level;

    public static TieredUpgrade deserialize(CompoundNBT nbt) {
        int tier = nbt.getInt(KEY_TIER);
        Upgrade upgrade;
        if (nbt.contains(KEY_UPGRADE_ID, NBT.TAG_STRING))
            upgrade = Registries.getUpgradeRegistry().getValue(new ResourceLocation(nbt.getString(KEY_UPGRADE_ID)));
        else
            upgrade = Registries.getUpgradeRegistry().getValue(nbt.getInt(KEY_UPGRADE_ID));
        return new TieredUpgrade(upgrade, tier);
    }

    public TieredUpgrade(Upgrade upgrade, int level) {
        Preconditions.checkNotNull(upgrade);
        Preconditions.checkArgument(upgrade.getRegistryName()!=null && upgrade.isValidLevel(level), "Cannot have level "+ level +" for upgrade "+upgrade.getRegistryName());
        this.upgrade = upgrade;
        this.level = level;
    }

    @Nonnull
    public Upgrade getUpgrade() {
        return upgrade;
    }

    public int getLevel() {
        return level;
    }

    public <T> UnaryOperator<T> getModificatorFor(Trait<T> characteristic) {
        return upgrade.getModificatorFor(characteristic, this);
    }

    public ImmutableSet<Trait<?>> getAppliedModifications() {
        return getUpgrade().getAppliedModifications();
    }

    public CompoundNBT serializeNBT() {
        return serializeNBT(true);
    }

    public CompoundNBT serializeNBT(boolean persistent) {
        CompoundNBT compound = new CompoundNBT();
        assert getUpgrade().getRegistryName()!=null;
        if (persistent)
            compound.putString(KEY_UPGRADE_ID, getUpgrade().getRegistryName().toString());
        else
            compound.putInt(KEY_UPGRADE_ID, Registries.getUpgradeRegistry().getID(getUpgrade()));
        compound.putInt(KEY_TIER, level);
        return compound;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (! (o instanceof TieredUpgrade)) return false;

        final TieredUpgrade that = (TieredUpgrade) o;

        if (level != that.level) return false;
        return getUpgrade().equals(that.getUpgrade());
    }

    @Override
    public int hashCode() {
        int result = getUpgrade().hashCode();
        result = 31 * result + level;
        return result;
    }
}
