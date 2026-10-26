package com.advanced.baby.chicken.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

/**
 * Generates and registers the sleeping eyes overlay texture for baby chickens.
 * The texture shows closed eyes (horizontal lines) to indicate sleeping.
 */
public class SleepingEyesTextureGenerator {

    public static final Identifier SLEEPING_EYES_TEXTURE =
        Identifier.fromNamespaceAndPath("advanced-baby-chicken", "textures/entity/chicken/baby_sleeping_eyes");

    private static boolean textureRegistered = false;

    /**
     * Registers the sleeping eyes texture dynamically.
     * Creates a 16x16 texture with closed eye lines matching the baby chicken texture layout.
     */
    public static void registerTexture() {
        if (textureRegistered) return;

        // Create 16x16 texture (same size as baby chicken texture)
        NativeImage image = new NativeImage(16, 16, true);

        // Baby chicken body texture layout (from BabyChickenModel):
        // body: texOffs(0, 0) - 4x4x4 cube
        // beak: texOffs(10, 8) - 2x1x1 cube
        // The front face of the body (where eyes would be) is at the front of the 4x4x4 cube

        // Draw closed eyes - black horizontal lines
        // ARGB format: 0xAARRGGBB, need to convert to ABGR for NativeImage
        int black = ARGB.toABGR(0xFF000000); // Fully opaque black

        // The face area on the texture - draw "closed eye" lines
        // Left eye area (small horizontal line)
        image.setPixelABGR(5, 2, black);
        image.setPixelABGR(6, 2, black);

        // Right eye area (small horizontal line)
        image.setPixelABGR(9, 2, black);
        image.setPixelABGR(10, 2, black);

        // Register the dynamic texture
        DynamicTexture dynamicTexture = new DynamicTexture(() -> "baby_chicken_sleeping_eyes", image);
        Minecraft.getInstance().getTextureManager().register(SLEEPING_EYES_TEXTURE, dynamicTexture);

        textureRegistered = true;
    }
}
