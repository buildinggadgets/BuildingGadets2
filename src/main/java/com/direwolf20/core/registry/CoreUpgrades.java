package com.direwolf20.core.registry;

import com.direwolf20.core.DireCore20;
import com.direwolf20.core.traits.Trait;
import com.direwolf20.core.traits.upgrade.Upgrade;
import com.google.common.collect.ImmutableSet;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

import java.util.function.UnaryOperator;

public enum CoreUpgrades {
    ;
    public static DeferredRegister<Upgrade> UPGRADES = null;
    public static RegistryObject<Upgrade> BLANK = null;
    //This is suboptimally solved by Forge - how would BG handle the Registry being created in the RegistryEvent.NewRegistry event?
    //I think it could therefore not use DeferredRegister for this
    public static void onUpgradesCreated() {
        UPGRADES = new DeferredRegister<>(CoreRegistries.getUpgradeRegistry(), DireCore20.MOD_ID);
        BLANK = UPGRADES.register(Upgrade.UPGRADE_BLANK_RL.getPath(), () -> new Upgrade(ImmutableSet.of()) {
            @Override
            protected boolean isValidTier(int tier) {
                return tier>=0;
            }

            @Override
            public <T> UnaryOperator<T> getModificatorFor(Trait<T> trait, int tier) {
                DireCore20.LOG.trace("Attempted to query unknown Trait {}", trait);
                return null;
            }
        });
    }
}
