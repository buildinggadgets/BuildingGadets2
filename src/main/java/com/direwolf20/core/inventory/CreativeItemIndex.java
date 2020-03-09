package com.direwolf20.core.inventory;

import java.util.Set;

public final class CreativeItemIndex extends MultiBindItemIndex {

    public CreativeItemIndex() {
        super();
    }

    CreativeItemIndex(Set<InventoryLink> boundInventories) {
        super(boundInventories);
    }

    @Override
    public boolean reIndex() {
        return true;
    }

    @Override
    public IItemCache copyCache() {
        return this;
    }

    @Override
    public IBulkItemTransaction bulkTransaction() {
        return new IBulkItemTransaction() {
            @Override
            public CommitResult commit() {
                return CommitResult.NO_ACTION;
            }

            @Override
            public int extractItem(IndexKey key, int count, boolean simulate) {
                return count;
            }

            @Override
            public int insertItem(IndexKey key, int count, boolean simulate) {
                return count;
            }

            @Override
            public IItemCache copyCache() {
                return this;
            }
        };
    }

    @Override
    public int extractItem(IndexKey key, int count, boolean simulate) {
        return count;
    }

    @Override
    public int insertItem(IndexKey key, int count, boolean simulate) {
        return count;
    }

    @Override
    public boolean updateIndex() {
        return false;
    }
}
