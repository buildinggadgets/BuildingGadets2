package com.direwolf20.core.inventory;

public interface IBulkItemTransaction extends IItemCache {
    CommitResult commit();

}
