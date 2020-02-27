package com.direwolf20.buildinggadgets.core.characteristics;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public interface ICharacteristicContainer extends INBTSerializable<CompoundNBT> {
   <T> Optional<T> getCharacteristic(Characteristic<T> characteristic);

   Set<TieredUpgrade> listTiers();

   default Set<Upgrade> listUpgrades() {
      return listTiers().stream().map(TieredUpgrade::getUpgrade).collect(Collectors.toSet());
   }


   boolean installUpgrade(TieredUpgrade upgrade);

   boolean removeUpgrade(TieredUpgrade upgrade);
}
