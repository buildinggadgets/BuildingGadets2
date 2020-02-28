package com.direwolf20.core.traits;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public interface ITraitContainer extends INBTSerializable<CompoundNBT> {
   <T> Optional<T> getCharacteristic(Trait<T> characteristic);

   Set<TieredUpgrade> listTiers();

   default Set<Upgrade> listUpgrades() {
      return listTiers().stream().map(TieredUpgrade::getUpgrade).collect(Collectors.toSet());
   }

   boolean installUpgrade(TieredUpgrade upgrade);

   boolean removeUpgrade(TieredUpgrade upgrade);
}
