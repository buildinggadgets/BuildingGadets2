package com.direwolf20.core.capability;

import com.direwolf20.core.DireCore20;
import com.direwolf20.core.traits.ITraitContainer;
import com.direwolf20.core.traits.Trait;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * A serializable {@link IEnergyStorage} implementation which pulls the values for {@link #getMaxEnergyStored()}, {@link #getMaxReceive()}
 * and {@link #getMaxExtract()} from an {@link ITraitContainer}. The Traits used for this may be defined via the provided
 * {@link #builder(ITraitContainer) builder}.Otherwise they'll default to {@link Trait#MAX_ENERGY}, {@link Trait#MAX_RECEIVE} and
 * {@link Trait#MAX_EXTRACT}. If a Trait is not present in the container, the represented value will default to 0.
 * <p>
 * This class implements {@link INBTSerializable<INBT>}, even though it only ever serializes {@link IntNBT} instances. This is to avoid future
 * breaking changes if we happen to change the nbt format.
 */
public final class TraitEnergyStorage implements IEnergyStorage, INBTSerializable<INBT> {
    private final Trait<Integer> maxEnergyStored;
    private final Trait<Integer> maxExtract;
    private final Trait<Integer> maxReceive;
    private final Consumer<TraitEnergyStorage> onChangeCallback;
    private final ITraitContainer traitContainer;
    private int energyStored;

    private TraitEnergyStorage(ITraitContainer traitContainer, Consumer<TraitEnergyStorage> onChangeCallback, Trait<Integer> maxEnergy, Trait<Integer> maxReceive, Trait<Integer> maxExtract, int energyStored) {
        this.traitContainer = traitContainer;
        this.onChangeCallback = onChangeCallback;
        this.maxEnergyStored = maxEnergy;
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
        this.energyStored = energyStored;
    }

    /**
     * Creates a new builder instance for initialising {@link TraitEnergyStorage} instances with non-default values.
     * Use this if you need to use different {@link Trait Traits} then {@link Trait#MAX_ENERGY}, {@link Trait#MAX_RECEIVE} and
     * {@link Trait#MAX_EXTRACT}.
     * <p>
     * For creating a default instance, use {@link #create(ITraitContainer)}. For creating a instance with just a different change callback
     * use {@link #createWithDefaultTraits(ITraitContainer, Runnable)} or {@link #createWithDefaultTraits(ITraitContainer, Consumer)}.
     *
     * @param container The container to back the {@link TraitEnergyStorage}. Must not be null.
     * @return A new {@link Builder}
     */
    public static Builder builder(ITraitContainer container) {
        return new Builder(container);
    }

    public static TraitEnergyStorage create(ITraitContainer container) {
        return builder(container).build();
    }

    public static TraitEnergyStorage createWithDefaultTraits(ITraitContainer container, Consumer<TraitEnergyStorage> onChangeCallback) {
        return builder(container).onChangeCallback(onChangeCallback).build();
    }

    public static TraitEnergyStorage createWithDefaultTraits(ITraitContainer container, Runnable onChangeCallback) {
        return builder(container).onChangeCallback(onChangeCallback).build();
    }

    public int getMaxExtract() {
        return traitContainer.getTrait(maxExtract).orElse(0);
    }

    public int getMaxReceive() {
        return traitContainer.getTrait(maxReceive).orElse(0);
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        maxReceive = MathHelper.clamp(maxReceive, 0, getMaxReceive());
        int energy = getEnergyStored();
        int maxEnergy = getMaxEnergyStored();
        int newEnergy = Math.min(energy + maxReceive, maxEnergy);

        updateEnergy(simulate, newEnergy);

        return newEnergy - energy;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        maxExtract = MathHelper.clamp(maxExtract, 0, getMaxExtract());
        int energy = getEnergyStored();
        int newEnergy = Math.max(energy - maxExtract, 0);

        updateEnergy(simulate, newEnergy);

        return newEnergy - energy;
    }

    @Override
    public int getEnergyStored() {
        return energyStored;
    }

    @Override
    public int getMaxEnergyStored() {
        return traitContainer.getTrait(maxEnergyStored).orElse(0);
    }

    @Override
    public boolean canExtract() {
        return getMaxExtract() > 0;
    }

    @Override
    public boolean canReceive() {
        return getMaxReceive() > 0;
    }

    @Override
    public INBT serializeNBT() {
        return IntNBT.valueOf(energyStored);
    }

    @Override
    public void deserializeNBT(INBT nbt) {
        if (nbt instanceof IntNBT)
            energyStored = ((IntNBT) nbt).getInt();
        else
            DireCore20.LOG.error("Assumed nbt data to be an instance of {}. This was not the case and therefore data could not be deserialized from {}.",
                    IntNBT.class.getName(), nbt);
    }

    private void updateEnergy(boolean simulate, int newEnergy) {
        if (! simulate) {
            energyStored = newEnergy;
            onChangeCallback.accept(this);
        }
    }

    /**
     * Utility Builder class, used for creating {@link TraitEnergyStorage} instances. This allows for easier chaining of all
     * the parameters, if they are needed.
     */
    public static final class Builder {
        private Trait<Integer> maxEnergy;
        private Trait<Integer> maxExtract;
        private Trait<Integer> maxReceive;
        private Consumer<TraitEnergyStorage> onChangeCallback;
        private ITraitContainer traitContainer;

        public Builder(ITraitContainer traitContainer) {
            traitContainer(traitContainer);
            this.onChangeCallback = s -> {};
            this.maxEnergy = Trait.MAX_ENERGY;
            this.maxReceive = Trait.MAX_RECEIVE;
            this.maxExtract = Trait.MAX_EXTRACT;
        }

        public Builder traitContainer(ITraitContainer traitContainer) {
            this.traitContainer = Objects.requireNonNull(traitContainer);
            return this;
        }

        public Builder onChangeCallback(Consumer<TraitEnergyStorage> onChangeCallback) {
            this.onChangeCallback = Objects.requireNonNull(onChangeCallback);
            return this;
        }

        public Builder onChangeCallback(Runnable onChangeCallback) {
            return onChangeCallback(s -> onChangeCallback.run());
        }

        public Builder maxEnergy(Trait<Integer> maxEnergy) {
            this.maxEnergy = maxEnergy;
            return this;
        }

        public Builder maxReceive(Trait<Integer> maxReceive) {
            this.maxReceive = maxReceive;
            return this;
        }

        public Builder maxExtract(Trait<Integer> maxExtract) {
            this.maxExtract = maxExtract;
            return this;
        }

        public TraitEnergyStorage build() {
            return new TraitEnergyStorage(traitContainer, onChangeCallback, maxEnergy, maxReceive, maxExtract, 0);
        }
    }
}
