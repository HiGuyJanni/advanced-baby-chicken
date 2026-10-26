package com.advanced.baby.chicken.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

/**
 * Generates and registers the dirty overlay texture for baby chickens.
 * The texture shows brown mud spots scattered across the chicken's body.
 */
public class DirtyChickenTextureGenerator {

    public static final Identifier DIRTY_OVERLAY_TEXTURE =
        Identifier.fromNamespaceAndPath("advanced-baby-chicken", "textures/entity/chicken/baby_dirty_overlay");

    private static boolean textureRegistered = false;

    /**
     * Registers the dirty overlay texture dynamically.
     * Creates a 16x16 texture with semi-transparent brown mud spots.
     */
    public static void registerTexture() {
        if (textureRegistered) return;

        // Create 16x16 texture (same size as baby chicken texture)
        NativeImage image = new NativeImage(16, 16, true);

        // Brown/mud colors (ABGR format for NativeImage)
        // Different shades of brown for variety
        int darkBrown = toABGR(0xCC, 0x4A, 0x2E, 0x1A);    // Dark mud
        int mediumBrown = toABGR(0xBB, 0x5C, 0x3D, 0x22);  // Medium mud
        int lightBrown = toABGR(0xAA, 0x6B, 0x4A, 0x2A);   // Light mud

        // Baby chicken body texture layout:
        // body: texOffs(0, 0) - 4x4x4 cube, uses roughly 0-12 x, 0-8 y area
        // We'll add mud spots on the body area

        // Scattered mud spots on the body (top face area ~0-8 x, 0-4 y)
        image.setPixelABGR(1, 1, darkBrown);
        image.setPixelABGR(2, 1, mediumBrown);

        image.setPixelABGR(5, 2, darkBrown);
        image.setPixelABGR(6, 2, mediumBrown);
        image.setPixelABGR(6, 3, lightBrown);

        image.setPixelABGR(3, 3, mediumBrown);

        // Side face spots (around 4-8 x, 4-8 y area)
        image.setPixelABGR(5, 5, darkBrown);
        image.setPixelABGR(6, 5, mediumBrown);

        image.setPixelABGR(2, 6, lightBrown);
        image.setPixelABGR(3, 6, darkBrown);

        image.setPixelABGR(7, 6, mediumBrown);
        image.setPixelABGR(7, 7, lightBrown);

        // Front face spots
        image.setPixelABGR(9, 2, darkBrown);
        image.setPixelABGR(10, 3, mediumBrown);

        image.setPixelABGR(11, 5, lightBrown);
        image.setPixelABGR(12, 5, darkBrown);
        image.setPixelABGR(12, 6, mediumBrown);

        // Bottom area spots
        image.setPixelABGR(1, 7, darkBrown);
        image.setPixelABGR(4, 7, mediumBrown);

        image.setPixelABGR(9, 7, lightBrown);
        image.setPixelABGR(10, 7, darkBrown);

        // Register the dynamic texture
        DynamicTexture dynamicTexture = new DynamicTexture(() -> "baby_chicken_dirty_overlay", image);
        Minecraft.getInstance().getTextureManager().register(DIRTY_OVERLAY_TEXTURE, dynamicTexture);

        textureRegistered = true;
    }

    /**
     * Convert ARGB components to ABGR format used by NativeImage.
     */
    private static int toABGR(int alpha, int red, int green, int blue) {
        return (alpha << 24) | (blue << 16) | (green << 8) | red;
    }
}
