package com.advanced.baby.chicken;

/**
 * Interface to track whether a baby chicken is currently sleeping on a bed.
 * This interface is implemented by ChickenMixin to add sleeping state tracking.
 */
public interface SleepingBabyChicken {
    /**
     * Returns true if the baby chicken is currently sleeping on a bed.
     */
    boolean advancedBabyChicken$isSleepingOnBed();

    /**
     * Sets the sleeping state of the baby chicken.
     */
    void advancedBabyChicken$setSleepingOnBed(boolean sleeping);
}
