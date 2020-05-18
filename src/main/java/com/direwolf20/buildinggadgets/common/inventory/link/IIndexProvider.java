package com.direwolf20.buildinggadgets.common.inventory.link;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IWorld;

import javax.annotation.Nullable;

@FunctionalInterface
public interface IIndexProvider {
    @Nullable
    IValidatableItemIndex createIndex(IWorld world, BlockState state, @Nullable TileEntity tileEntity);
}
