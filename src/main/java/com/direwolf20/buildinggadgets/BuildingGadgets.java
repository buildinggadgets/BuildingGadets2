package com.direwolf20.buildinggadgets;

import com.direwolf20.buildinggadgets.common.items.BGItems;
import com.direwolf20.core.DireCore20;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(BuildingGadgets.MOD_ID)
public class BuildingGadgets
{
    public static final String MOD_ID = "buildinggadgets";
    public static final Logger LOG = LogManager.getLogger();

    public BuildingGadgets() {
        //load the core from within here for now...
        new DireCore20();
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::setup);
        bus.addListener(this::clientSetup);
        BGItems.ITEMS.register(bus);

        // Todo: remove once events have their own classes
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {

    }

    private void clientSetup(final FMLClientSetupEvent event) {

    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
    }
}
