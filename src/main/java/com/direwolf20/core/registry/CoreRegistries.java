package com.direwolf20.core.registry;

import com.direwolf20.core.DireCore20;
import com.direwolf20.core.traits.upgrade.Upgrade;
import com.google.common.base.Preconditions;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

@EventBusSubscriber(bus = Bus.MOD)
public enum CoreRegistries {
    ;
    public static final ResourceLocation UPGRADE_REGISTRY_NAME = new ResourceLocation(DireCore20.MOD_ID +":upgrade_type");
    private static ForgeRegistry<Upgrade> UPGRADES = null;

    public static ForgeRegistry<Upgrade> getUpgradeRegistry() {
        Preconditions.checkState(UPGRADES!=null, "Attempted to access Upgrade-Registry before it could be created!");
        return UPGRADES;
    }

    @SubscribeEvent
    public static void onCreateRegistries(RegistryEvent.NewRegistry event) {
        DireCore20.LOG.debug("Creating Registries");
        UPGRADES = (ForgeRegistry<Upgrade>) new RegistryBuilder<Upgrade>()
                .setType(Upgrade.class)
                .setName(UPGRADE_REGISTRY_NAME)
                .setDefaultKey(Upgrade.UPGRADE_BLANK_RL)
                .create();
        CoreUpgrades.onUpgradesCreated();
        DireCore20.LOG.debug("Registry creation finished");
    }
}
