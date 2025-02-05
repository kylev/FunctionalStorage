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

        renderItems(tile, partialTicks, matrixStack, bufferIn, combinedLightIn, combinedOverlayIn);
        renderUpgrades(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, tile);

        matrixStack.popPose();
    }

    public abstract void renderItems(T tile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn);

    public static void renderUpgrades(PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn, ControllableDrawerTile<?> tile) {
        final float scale = 1 / 16f;
        final float zOffset = 0.45f / 16f;

        if (tile.getDrawerOptions().isActive(ConfigurationToolItem.ConfigurationAction.TOGGLE_UPGRADES)) {
            matrixStack.pushPose();
            matrixStack.translate(14.5 / 16f, 0.5 / 16f, zOffset);
            matrixStack.scale(scale, scale, 1);

            for (int i = 0; i < tile.getStorageUpgrades().getSlots(); i++) {
                ItemStack stack = tile.getStorageUpgrades().getStackInSlot(i);
                if (!stack.isEmpty()) {
                    Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.NONE, combinedLightIn, combinedOverlayIn, matrixStack, bufferIn, tile.getLevel(), 0);
                    matrixStack.translate(-1, 0, 0);
                }
            }
            matrixStack.popPose();
        }

        if (tile.isVoid()) {
            matrixStack.pushPose();
            matrixStack.translate(1.5 / 16f, 0.5 / 16f, zOffset);
            matrixStack.scale(scale, scale, 1);
            Minecraft.getInstance().getItemRenderer().renderStatic(new ItemStack(FunctionalStorage.VOID_UPGRADE.get()), ItemDisplayContext.NONE, combinedLightIn, combinedOverlayIn, matrixStack, bufferIn, tile.getLevel(),0);
            matrixStack.popPose();
        }
    }

}
