package com.advanced.baby.chicken.client.mixin;

import com.advanced.baby.chicken.DirtyBabyChicken;
import com.advanced.baby.chicken.SleepingBabyChicken;
import com.advanced.baby.chicken.client.DirtyBabyChickenLayer;
import com.advanced.baby.chicken.client.DirtyChickenRenderState;
import com.advanced.baby.chicken.client.SleepingChickenRenderState;
import net.minecraft.client.model.animal.chicken.ChickenModel;
import net.minecraft.client.renderer.entity.ChickenRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.ChickenRenderState;
import net.minecraft.world.entity.animal.chicken.Chicken;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to extract sleeping and dirty state from chicken entity into render state,
 * and to add the dirty overlay render layer.
 */
@Mixin(ChickenRenderer.class)
public abstract class ChickenRendererMixin extends MobRenderer<Chicken, ChickenRenderState, ChickenModel> {

    protected ChickenRendererMixin(EntityRendererProvider.Context context, ChickenModel model, float shadowRadius) {
        super(context, model, shadowRadius);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void addDirtyLayer(EntityRendererProvider.Context context, CallbackInfo ci) {
        // Add the dirty overlay layer to the chicken renderer
        this.addLayer(new DirtyBabyChickenLayer((ChickenRenderer) (Object) this));
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/animal/chicken/Chicken;Lnet/minecraft/client/renderer/entity/state/ChickenRenderState;F)V", at = @At("TAIL"))
    private void extractCustomState(Chicken entity, ChickenRenderState state, float partialTicks, CallbackInfo ci) {
        // Extract sleeping state
        if (entity instanceof SleepingBabyChicken sleepingChicken && state instanceof SleepingChickenRenderState sleepingState) {
            sleepingState.advancedBabyChicken$setSleepingOnBed(sleepingChicken.advancedBabyChicken$isSleepingOnBed());
        }

        // Extract dirty state
        if (entity instanceof DirtyBabyChicken dirtyChicken && state instanceof DirtyChickenRenderState dirtyState) {
            dirtyState.advancedBabyChicken$setDirty(dirtyChicken.advancedBabyChicken$isDirty());
        }
    }
}
