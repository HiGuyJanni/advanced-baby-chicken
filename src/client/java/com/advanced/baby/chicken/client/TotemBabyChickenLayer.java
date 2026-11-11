package com.advanced.baby.chicken.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.animal.chicken.ChickenModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.ChickenRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

/**
 * Render layer that makes a baby chicken hold an (oversized) totem of undying
 * upright at its side. The totem is attached to the wing, so it moves with it.
 */
public class TotemBabyChickenLayer extends RenderLayer<ChickenRenderState, ChickenModel> {

    // Tuning knobs. Offsets are in model pixels (1/16 block) relative to the wing joint.
    // For reference: the baby chicken's body is only 4 pixels wide/tall.

    /** Totem size in blocks. The sprite is mostly filled, so 0.6 is about twice the chicken's height. */
    private static final float TOTEM_SCALE = 0.6F;
    /** How far the totem's centre sits out from the chicken's side. */
    private static final float OFFSET_OUT = 3.5F;
    /** How far the totem's centre sits above the wing. */
    private static final float OFFSET_UP = 2.0F;
    /** How far the totem sits towards the chicken's front. */
    private static final float OFFSET_FORWARD = 0.5F;
    /** Turn around the vertical axis: 0 = faces forward, 90 = flat against the chicken's side. */
    private static final float YAW_DEGREES = 30.0F;
    /** Sideways lean, so it looks like the chicken is struggling to hold it. */
    private static final float LEAN_DEGREES = 12.0F;

    public TotemBabyChickenLayer(RenderLayerParent<ChickenRenderState, ChickenModel> renderer) {
        super(renderer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords,
                       ChickenRenderState state, float yRot, float xRot) {
        if (!state.isBaby || state.isInvisible) {
            return;
        }

        if (!(state instanceof TotemChickenRenderState totemState) || !totemState.advancedBabyChicken$hasTotem()) {
            return;
        }

        ItemStackRenderState totem = totemState.advancedBabyChicken$getTotemRenderState();
        if (totem.isEmpty()) {
            return;
        }

        // The wing on the +X side of the baby model (vanilla names it "right_wing").
        ModelPart wing = this.getParentModel().root().getChild("right_wing");

        poseStack.pushPose();

        // Move to the wing joint and follow its rotation (flapping, sleeping pose).
        wing.translateAndRotate(poseStack);

        // Model space has Y pointing down and -Z pointing forward.
        poseStack.translate(OFFSET_OUT / 16.0F, -OFFSET_UP / 16.0F, -OFFSET_FORWARD / 16.0F);

        // Model space is upside down; the 180 degree turn stands the item upright.
        poseStack.rotateDegrees(Axis.ZP, 180.0F + LEAN_DEGREES);
        poseStack.rotateDegrees(Axis.YP, YAW_DEGREES);
        poseStack.scale(TOTEM_SCALE, TOTEM_SCALE, TOTEM_SCALE);

        totem.submit(
            poseStack,
            submitNodeCollector,
            lightCoords,
            LivingEntityRenderer.getOverlayCoords(state, 0.0F),
            state.outlineColor
        );

        poseStack.popPose();
    }
}
