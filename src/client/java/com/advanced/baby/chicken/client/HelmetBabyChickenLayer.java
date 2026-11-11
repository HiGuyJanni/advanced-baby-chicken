package com.advanced.baby.chicken.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.animal.chicken.ChickenModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.ChickenRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;

/**
 * Render layer that draws the helmet a baby chicken is wearing.
 * Uses the vanilla armor renderer, so dyed leather, armor trims and enchantment glint all work.
 */
public class HelmetBabyChickenLayer extends RenderLayer<ChickenRenderState, ChickenModel> {

    private final BabyChickenHelmetModel helmetModel = new BabyChickenHelmetModel();
    private final EquipmentLayerRenderer equipmentRenderer;

    public HelmetBabyChickenLayer(RenderLayerParent<ChickenRenderState, ChickenModel> renderer, EquipmentLayerRenderer equipmentRenderer) {
        super(renderer);
        this.equipmentRenderer = equipmentRenderer;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords,
                       ChickenRenderState state, float yRot, float xRot) {
        if (!state.isBaby || state.isInvisible) {
            return;
        }

        if (!(state instanceof HelmetChickenRenderState helmetState)) {
            return;
        }

        ItemStack helmet = helmetState.advancedBabyChicken$getHelmet();
        Equippable equippable = helmet.get(DataComponents.EQUIPPABLE);
        if (equippable == null || equippable.slot() != EquipmentSlot.HEAD || equippable.assetId().isEmpty()) {
            return;
        }

        // The baby chicken has no separate head - its body cube is its head.
        ModelPart body = this.getParentModel().root().getChild("body");

        poseStack.pushPose();

        // Follow the body, so the helmet stays on when the chicken flips over to sleep
        body.translateAndRotate(poseStack);

        this.equipmentRenderer.renderLayers(
            EquipmentClientInfo.LayerType.HUMANOID,
            equippable.assetId().get(),
            this.helmetModel,
            state,
            helmet,
            poseStack,
            submitNodeCollector,
            lightCoords,
            state.outlineColor
        );

        poseStack.popPose();
    }
}
