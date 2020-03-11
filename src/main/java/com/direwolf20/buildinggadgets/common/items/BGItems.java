package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public enum BGItems {
    ;
    public static final ItemGroup ITEM_GROUP = new ItemGroup(BuildingGadgets.MOD_ID) {
        @Override
        public ItemStack createIcon() {
            return BUILDING_GADGET.get().getDefaultInstance();
        }
    };

    public static final DeferredRegister<Item> ITEMS = new DeferredRegister<>(ForgeRegistries.ITEMS, BuildingGadgets.MOD_ID);
    public static final RegistryObject<BuildingGadget> BUILDING_GADGET = ITEMS.register("building_gadget", BuildingGadget::new);
}
