package com.advanced.baby.chicken.client.mixin;

import com.advanced.baby.chicken.client.DirtyChickenRenderState;
import com.advanced.baby.chicken.client.SleepingChickenRenderState;
import net.minecraft.client.renderer.entity.state.ChickenRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Mixin to add sleeping and dirty state to ChickenRenderState for client-side rendering.
 */
@Mixin(ChickenRenderState.class)
public class ChickenRenderStateMixin implements SleepingChickenRenderState, DirtyChickenRenderState {

    @Unique
    private boolean advancedBabyChicken$isSleepingOnBed = false;

    @Unique
    private boolean advancedBabyChicken$isDirty = false;

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
}
