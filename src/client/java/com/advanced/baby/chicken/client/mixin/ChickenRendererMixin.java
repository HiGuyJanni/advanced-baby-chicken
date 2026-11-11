package com.advanced.baby.chicken.client.mixin;

import com.advanced.baby.chicken.DirtyBabyChicken;
import com.advanced.baby.chicken.SleepingBabyChicken;
import com.advanced.baby.chicken.TotemBabyChicken;
import com.advanced.baby.chicken.client.DirtyBabyChickenLayer;
import com.advanced.baby.chicken.client.DirtyChickenRenderState;
import com.advanced.baby.chicken.client.HelmetBabyChickenLayer;
import com.advanced.baby.chicken.client.HelmetChickenRenderState;
import com.advanced.baby.chicken.client.SleepingChickenRenderState;
import com.advanced.baby.chicken.client.TotemBabyChickenLayer;
import com.advanced.baby.chicken.client.TotemChickenRenderState;
import net.minecraft.client.model.animal.chicken.ChickenModel;
import net.minecraft.client.renderer.entity.ChickenRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.ChickenRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to extract sleeping, dirty, and totem state from chicken entity into render state,
 * and to add the dirty overlay and totem render layers.
 */
@Mixin(ChickenRenderer.class)
public abstract class ChickenRendererMixin extends MobRenderer<Chicken, ChickenRenderState, ChickenModel> {

    protected ChickenRendererMixin(EntityRendererProvider.Context context, ChickenModel model, float shadowRadius) {
        super(context, model, shadowRadius);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void addCustomLayers(EntityRendererProvider.Context context, CallbackInfo ci) {
        // Add the dirty overlay layer to the chicken renderer
        this.addLayer(new DirtyBabyChickenLayer((ChickenRenderer) (Object) this));

        // Add the totem render layer
        this.addLayer(new TotemBabyChickenLayer((ChickenRenderer) (Object) this));

        // Add the helmet render layer
        this.addLayer(new HelmetBabyChickenLayer((ChickenRenderer) (Object) this, context.getEquipmentRenderer()));
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

        // Extract the worn helmet (vanilla head equipment slot)
        if (state instanceof HelmetChickenRenderState helmetState) {
            helmetState.advancedBabyChicken$setHelmet(entity.getItemBySlot(EquipmentSlot.HEAD).copy());
        }

        // Extract totem state
        if (entity instanceof TotemBabyChicken totemChicken && state instanceof TotemChickenRenderState totemState) {
            ItemStack totem = totemChicken.advancedBabyChicken$getTotem();
            totemState.advancedBabyChicken$setHasTotem(!totem.isEmpty());

            // Each chicken's render state carries its own totem item state
            ItemStackRenderState totemRenderState = totemState.advancedBabyChicken$getTotemRenderState();
            if (totem.isEmpty()) {
                totemRenderState.clear();
            } else {
                this.itemModelResolver.updateForLiving(totemRenderState, totem, ItemDisplayContext.FIXED, entity);
            }
        }
    }
}
