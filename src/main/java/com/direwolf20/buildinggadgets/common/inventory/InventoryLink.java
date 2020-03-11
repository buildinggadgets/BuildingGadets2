package com.direwolf20.buildinggadgets.common.inventory;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.dimension.DimensionType;

import java.lang.ref.WeakReference;
import java.util.Optional;

public final class InventoryLink {
    private final DimensionType dim;
    private final BlockPos position;
    private WeakReference<IWorld> world;

    private InventoryLink(BlockPos position, IWorld world) {
        this.position = position;
        this.dim = world.getDimension().getType();
        this.world = new WeakReference<>(world);
    }

    private InventoryLink(BlockPos position, DimensionType dim) {
        this.position = position;
        this.dim = dim;
    }

    public Optional<IItemIndex> getIndex() {
        return Optional.empty();
    }
}
