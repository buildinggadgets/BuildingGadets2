package com.direwolf20.core.capability;

import com.direwolf20.core.DireCore20;
import com.direwolf20.core.capability.itemsync.CapabilityModificationSyncHelper;
import com.direwolf20.core.capability.itemsync.ICapabilitySyncHelper;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;

public enum SyncHelperCapability {
    ;
    @CapabilityInject(ICapabilitySyncHelper.class)
    public static Capability<ICapabilitySyncHelper> SYNC_HELPER_CAPABILITY = null;

    public static void register() {
        DireCore20.LOG.debug("Registering Sync Helper Capability");
        CapabilityManager.INSTANCE.register(ICapabilitySyncHelper.class, new IStorage<ICapabilitySyncHelper>() {
            @Nullable
            @Override
            public INBT writeNBT(Capability<ICapabilitySyncHelper> capability, ICapabilitySyncHelper instance, Direction side) {
                return null;
            }

            @Override
            public void readNBT(Capability<ICapabilitySyncHelper> capability, ICapabilitySyncHelper instance, Direction side, INBT nbt) {

            }
        }, CapabilityModificationSyncHelper::new);
        DireCore20.LOG.debug("Registered Sync HelperCapability");
    }
}
