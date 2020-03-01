package com.direwolf20.core.capability;

import com.direwolf20.core.DireCore20;
import com.direwolf20.core.traits.ITraitContainer;
import com.direwolf20.core.traits.TraitContainer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;

public final class TraitContainerCapability {
    @CapabilityInject(ITraitContainer.class)
    public static Capability<ITraitContainer> TRAIT_CONTAINER_CAPABILITY = null;

    public static void register() {
        DireCore20.LOG.debug("Registering Trait Container Capability");
        CapabilityManager.INSTANCE.register(ITraitContainer.class, new IStorage<ITraitContainer>() {
            @Nullable
            @Override
            public INBT writeNBT(Capability<ITraitContainer> capability, ITraitContainer instance, Direction side) {
                return instance.serializeNBT();
            }

            @Override
            public void readNBT(Capability<ITraitContainer> capability, ITraitContainer instance, Direction side, INBT nbt) {
                instance.deserializeNBT((CompoundNBT)nbt);
            }
        }, () -> TraitContainer.builder().build());
    }
}
