package com.buuz135.functionalstorage.client;

import com.buuz135.functionalstorage.block.tile.EnderDrawerTile;
import com.buuz135.functionalstorage.inventory.EnderInventoryHandler;
import com.buuz135.functionalstorage.world.EnderSavedData;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;


public class EnderDrawerRenderer extends BaseDrawerRenderer<EnderDrawerTile> {

    @Override
    public void renderItems(EnderDrawerTile tile, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        EnderInventoryHandler inventoryHandler =  EnderSavedData.getInstance(tile.getLevel()).getFrequency(tile.getFrequency());
        if (!inventoryHandler.getStoredStacks().get(0).getStack().isEmpty()){
            matrixStack.pushPose();
            matrixStack.translate(0.5, 0.5, 0.0005f);
            ItemStack stack = inventoryHandler.getStoredStacks().get(0).getStack();
            DrawerRenderer.renderStack(matrixStack,  bufferIn, combinedLightIn, combinedOverlayIn, stack, inventoryHandler.getStoredStacks().get(0).getAmount(), inventoryHandler.getSlotLimit(0), 0.015f, tile.getDrawerOptions(), tile.getLevel());
            matrixStack.popPose();
        }
    }

}
