package com.direwolf20.buildinggadgets.common.items;

public class BuildingGadget extends Gadget {
    @Override
    protected int getMaxEnergy() {
        return 1000;
    }

    @Override
    protected int getMaxReceive() {
        return 100;
    }

    @Override
    protected int getMaxExtract() {
        return 100;
    }
}
