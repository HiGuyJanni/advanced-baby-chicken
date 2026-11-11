package com.advanced.baby.chicken.client;

import net.minecraft.client.renderer.item.ItemStackRenderState;

/**
 * Interface for accessing totem state in ChickenRenderState.
 */
public interface TotemChickenRenderState {
    boolean advancedBabyChicken$hasTotem();
    void advancedBabyChicken$setHasTotem(boolean hasTotem);

    /** Per-chicken item render state for the held totem (empty when no totem). */
    ItemStackRenderState advancedBabyChicken$getTotemRenderState();
}
