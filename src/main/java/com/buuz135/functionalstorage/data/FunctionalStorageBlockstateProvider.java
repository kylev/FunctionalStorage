package com.buuz135.functionalstorage.data;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.DrawerBlock;
import com.hrznstudio.titanium.block.RotatableBlock;

import com.mojang.logging.LogUtils;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.util.Lazy;

import java.util.List;

public class FunctionalStorageBlockstateProvider extends BlockStateProvider {

    private static final org.slf4j.Logger LOGGER = LogUtils.getLogger();
    private final Lazy<List<Block>> blocks;

    public FunctionalStorageBlockstateProvider(DataGenerator gen, ExistingFileHelper exFileHelper, Lazy<List<Block>> blocks) {
        super(gen.getPackOutput(), FunctionalStorage.MOD_ID, exFileHelper);
        this.blocks = blocks;
    }

    public static ResourceLocation getModel(Block block, boolean locked) {
        return com.buuz135.functionalstorage.util.Utils.resourceLocation(BuiltInRegistries.BLOCK.getKey(block).getNamespace(),
                "block/" + BuiltInRegistries.BLOCK.getKey(block).getPath() + (locked ? "_locked" : ""));
    }

    @Override
    protected void registerStatesAndModels() {
        blocks.get().stream()
            .filter(b -> b instanceof RotatableBlock)
            .forEach(b -> registerForAllLoop((RotatableBlock) b));
    }

    private void registerForAllLoop(RotatableBlock<?> block) {
        getVariantBuilder(block).forAllStates(state -> {
            boolean locked = false;
            Direction facing = state.getValue(RotatableBlock.FACING_HORIZONTAL);
            if (block instanceof DrawerBlock) {
                LOGGER.info("DrawerBlock {}", state);
                locked = state.getValue(DrawerBlock.LOCKED);
            }

            var modelFile = new ModelFile.UncheckedModelFile(getModel(block, locked));
            return ConfiguredModel.builder()
                    .modelFile(modelFile)
                    .rotationY((int) facing.getOpposite().toYRot())
                    .uvLock(true)
                    .build();
        });
    }
}
