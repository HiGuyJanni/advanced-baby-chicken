package com.advanced.baby.chicken.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.ChickenRenderState;

/**
 * The vanilla player helmet shape, shrunk to fit a baby chicken.
 *
 * A player's head is an 8x8x8 pixel cube and the baby chicken's whole body is a 4x4x4 cube,
 * so the helmet is scaled to exactly half size and sits on the chicken just like it sits on a player.
 * The model is positioned relative to the baby chicken's "body" part.
 */
public class BabyChickenHelmetModel extends EntityModel<ChickenRenderState> {

    /** Baby chicken body size divided by player head size. */
    private static final float HELMET_SCALE = 0.5F;

    // Where the bottom centre of the baby chicken's body cube is, relative to the body part's pivot.
    // (Body box: x -2..2, y -2.25..1.75, z -0.75..3.25 - see vanilla BabyChickenModel.)
    private static final float BODY_BOTTOM_Y = 1.75F;
    private static final float BODY_CENTER_Z = 1.25F;

    public BabyChickenHelmetModel() {
        super(createLayer().bakeRoot());
    }

    private static LayerDefinition createLayer() {
        // Same cubes, texture offsets and inflation as the helmet part of vanilla player armor
        CubeDeformation armorInflation = new CubeDeformation(1.0F);

        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild(
            "head",
            CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, armorInflation),
            PartPose.offset(0.0F, BODY_BOTTOM_Y, BODY_CENTER_Z).withScale(HELMET_SCALE)
        );
        head.addOrReplaceChild(
            "hat",
            CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, armorInflation.extend(0.5F)),
            PartPose.ZERO
        );
        return LayerDefinition.create(mesh, 64, 32);
    }
}
