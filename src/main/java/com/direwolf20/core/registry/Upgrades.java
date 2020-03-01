package com.direwolf20.core.registry;

import com.direwolf20.core.DireCore20;
import com.direwolf20.core.traits.upgrade.Upgrade;
import com.direwolf20.core.traits.upgrade.UpgradeBuilder;
import com.google.common.collect.Range;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public enum Upgrades {
    ;
    public static DeferredRegister<Upgrade> UPGRADES = null;
    public static RegistryObject<Upgrade> BLANK = null;
    //This is suboptimally solved by Forge - how would BG handle the Registry being created in the RegistryEvent.NewRegistry event?
    //I think it could therefore not use DeferredRegister for this
    public static void onUpgradesCreated() {
        UPGRADES = new DeferredRegister<>(Registries.getUpgradeRegistry(), DireCore20.MOD_ID);
        BLANK = UPGRADES.register(Upgrade.UPGRADE_BLANK_RL.getPath(), () -> UpgradeBuilder.create().build(Range.singleton(0)));
    }
}
