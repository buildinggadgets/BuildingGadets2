package com.direwolf20.buildinggadgets.common.inventory.link;

import com.direwolf20.buildinggadgets.common.inventory.AbstractHandlerItemIndex;
import com.direwolf20.buildinggadgets.common.inventory.IItemIndex;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public enum HandlerIndexProvider implements IIndexProvider {
    INSTANCE;

    @Nullable
    @Override
    public IValidatableItemIndex createIndex(IWorld world, BlockState state, @Nullable TileEntity tileEntity) {
        if (tileEntity == null)
            return null;
        LazyOptional<IItemHandler> handlerOpt = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
        if (handlerOpt.isPresent())
            return new HandlerItemIndex(handlerOpt);
        return null;
    }

    private static final class HandlerItemIndex extends AbstractHandlerItemIndex implements IValidatableItemIndex {
        private final LazyOptional<IItemHandler> opt;
        private IndexCacheObject cache;
        private int lastUpdateIndex;

        public HandlerItemIndex(LazyOptional<IItemHandler> opt) {
            this.opt = opt;
            this.cache = new IndexCacheObject();
            this.lastUpdateIndex = 0;
        }

        @Override
        public boolean isValid() {
            return opt.isPresent();
        }

        @Override
        public boolean updateIndex(PlayerEntity player, int maxIndex) {
            int updateIndex = performIndex(player, lastUpdateIndex, maxIndex);
            lastUpdateIndex = updateIndex >= 0 ? updateIndex : lastUpdateIndex;
            return updateIndex >= 0;
        }

        @Override
        public BindingResult bind(InventoryLink other) {
            return BindingResult.NO_BIND;
        }

        @Override
        public boolean unbind(InventoryLink other) {
            return false;
        }

        @Override
        public List<InventoryLink> boundLinks() {
            return ImmutableList.of();
        }

        @Override
        protected Optional<IItemHandler> getHandler(PlayerEntity player) {
            if (opt.isPresent())
                return Optional.of(opt.orElseThrow(RuntimeException::new));
            return Optional.empty();
        }

        @Override
        protected IndexCacheObject getCache(PlayerEntity player) {
            return cache;
        }

        @Override
        protected Optional<IItemIndex> getBoundIndex(PlayerEntity player) {
            return Optional.empty();
        }
    }
}
