package com.buuz135.functionalstorage.client;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.block.tile.DrawerTile;
import com.buuz135.functionalstorage.inventory.BigInventoryHandler;
import com.buuz135.functionalstorage.item.ConfigurationToolItem;
import com.buuz135.functionalstorage.util.NumberUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;
import org.joml.Vector3f;


import java.util.Map;
import java.util.stream.IntStream;

public class DrawerRenderer extends BaseDrawerRenderer<DrawerTile> {
    private static final org.slf4j.Logger LOGGER = LogUtils.getLogger();

    private static final float TALL_Y = 8 / 16f;
    private static final float SHORT_Y_LOW = 4.5f / 16f;
    private static final float SHORT_Y_HIGH = SHORT_Y_LOW + 0.5f;
    private static final float TALL_TEXT_OFFSET = -6 / 16f;
    private static final float SHORT_TEXT_OFFSET = -2.5f / 16f;
    private static final float TALL_ITEMSCALE = 0.8f;
    private static final float SHORT_ITEMSCALE = 0.45f;


    private static final Map<FunctionalStorage.DrawerType, Vector3f[]> SLOT_CENTERS = Map.of(
            FunctionalStorage.DrawerType.X_1, new Vector3f[] {
                    new Vector3f(0.5f, TALL_Y, 0.0005f) },
            FunctionalStorage.DrawerType.X_2, new Vector3f[] {
                    new Vector3f(0.5f, SHORT_Y_LOW, 0.0005f),
                    new Vector3f(0.5f, SHORT_Y_HIGH, 0.0005f) },
            FunctionalStorage.DrawerType.X_4, new Vector3f[] {
                    new Vector3f(0.25f, SHORT_Y_LOW, 0.0005f),
                    new Vector3f(0.75f, SHORT_Y_LOW, 0.0005f),
                    new Vector3f(0.75f, SHORT_Y_HIGH, 0.0005f),
                    new Vector3f(0.25f, SHORT_Y_HIGH, 0.0005f) });

