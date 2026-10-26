package com.advanced.baby.chicken;

/**
 * Interface to track whether a baby chicken is dirty.
 * Dirty baby chickens move slower, only eat baked potatoes, and need water to clean.
 * This interface is implemented by ChickenMixin to add dirty state tracking.
 */
public interface DirtyBabyChicken {
    /**
     * Returns true if the baby chicken is currently dirty.
     */
    boolean advancedBabyChicken$isDirty();

    /**
     * Sets the dirty state of the baby chicken.
     */
    void advancedBabyChicken$setDirty(boolean dirty);

    /**
     * Gets the dirty tick counter (how long the chicken has been accumulating dirt).
     */
    int advancedBabyChicken$getDirtyTicks();

    /**
     * Sets the dirty tick counter.
     */
    void advancedBabyChicken$setDirtyTicks(int ticks);
}
