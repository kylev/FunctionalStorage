package com.buuz135.functionalstorage.client;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.block.tile.DrawerTile;
import com.buuz135.functionalstorage.inventory.BigInventoryHandler;
import com.buuz135.functionalstorage.item.ConfigurationToolItem;
import com.buuz135.functionalstorage.util.NumberUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static com.buuz135.functionalstorage.util.MathUtils.createTransformMatrix;

import java.util.Map;
import java.util.stream.IntStream;

public class DrawerRenderer extends BaseDrawerRenderer<DrawerTile> {
    private static final Map<FunctionalStorage.DrawerType, Vector4f[]> SLOT_TRANSFORMS = Map.of(
        FunctionalStorage.DrawerType.X_1, new Vector4f[]{new Vector4f(0.5f, 0.5f, 0.0005f, 0.015f)},
        FunctionalStorage.DrawerType.X_2, new Vector4f[]{new Vector4f(0.5f, 0.27f, 0.0005f, 0.02f), new Vector4f(0.5f, 0.77f, 0.0005f, 0.02f)},
        FunctionalStorage.DrawerType.X_4, new Vector4f[]{new Vector4f(0.25f, 0.27f, 0.0005f, 0.02f), new Vector4f(0.75f, 0.27f, 0.0005f, 0.02f), new Vector4f(0.75f, 0.77f, 0.0005f, 0.02f), new Vector4f(0.25f, 0.77f, 0.0005f, 0.02f)}
    );

    @Override
    public final void renderItems(DrawerTile tile, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        var drawerType = tile.getDrawerType();
        var coords = SLOT_TRANSFORMS.get(drawerType);
        BigInventoryHandler inventoryHandler = (BigInventoryHandler) tile.getStorage();

        IntStream.range(0, drawerType.getSlots()).forEach(i -> {
            matrixStack.pushPose();
            matrixStack.translate(coords[i].x, coords[i].y, coords[i].z);
            matrixStack.scale(0.5f, 0.5f, 1.0f);
            ItemStack stack = inventoryHandler.getStoredStacks().get(i).getStack();
            renderStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, stack, inventoryHandler.getStackInSlot(i).getCount(), inventoryHandler.getSlotLimit(i), coords[i].w, tile.getDrawerOptions(), tile.getLevel());
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

    public static void renderStack(PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn, ItemStack stack, int amount, int maxAmount, float scale, ControllableDrawerTile.DrawerOptions options, Level level) {
        renderIndicator(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, Math.min(1, amount / (float) maxAmount), options);

        BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(stack, Minecraft.getInstance().level, null, 0);
        if (model.isGui3d()){
        	float thickness = (float)FunctionalStorageClientConfig.DRAWER_RENDER_THICKNESS;
        	// Avoid scaling normal matrix by using mulPose() instead of scale()
        	matrixStack.mulPose(createTransformMatrix(
        			new Vector3f(0), new Vector3f(0), new Vector3f(.75f, .75f, thickness)));
        } else {
        	matrixStack.mulPose(createTransformMatrix(
        			new Vector3f(0), new Vector3f(0), .4f));
        }

        if (options.isActive(ConfigurationToolItem.ConfigurationAction.TOGGLE_RENDER)) {
        	Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, combinedLightIn, combinedOverlayIn, matrixStack, bufferIn, level,0);
        }

        if (options.isActive(ConfigurationToolItem.ConfigurationAction.TOGGLE_NUMBERS))
            renderText(matrixStack, bufferIn, combinedOverlayIn, Component.literal(ChatFormatting.WHITE + "" + NumberUtils.getFormatedBigNumber(amount)), scale);
    }


    /* Thanks Mekanism */
    public static void renderText(PoseStack matrix, MultiBufferSource renderer, int overlayLight, Component text, float maxScale) {


        float displayWidth = 1;
        float displayHeight = 1;
        //matrix.translate(displayWidth / 2, 0, displayHeight / 2);
        //matrix.mulPose(Vector3f.XP.rotationDegrees(-90));

        Font font = Minecraft.getInstance().font;

        int requiredWidth = Math.max(font.width(text), 1);
        int requiredHeight = font.lineHeight + 2;
        float scaler = 0.4F;
        float scaleX = displayWidth / requiredWidth;
        float scale = scaleX * scaler;
        if (maxScale > 0) {
            scale = Math.min(scale, maxScale);
        }

        matrix.pushPose();
        matrix.translate(0, -0.745, -0.001);
        matrix.scale(scale, -scale, scale);
        int realHeight = (int) Math.floor(displayHeight / scale);
        int realWidth = (int) Math.floor(displayWidth / scale);
        int offsetX = (realWidth - requiredWidth) / 2;
        int offsetY = (realHeight - requiredHeight) / 2;
        font.drawInBatch(text, offsetX - realWidth / 2, 3 + offsetY - realHeight / 2, overlayLight, false, matrix.last().pose(), renderer, Font.DisplayMode.NORMAL,  0, 0xF000F0);
        matrix.popPose();
    }
}
