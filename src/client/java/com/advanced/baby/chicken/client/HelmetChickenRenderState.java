package com.advanced.baby.chicken.client;

import net.minecraft.world.item.ItemStack;

/**
 * Interface for accessing the worn helmet in ChickenRenderState.
 */
public interface HelmetChickenRenderState {
    ItemStack advancedBabyChicken$getHelmet();
    void advancedBabyChicken$setHelmet(ItemStack helmet);
}
