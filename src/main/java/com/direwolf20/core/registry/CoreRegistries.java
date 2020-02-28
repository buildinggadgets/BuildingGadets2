package com.direwolf20.core.registry;

import com.direwolf20.core.DireCore20;
import com.direwolf20.core.traits.Upgrade;
import com.google.common.base.Preconditions;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

@EventBusSubscriber(bus = Bus.MOD)
public enum CoreRegistries {
    ;
    public static final ResourceLocation UPGRADE_REGISTRY_NAME = new ResourceLocation(DireCore20.MODID+":upgrade_type");
    private static ForgeRegistry<Upgrade> UPGRADES = null;

    public static ForgeRegistry<Upgrade> getUpgradeRegistry() {
        Preconditions.checkState(UPGRADES!=null, "Attempted to access Upgrade-Registry before it could be created!");
        return UPGRADES;
    }

    public static void onCreateRegistries(RegistryEvent.NewRegistry event) {
        UPGRADES = (ForgeRegistry<Upgrade>) new RegistryBuilder<Upgrade>()
                .setName(UPGRADE_REGISTRY_NAME)
                .setDefaultKey(CoreUpgrades.UPGRADE_BLANK_RL)
                .create();
    }
}
