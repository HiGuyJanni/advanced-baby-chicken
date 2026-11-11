package com.advanced.baby.chicken;

import net.minecraft.world.item.ItemStack;

/**
 * Interface to track whether a baby chicken is holding a totem of undying.
 * Baby chickens with a totem will be protected from death once, consuming the totem.
 * This interface is implemented by ChickenMixin to add totem state tracking.
 */
public interface TotemBabyChicken {
    /**
     * Returns true if the baby chicken is currently holding a totem.
     */
    boolean advancedBabyChicken$hasTotem();

    /**
     * Gets the totem item the baby chicken is holding.
     * Returns ItemStack.EMPTY if not holding a totem.
     */
    ItemStack advancedBabyChicken$getTotem();

    /**
     * Sets the totem item the baby chicken is holding.
     * Pass ItemStack.EMPTY to remove the totem.
     */
    void advancedBabyChicken$setTotem(ItemStack totem);
}
