package com.direwolf20.core.traits;


import com.direwolf20.core.registry.CoreRegistries;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nonnull;
import java.util.function.UnaryOperator;

public final class TieredUpgrade  {
    private static final String KEY_UPGRADE_ID = "upgrade";
    private static final String KEY_TIER = "tier";
    @Nonnull
    private final Upgrade upgrade;
    private final int tier;

    public static TieredUpgrade deserialize(CompoundNBT nbt) {
        int tier = nbt.getInt(KEY_TIER);
        Upgrade upgrade;
        if (nbt.contains(KEY_UPGRADE_ID, NBT.TAG_STRING))
            upgrade = CoreRegistries.getUpgradeRegistry().getValue(new ResourceLocation(nbt.getString(KEY_UPGRADE_ID)));
        else
            upgrade = CoreRegistries.getUpgradeRegistry().getValue(nbt.getInt(KEY_UPGRADE_ID));
        return new TieredUpgrade(upgrade, tier);
    }

    public TieredUpgrade(Upgrade upgrade, int tier) {
        Preconditions.checkNotNull(upgrade);
        Preconditions.checkArgument(upgrade.getRegistryName()!=null && upgrade.isValidTier(tier), "Cannot have tier "+tier+" for upgrade "+upgrade.getRegistryName());
        this.upgrade = upgrade;
        this.tier = tier;
    }

    @Nonnull
    public Upgrade getUpgrade() {
        return upgrade;
    }

    public <T> UnaryOperator<T> getModificatorFor(Trait<T> characteristic) {
        return upgrade.getModificatorFor(characteristic, tier);
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
            compound.putInt(KEY_UPGRADE_ID, CoreRegistries.getUpgradeRegistry().getID(getUpgrade()));
        compound.putInt(KEY_TIER, tier);
        return compound;
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
