package com.advanced.baby.chicken.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.animal.chicken.ChickenModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.ChickenRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.UvMapping;

/**
 * Render layer that adds a dirty mud overlay on baby chickens when they are dirty.
 */
public class DirtyBabyChickenLayer extends RenderLayer<ChickenRenderState, ChickenModel> {

    public DirtyBabyChickenLayer(RenderLayerParent<ChickenRenderState, ChickenModel> renderer) {
        super(renderer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords,
                       ChickenRenderState state, float yRot, float xRot) {
        // Only render for dirty baby chickens
        if (!state.isBaby) {
            return;
        }

        // Check if the chicken is dirty using our interface
        if (!(state instanceof DirtyChickenRenderState dirtyState) || !dirtyState.advancedBabyChicken$isDirty()) {
            return;
        }

        // Don't render if invisible
        if (state.isInvisible) {
            return;
        }

        // Ensure texture is registered
        DirtyChickenTextureGenerator.registerTexture();

        // Render the dirty overlay on top of the chicken model
        // Use a slightly brownish tint color (ARGB: semi-transparent brown)
        int dirtyTintColor = 0xFFFFFFFF; // White = no tint, let texture colors show through

        submitNodeCollector.order(1).submitModel(
            this.getParentModel(),
            state,
            poseStack,
            RenderTypes.entityTranslucent(DirtyChickenTextureGenerator.DIRTY_OVERLAY_TEXTURE),
            lightCoords,
            LivingEntityRenderer.getOverlayCoords(state, 0.0F),
            dirtyTintColor,
            (UvMapping) null,
            state.outlineColor
        );
    }
}