    @Override
    public final void renderItems(DrawerTile tile, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        var drawerType = tile.getDrawerType();
        var coords = SLOT_CENTERS.get(drawerType);
        final float scale = drawerType == FunctionalStorage.DrawerType.X_1 ? TALL_ITEMSCALE : SHORT_ITEMSCALE;
        BigInventoryHandler inventoryHandler = (BigInventoryHandler) tile.getStorage();

        IntStream.range(0, drawerType.getSlots()).forEach(i -> {
            matrixStack.pushPose();
            matrixStack.translate(coords[i].x, coords[i].y, 0.49 / 16f);
            ItemStack stack = inventoryHandler.getStoredStacks().get(i).getStack();
            var options = tile.getDrawerOptions();
            renderStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, stack, stack.getCount(), inventoryHandler.getSlotLimit(i), scale, options, tile.getLevel());

            if (options.isActive(ConfigurationToolItem.ConfigurationAction.TOGGLE_NUMBERS)) {
                matrixStack.translate(0f, drawerType == FunctionalStorage.DrawerType.X_1 ? TALL_TEXT_OFFSET : SHORT_TEXT_OFFSET, 0);
                renderText(matrixStack, bufferIn, combinedOverlayIn, Component.literal(ChatFormatting.WHITE + "" + NumberUtils.getFormatedBigNumber(stack.getCount())));
            }

            matrixStack.popPose();
        });
    }

    public static void renderIndicator(PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn, float progress, ControllableDrawerTile.DrawerOptions options) {
        var indicatiorValue = options.getAdvancedValue(ConfigurationToolItem.ConfigurationAction.INDICATOR);
        if (indicatiorValue != 0) {
            TextureAtlasSprite still = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(com.buuz135.functionalstorage.util.Utils.resourceLocation(FunctionalStorage.MOD_ID, "block/indicator"));
            VertexConsumer builder = bufferIn.getBuffer(RenderType.translucent());
            Matrix4f posMat = matrixStack.last().pose();
            float red = 1;
            float green = 1;
            float blue = 1;
            float alpha = 1;
            float x1 = -4 / 16F;
            float x2 = x1 + 0.5f;
            float y1 = -6.65F / 16F;
            float y2 = y1 + 1.25f / 16F;
            float z2 = 0;
            float bx1 = 0 / 16F;
            float bx2 = 8 / 16F;
            float bz1 = 0 / 16F;
            float bz2 = 2 / 16F;
            float u1 = still.getU(bx1);
            float u2 = still.getU(bx2);
            float v1 = still.getV(bz1);
            float v2 = still.getV(bz2);
            if (indicatiorValue != 3 ) { //HIDE IN MODE 3 NO BG
                builder.addVertex(posMat, x2, y1, z2).setColor(red, green, blue, alpha).setUv(u2, v1).setOverlay(combinedOverlayIn).setLight(combinedLightIn).setNormal(0f, 0f, 1f);
                builder.addVertex(posMat, x2, y2, z2).setColor(red, green, blue, alpha).setUv(u2, v2).setOverlay(combinedOverlayIn).setLight(combinedLightIn).setNormal(0f, 0f, 1f);
                builder.addVertex(posMat, x1, y2, z2).setColor(red, green, blue, alpha).setUv(u1, v2).setOverlay(combinedOverlayIn).setLight(combinedLightIn).setNormal(0f, 0f, 1f);
                builder.addVertex(posMat, x1, y1, z2).setColor(red, green, blue, alpha).setUv(u1, v1).setOverlay(combinedOverlayIn).setLight(combinedLightIn).setNormal(0f, 0f, 1f);
            }

            u2 = still.getU((bx2 * progress));
            x2 = x1 + 0.5f * progress;
            z2 = 0.0001f;
            v1 = still.getV(8 / 16F);
            v2 = still.getV(10 / 16F);
            if (indicatiorValue == 1 || progress >= 1) {
                builder.addVertex(posMat, x2, y1, z2).setColor(red, green, blue, alpha).setUv(u2, v1).setOverlay(combinedOverlayIn).setLight(combinedLightIn).setNormal(0f, 0f, 1f);
                builder.addVertex(posMat, x2, y2, z2).setColor(red, green, blue, alpha).setUv(u2, v2).setOverlay(combinedOverlayIn).setLight(combinedLightIn).setNormal(0f, 0f, 1f);
                builder.addVertex(posMat, x1, y2, z2).setColor(red, green, blue, alpha).setUv(u1, v2).setOverlay(combinedOverlayIn).setLight(combinedLightIn).setNormal(0f, 0f, 1f);
                builder.addVertex(posMat, x1, y1, z2).setColor(red, green, blue, alpha).setUv(u1, v1).setOverlay(combinedOverlayIn).setLight(combinedLightIn).setNormal(0f, 0f, 1f);
            }
        }
    }

    public static void renderStack(PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn, ItemStack stack, int amount, int maxAmount, float itemScale, ControllableDrawerTile.DrawerOptions options, Level level) {
        renderIndicator(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, Math.min(1, amount / (float) maxAmount), options);

        BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(stack, Minecraft.getInstance().level, null, 0);
        if (options.isActive(ConfigurationToolItem.ConfigurationAction.TOGGLE_RENDER)) {
            matrixStack.pushPose();

            if (model.isGui3d()) {
                matrixStack.scale(itemScale, itemScale, itemScale / 4f);
            } else {
                // Add fake item depth
                matrixStack.scale(itemScale * 0.5f, itemScale * 0.5f, 0.5f);
            }

        	Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, combinedLightIn, combinedOverlayIn, matrixStack, bufferIn, level,0);
            matrixStack.popPose();
        }
    }


    /* Thanks Mekanism */
    public static void renderText(PoseStack matrix, MultiBufferSource renderer, int overlayLight, Component text) {
        final var font = Minecraft.getInstance().font;
        final int textWidth = Math.max(font.width(text), 1);
        final float scale = 1 / 128F;

        matrix.pushPose();
        matrix.scale(-scale, -scale, 1f);

        font.drawInBatch(text.getVisualOrderText(), -textWidth / 2f, 0, overlayLight,
        false, matrix.last().pose(), renderer, Font.DisplayMode.NORMAL,  0, 0xF000F0);
        matrix.popPose();
    }
}
