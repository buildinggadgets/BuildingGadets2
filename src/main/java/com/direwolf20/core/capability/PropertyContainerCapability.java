package com.direwolf20.core.capability;

import com.direwolf20.core.DireCore20;
import com.direwolf20.core.properties.IPropertyContainer;
import com.direwolf20.core.properties.PropertyContainer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;

public enum PropertyContainerCapability {
    ;
    @CapabilityInject(IPropertyContainer.class)
    public static Capability<IPropertyContainer> PROPERTY_CONTAINER_CAPABILITY = null;

    public static void register() {
        DireCore20.LOG.debug("Registering Property Container Capability");
        CapabilityManager.INSTANCE.register(IPropertyContainer.class, new IStorage<IPropertyContainer>() {
            @Nullable
            @Override
            public INBT writeNBT(Capability<IPropertyContainer> capability, IPropertyContainer instance, Direction side) {
                return instance.serializeNBT();
            }

            @Override
            public void readNBT(Capability<IPropertyContainer> capability, IPropertyContainer instance, Direction side, INBT nbt) {
                instance.deserializeNBT((CompoundNBT) nbt);
            }
        }, () -> PropertyContainer.builder().build());
        DireCore20.LOG.debug("Registered Property Container Capability");
    }
}
