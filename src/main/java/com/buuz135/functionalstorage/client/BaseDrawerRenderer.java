package com.buuz135.functionalstorage.client;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.item.ConfigurationToolItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import org.joml.Vector3f;

import static com.buuz135.functionalstorage.util.MathUtils.createTransformMatrix;

public abstract class BaseDrawerRenderer<T extends ControllableDrawerTile<T>> implements BlockEntityRenderer<T> {
    private static final org.slf4j.Logger LOGGER = LogUtils.getLogger();

    @Override
    public int getViewDistance() {
        return FunctionalStorageClientConfig.DRAWER_RENDER_RANGE;
    }

    @Override
    public final void render(T tile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        matrixStack.pushPose();

        Direction facing = tile.getFacingDirection().getOpposite();
        matrixStack.rotateAround(Axis.YP.rotationDegrees(-facing.toYRot()), 0.5f, 0.5f, 0.5f);
        combinedLightIn = LevelRenderer.getLightColor(tile.getLevel(), tile.getBlockPos().relative(facing));

        renderUpgrades(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, tile);
        renderItems(tile, partialTicks, matrixStack, bufferIn, combinedLightIn, combinedOverlayIn);
        matrixStack.popPose();
    }

    public abstract void renderItems(T tile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn);

    public static void renderUpgrades(PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn, ControllableDrawerTile<?> tile) {

        float scale = 0.0625f;
        if (tile.getDrawerOptions().isActive(ConfigurationToolItem.ConfigurationAction.TOGGLE_UPGRADES)){
            matrixStack.pushPose();
            matrixStack.translate(0.031, 0.031f, 0.472f / 16);
            for (int i = 0; i < tile.getStorageUpgrades().getSlots(); i++) {
                ItemStack stack = tile.getStorageUpgrades().getStackInSlot(i);
                if (!stack.isEmpty()){
                    matrixStack.pushPose();
                    matrixStack.scale(scale, scale, scale);
                    Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.NONE, combinedLightIn, combinedOverlayIn, matrixStack, bufferIn, tile.getLevel(), 0);
                    matrixStack.popPose();
                    matrixStack.translate(scale,0,0);
                }
            }
            matrixStack.popPose();
        }
        if (tile.isVoid()){
            matrixStack.pushPose();
            matrixStack.mulPose(createTransformMatrix(
            		new Vector3f(0.969f, 0.031f, 0.469f / 16), new Vector3f(0), scale));
            Minecraft.getInstance().getItemRenderer().renderStatic(new ItemStack(FunctionalStorage.VOID_UPGRADE.get()), ItemDisplayContext.NONE, combinedLightIn, combinedOverlayIn, matrixStack, bufferIn, tile.getLevel(),0);
            matrixStack.popPose();
        }
    }

}
