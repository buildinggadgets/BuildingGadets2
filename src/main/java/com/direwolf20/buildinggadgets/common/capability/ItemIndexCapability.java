package com.direwolf20.buildinggadgets.common.capability;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.inventory.IItemIndex;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;

public enum ItemIndexCapability {
    ;
    @CapabilityInject(IItemIndex.class)
    public static Capability<IItemIndex> ITEM_INDEX_CAPABILITY = null;

    public static void register() {
        BuildingGadgets.LOG.debug("Registering IItemIndex capability");
        CapabilityManager.INSTANCE.register(IItemIndex.class, new IStorage<IItemIndex>() {
            @Nullable
            @Override
            public INBT writeNBT(Capability<IItemIndex> capability, IItemIndex instance, Direction side) {
                return null;
            }

            @Override
            public void readNBT(Capability<IItemIndex> capability, IItemIndex instance, Direction side, INBT nbt) {

            }
        }, () -> null);
        BuildingGadgets.LOG.debug("Registered IItemIndex capability");
    }
}
