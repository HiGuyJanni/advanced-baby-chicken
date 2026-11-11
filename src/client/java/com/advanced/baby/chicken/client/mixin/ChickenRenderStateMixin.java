package com.advanced.baby.chicken.client.mixin;

import com.advanced.baby.chicken.client.DirtyChickenRenderState;
import com.advanced.baby.chicken.client.HelmetChickenRenderState;
import com.advanced.baby.chicken.client.SleepingChickenRenderState;
import com.advanced.baby.chicken.client.TotemChickenRenderState;
import net.minecraft.client.renderer.entity.state.ChickenRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Mixin to add sleeping, dirty, totem, and helmet state to ChickenRenderState for client-side rendering.
 */
@Mixin(ChickenRenderState.class)
public class ChickenRenderStateMixin implements SleepingChickenRenderState, DirtyChickenRenderState, TotemChickenRenderState, HelmetChickenRenderState {

    @Unique
    private boolean advancedBabyChicken$isSleepingOnBed = false;

    @Unique
    private boolean advancedBabyChicken$isDirty = false;

    @Unique
    private boolean advancedBabyChicken$hasTotem = false;

    @Unique
    private final ItemStackRenderState advancedBabyChicken$totemRenderState = new ItemStackRenderState();

    @Override
    public boolean advancedBabyChicken$isSleepingOnBed() {
        return advancedBabyChicken$isSleepingOnBed;
    }

    @Override
    public void advancedBabyChicken$setSleepingOnBed(boolean sleeping) {
        this.advancedBabyChicken$isSleepingOnBed = sleeping;
    }

    @Override
    public boolean advancedBabyChicken$isDirty() {
        return advancedBabyChicken$isDirty;
    }

    @Override
    public void advancedBabyChicken$setDirty(boolean dirty) {
        this.advancedBabyChicken$isDirty = dirty;
    }

    @Override
    public boolean advancedBabyChicken$hasTotem() {
        return advancedBabyChicken$hasTotem;
    }

    @Override
    public void advancedBabyChicken$setHasTotem(boolean hasTotem) {
        this.advancedBabyChicken$hasTotem = hasTotem;
    }

    @Override
    public ItemStackRenderState advancedBabyChicken$getTotemRenderState() {
        return advancedBabyChicken$totemRenderState;
    }

    @Unique
    private ItemStack advancedBabyChicken$helmet = ItemStack.EMPTY;

    @Override
    public ItemStack advancedBabyChicken$getHelmet() {
        return advancedBabyChicken$helmet;
    }

    @Override
    public void advancedBabyChicken$setHelmet(ItemStack helmet) {
        this.advancedBabyChicken$helmet = helmet;
    }
}
