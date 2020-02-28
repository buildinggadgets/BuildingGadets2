package com.direwolf20.core;

import com.direwolf20.core.capability.TraitContainerCapability;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class DireCore20 {
    public static final String MODID = "direcore20";

    public static final Logger LOG = LogManager.getLogger();

    public DireCore20() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::setup);
    }

    private void setup(final FMLCommonSetupEvent event) {
        TraitContainerCapability.register();
    }
}
