package com.direwolf20.buildinggadgets.common.inventory.link;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.inventory.IItemIndex;
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

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

public final class InventoryLink {
    private static final String KEY_DIM = "dim";
    private static final String KEY_POS = "pos";
    //We have very few index providers, which are only registered during parallel mod loading...
    //The fast <b>parallel</b> iteration in game, is much more important, then some low size array copies when the game is starting
    private static final List<IIndexProvider> registeredProviders = new CopyOnWriteArrayList<>();
    private final DimensionType dim;
    private final BlockPos position;
    private IValidatableItemIndex index;

    private InventoryLink(BlockPos position, DimensionType dim) {
        this.position = position;
        this.dim = dim;
        this.index = null;
    }

    public static void registerDefaultProviders() {
        registerIndexProvider(HandlerIndexProvider.INSTANCE);
    }

    /**
     * Registers a new IIndexProvider. The order in which this provider is called entirely depends on registration order.
     * If <b>some other mod<b/> happens to need to replace one of our own providers, we can revise this if we want to. Notice
     * that BuildingGadgets itself can safely ensure a order, by registering in the desired order.
     * <p>
     * This Method is safe to call during parallel mod loading.
     *
     * @param provider The provider to register
     */
    public static void registerIndexProvider(IIndexProvider provider) {
        registeredProviders.add(provider);
    }

    public static Optional<IValidatableItemIndex> indexIfAvailable(IWorld world, BlockPos position) {
        BlockState state = world.getBlockState(position);
        TileEntity tileEntity = world.getTileEntity(position);
        for (IIndexProvider provider : registeredProviders) {
            IValidatableItemIndex index = provider.createIndex(world, state, tileEntity);
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
        return index != null && index.isValid() ?
                Optional.of(index) :
                createIndex(world).map(Function.identity()); //map is because the types mismatch
    }

    //should only be called on the server...
    public CompoundNBT serializeNBT() {
        assert dim.getRegistryName() != null;
        CompoundNBT nbt = new CompoundNBT();
        nbt.put(KEY_POS, position.serialize(NBTDynamicOps.INSTANCE));
        nbt.putString(KEY_DIM, dim.getRegistryName().toString());
        return nbt;
    }

    private Optional<IValidatableItemIndex> createIndex(IWorld world) {
        if (world.getDimension().getType() == dim)
            return indexIfAvailable(world, position);
        index = null;
        return Optional.empty();
    }

}
