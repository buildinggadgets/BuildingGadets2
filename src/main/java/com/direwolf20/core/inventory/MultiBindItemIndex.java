package com.direwolf20.core.inventory;

import java.util.*;

public abstract class MultiBindItemIndex implements IItemIndex {
    private final Set<InventoryLink> boundInventories;

    public MultiBindItemIndex() {
        this(Collections.emptySet());
    }

    MultiBindItemIndex(Set<InventoryLink> boundInventories) {
        this.boundInventories = new LinkedHashSet<>(boundInventories);
    }

    protected Set<InventoryLink> getBoundInventories() {
        return boundInventories;
    }

    @Override
    public boolean reIndex() {
        for (InventoryLink link : boundInventories)
            link.getIndex().ifPresent(IItemIndex::reIndex);
        return true;
    }

    @Override
    public BindingResult bind(InventoryLink other) {
        boundInventories.add(other);
        return BindingResult.BIND;
    }

    @Override
    public boolean unbind(InventoryLink other) {
        return boundInventories.remove(other);
    }

    @Override
    public List<InventoryLink> boundLinks() {
        return new ArrayList<>(boundInventories);
    }
}
