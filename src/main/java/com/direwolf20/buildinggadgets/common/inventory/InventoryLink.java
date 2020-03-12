package com.direwolf20.buildinggadgets.common.inventory;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.mojang.datafixers.Dynamic;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public final class InventoryLink {
    private static final String KEY_DIM = "dim";
    private static final String KEY_POS = "pos";
    private static final List<IIndexProvider> registeredProviders = new LinkedList<>();
    private final DimensionType dim;
    private final BlockPos position;
    private IItemIndex index;

    private InventoryLink(BlockPos position, DimensionType dim) {
        this.position = position;
        this.dim = dim;
        this.index = null;
    }

    public static synchronized void registerIndexProvider(IIndexProvider provider) {
        registeredProviders.add(provider);
    }

    public static Optional<IItemIndex> indexIfAvailable(IWorld world, BlockPos position) {
        BlockState state = world.getBlockState(position);
        TileEntity tileEntity = world.getTileEntity(position);
        for (IIndexProvider provider : registeredProviders) {
            IItemIndex index = provider.createIndex(world, state, tileEntity);
            if (index != null)
                return Optional.of(index);
        }
        return Optional.empty();
    }

    public static InventoryLink deserialize(CompoundNBT nbt) {
        assert nbt.contains(KEY_POS) && nbt.contains(KEY_DIM, NBT.TAG_STRING);
        BlockPos pos = BlockPos.deserialize(new Dynamic<>(NBTDynamicOps.INSTANCE, nbt.get(KEY_POS)));
        DimensionType dim = DimensionType.byName(new ResourceLocation(nbt.getString(KEY_DIM)));
        if (dim == null) {
            BuildingGadgets.LOG.trace("Failed to deserialize InventoryLink at {} in dimension {} from {}.",
                    pos, nbt.getString(KEY_DIM), nbt);
            return null;
        }
        return new InventoryLink(pos, dim);
    }

    public Optional<IItemIndex> getIndex(IWorld world) {
        //#IWantJava9
        return index != null ? Optional.of(index) : createIndex(world);
    }

    //should only be called on the server...
    public CompoundNBT serializeNBT() {
        assert dim.getRegistryName() != null;
        CompoundNBT nbt = new CompoundNBT();
        nbt.put(KEY_POS, position.serialize(NBTDynamicOps.INSTANCE));
        nbt.putString(KEY_DIM, dim.getRegistryName().toString());
        return nbt;
    }

    private Optional<IItemIndex> createIndex(IWorld world) {
        return world.getDimension().getType() == dim ? indexIfAvailable(world, position) : Optional.empty();
    }

    public interface IIndexProvider {
        @Nullable
        IItemIndex createIndex(IWorld world, BlockState state, @Nullable TileEntity tileEntity);
    }
}
