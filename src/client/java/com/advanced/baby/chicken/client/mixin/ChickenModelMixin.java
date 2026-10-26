package com.advanced.baby.chicken.client.mixin;

import com.advanced.baby.chicken.client.SleepingChickenRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.animal.chicken.ChickenModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.ChickenRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to add sleeping animation to chicken models.
 * When sleeping, the baby chicken flips upside down, balancing on its head!
 */
@Mixin(ChickenModel.class)
public abstract class ChickenModelMixin extends EntityModel<ChickenRenderState> {

    @Unique
    private ModelPart advancedBabyChicken$body;
    @Unique
    private ModelPart advancedBabyChicken$leftLeg;
    @Unique
    private ModelPart advancedBabyChicken$rightLeg;
    @Unique
    private ModelPart advancedBabyChicken$leftWing;
    @Unique
    private ModelPart advancedBabyChicken$rightWing;

    public ChickenModelMixin(ModelPart root) {
        super(root);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void captureModelParts(ModelPart root, CallbackInfo ci) {
        this.advancedBabyChicken$body = root.getChild("body");
        this.advancedBabyChicken$leftLeg = root.getChild("left_leg");
        this.advancedBabyChicken$rightLeg = root.getChild("right_leg");
        this.advancedBabyChicken$leftWing = root.getChild("left_wing");
        this.advancedBabyChicken$rightWing = root.getChild("right_wing");
    }

    @Inject(method = "setupAnim", at = @At("TAIL"))
    private void applySleepingAnimation(ChickenRenderState state, CallbackInfo ci) {
        if (state instanceof SleepingChickenRenderState sleepingState && sleepingState.advancedBabyChicken$isSleepingOnBed()) {
            // Only apply sleeping animation to baby chickens
            if (state.isBaby) {
                // Flip the entire chicken upside down!
                // Pi radians = 180 degrees = completely upside down
                float upsideDown = (float) Math.PI;

                if (advancedBabyChicken$body != null) {
                    // Flip the body upside down
                    advancedBabyChicken$body.xRot = upsideDown;
                    // Move body up so the head touches the bed surface
                    // Baby chicken body is at y=20.25, need to adjust for flip
                    advancedBabyChicken$body.y = 24.0F;
                }

                if (advancedBabyChicken$leftLeg != null) {
                    // Flip legs and make them stick up in the air, slightly spread
                    advancedBabyChicken$leftLeg.xRot = upsideDown + 0.3F; // Slightly angled
                    advancedBabyChicken$leftLeg.y = 22.0F;
                    advancedBabyChicken$leftLeg.zRot = -0.2F; // Spread outward
                }

                if (advancedBabyChicken$rightLeg != null) {
                    // Flip legs and make them stick up in the air, slightly spread
                    advancedBabyChicken$rightLeg.xRot = upsideDown - 0.3F; // Slightly angled other way
                    advancedBabyChicken$rightLeg.y = 22.0F;
                    advancedBabyChicken$rightLeg.zRot = 0.2F; // Spread outward
                }

                if (advancedBabyChicken$leftWing != null) {
                    // Wings flopped out to the sides
                    advancedBabyChicken$leftWing.zRot = -0.8F;
                    advancedBabyChicken$leftWing.y = 23.0F;
                }

                if (advancedBabyChicken$rightWing != null) {
                    // Wings flopped out to the sides
                    advancedBabyChicken$rightWing.zRot = 0.8F;
                    advancedBabyChicken$rightWing.y = 23.0F;
                }
            }
        }
    }
}
