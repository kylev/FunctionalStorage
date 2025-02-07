package com.buuz135.functionalstorage.block;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.DrawerTile;
import com.buuz135.functionalstorage.client.item.DrawerISTER;
import com.buuz135.functionalstorage.inventory.item.DrawerStackItemHandler;
import com.buuz135.functionalstorage.recipe.DrawerlessWoodIngredient;
import com.buuz135.functionalstorage.util.IWoodType;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.hrznstudio.titanium.block.RotatableBlock;
import com.hrznstudio.titanium.module.BlockWithTile;
import com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder;
import com.hrznstudio.titanium.tab.TitaniumTab;
import com.mojang.logging.LogUtils;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3d;
import org.joml.Matrix4d;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.awt.Shape;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class DrawerBlock extends Drawer<DrawerTile> {

    public static final org.slf4j.Logger LOGGER = LogUtils.getLogger();

    private static final List<AABB> FRONT_SHAPE_X1 = List.of(
        new AABB(1 / 16D, 1 / 16D, 0, 15 / 16D, 15 / 16D, 1 / 16D));
    private static final List<AABB> FRONT_SHAPE_X2 = List.of(
        new AABB(1 / 16D, 1 / 16D, 0, 15 / 16D, 7 / 16D, 1 / 16D),
        new AABB(1 / 16D, 9 / 16D, 0, 15 / 16D, 15 / 16D, 1 / 16D));
    private static final List<AABB> FRONT_SHAPE_X4 = List.of(
        new AABB(1 / 16D, 1 / 16D, 0, 7 / 16D, 7 / 16D, 1 / 16D),
        new AABB(9 / 16D, 1 / 16D, 0, 15 / 16D, 7 / 16D, 1 / 16D),
        new AABB(9 / 16D, 9 / 16D, 0, 15 / 16D, 15 / 16D, 1 / 16D),
        new AABB(1 / 16D, 9 / 16D, 0, 7 / 16D, 15 / 16D, 1 / 16D));

    public static final HashMap<FunctionalStorage.DrawerType, Multimap<Direction, VoxelShape>> CACHED_SHAPES = new HashMap<>();

    public static final BooleanProperty LOCKED = BooleanProperty.create("locked");

    static {
        CACHED_SHAPES
                .computeIfAbsent(FunctionalStorage.DrawerType.X_1,
                        type1 -> MultimapBuilder.hashKeys().arrayListValues().build())
                .put(Direction.NORTH, Shapes.box(1 / 16D, 1 / 16D, 0, 15 / 16D, 15 / 16D, 1 / 16D));
        CACHED_SHAPES
                .computeIfAbsent(FunctionalStorage.DrawerType.X_1,
                        type1 -> MultimapBuilder.hashKeys().arrayListValues().build())
                .put(Direction.SOUTH, Shapes.box(1 / 16D, 1 / 16D, 15 / 16D, 15 / 16D, 15 / 16D, 1));
        CACHED_SHAPES
                .computeIfAbsent(FunctionalStorage.DrawerType.X_1,
                        type1 -> MultimapBuilder.hashKeys().arrayListValues().build())
                .put(Direction.WEST, Shapes.box(0, 1 / 16D, 1 / 16D, 1 / 16D, 15 / 16D, 15 / 16D));
        CACHED_SHAPES
                .computeIfAbsent(FunctionalStorage.DrawerType.X_1,
                        type1 -> MultimapBuilder.hashKeys().arrayListValues().build())
                .put(Direction.EAST, Shapes.box(15 / 16D, 1 / 16D, 1 / 16D, 1, 15 / 16D, 15 / 16D));
        for (Direction direction : CACHED_SHAPES.get(FunctionalStorage.DrawerType.X_1).keySet()) {
            for (VoxelShape voxelShape : CACHED_SHAPES.get(FunctionalStorage.DrawerType.X_1).get(direction)) {
                AABB bounding = voxelShape.toAabbs().get(0);
                CACHED_SHAPES
                        .computeIfAbsent(FunctionalStorage.DrawerType.X_2,
                                type1 -> MultimapBuilder.hashKeys().arrayListValues().build())
                        .put(direction, Shapes.box(bounding.minX, bounding.minY, bounding.minZ, bounding.maxX, 7 / 16D,
                                bounding.maxZ));
                CACHED_SHAPES
                        .computeIfAbsent(FunctionalStorage.DrawerType.X_2,
                                type1 -> MultimapBuilder.hashKeys().arrayListValues().build())
                        .put(direction, Shapes.box(bounding.minX, 9 / 16D, bounding.minZ, bounding.maxX, bounding.maxY,
                                bounding.maxZ));
            }
        }
        for (Direction direction : CACHED_SHAPES.get(FunctionalStorage.DrawerType.X_2).keySet()) {
            for (VoxelShape voxelShape : CACHED_SHAPES.get(FunctionalStorage.DrawerType.X_2).get(direction)) {
                AABB bounding = voxelShape.toAabbs().get(0);
                if (direction == Direction.SOUTH) {
                    CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_4, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                            put(direction, Shapes.box(9/16D, bounding.minY, bounding.minZ ,bounding.maxX, bounding.maxY, bounding.maxZ));
                    CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_4, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                            put(direction, Shapes.box(bounding.minX, bounding.minY, bounding.minZ , 7/16D, bounding.maxY, bounding.maxZ));
                }else if (direction == Direction.NORTH){
                    CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_4, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                            put(direction, Shapes.box(bounding.minX, bounding.minY, bounding.minZ , 7/16D, bounding.maxY, bounding.maxZ));
                    CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_4, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                            put(direction, Shapes.box(9/16D, bounding.minY, bounding.minZ ,bounding.maxX, bounding.maxY, bounding.maxZ));
                } else if (direction == Direction.EAST){
                    CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_4, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                            put(direction, Shapes.box(bounding.minX, bounding.minY, bounding.minZ , bounding.maxX, bounding.maxY, 7/16D));
                    CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_4, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                            put(direction, Shapes.box(bounding.minX, bounding.minY, 9/16D,bounding.maxX, bounding.maxY, bounding.maxZ));
                } else {
                    CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_4, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                            put(direction, Shapes.box(bounding.minX, bounding.minY, 9/16D,bounding.maxX, bounding.maxY, bounding.maxZ));
                    CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_4, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                            put(direction, Shapes.box(bounding.minX, bounding.minY, bounding.minZ , bounding.maxX, bounding.maxY, 7/16D));
                }
            }
        }
    }

    private final FunctionalStorage.DrawerType type;
    private final IWoodType woodType;

    public DrawerBlock(IWoodType woodType, FunctionalStorage.DrawerType type, BlockBehaviour.Properties properties) {
        super(woodType.getName() + "_" + type.getSlots(), properties, DrawerTile.class);
        this.woodType = woodType;
        this.type = type;
        setItemGroup(FunctionalStorage.TAB);
        registerDefaultState(defaultBlockState().setValue(RotatableBlock.FACING_HORIZONTAL, Direction.NORTH).setValue(LOCKED, false));
    }
    @Override
    public BlockEntityType.BlockEntitySupplier<DrawerTile> getTileEntityFactory() {
        return (blockPos, state) -> new DrawerTile(this, (BlockEntityType<DrawerTile>) FunctionalStorage.DRAWER_TYPES.get(type).stream().filter(registryObjectRegistryObjectPair -> registryObjectRegistryObjectPair.getBlock() == this).map(BlockWithTile::type).findFirst().get().get(), blockPos, state, type, woodType);
    }

    @Override
    public List<VoxelShape> getBoundingBoxes(BlockState state, BlockGetter source, BlockPos pos) {
        var direction = state.getValue(RotatableBlock.FACING_HORIZONTAL).getOpposite();
        switch (this.type) {
            case X_1:
                return FRONT_SHAPE_X1.stream().map(a -> doThingy(a, direction)).map(Shapes::create).toList();
            case X_2:
                return FRONT_SHAPE_X2.stream().map(a -> doThingy(a, direction)).map(Shapes::create).toList();
            case X_4:
                return FRONT_SHAPE_X4.stream().map(a -> doThingy(a, direction)).map(Shapes::create).toList();
            default:
                return List.of(Shapes.block());
        }
    }

    public AABB doThingy(AABB in, Direction direction) {
        var center = new Vector3d(0.5f, 0.5f, 0.5f);
        var min = in.getMinPosition();
        var max = in.getMaxPosition();
        var resMin = new Vector3d(min.x, min.y, min.z);
        var resMax = new Vector3d(max.x, max.y, max.z);

        // var transformer = new Matrix4d().rotate(direction.toYRot(), 0.5f, 0.5f, 0.5f);
        var transformer = new Matrix4d().translate(center)
        .rotate((float) Math.toRadians(-direction.toYRot()), 0f, 1f, 0f)
        .translate(center.negate());
        transformer.transformPosition(resMin);
        transformer.transformPosition(resMax);

        LOGGER.info("minX from {} to {}", min.x, resMin.x);

        return new AABB(resMin.x, resMin.y, resMin.z, resMax.x, resMax.y, resMax.z);
    }

    @Override
    public void registerRecipe(RecipeOutput consumer) {
        if (type == FunctionalStorage.DrawerType.X_1) {
            if (woodType.getName().equals("oak")) {
                TitaniumShapedRecipeBuilder.shapedRecipe(this)
                        .setName(com.buuz135.functionalstorage.util.Utils.resourceLocation(FunctionalStorage.MOD_ID,
                                "oak_drawer_alternate_x1"))
                        .pattern("PPP").pattern("PCP").pattern("PPP")
                        .define('P', new DrawerlessWoodIngredient().toVanilla())
                        .define('C', Tags.Items.CHESTS_WOODEN)
                        .save(consumer);
            } else {
                TitaniumShapedRecipeBuilder.shapedRecipe(this)
                        .pattern("PPP").pattern("PCP").pattern("PPP")
                        .define('P', woodType.getPlanks())
                        .define('C', Tags.Items.CHESTS_WOODEN)
                        .save(consumer);
            }
        }
        if (type == FunctionalStorage.DrawerType.X_2) {
            if (woodType.getName().equals("oak")) {
                TitaniumShapedRecipeBuilder.shapedRecipe(this, 2)
                        .setName(com.buuz135.functionalstorage.util.Utils.resourceLocation(FunctionalStorage.MOD_ID,
                                "oak_drawer_alternate_x2"))
                        .pattern("PCP").pattern("PPP").pattern("PCP")
                        .define('P', new DrawerlessWoodIngredient().toVanilla())
                        .define('C', Tags.Items.CHESTS_WOODEN)
                        .save(consumer);
            } else {
                TitaniumShapedRecipeBuilder.shapedRecipe(this, 2)
                        .pattern("PCP").pattern("PPP").pattern("PCP")
                        .define('P', woodType.getPlanks())
                        .define('C', Tags.Items.CHESTS_WOODEN)
                        .save(consumer);
            }
        }
        if (type == FunctionalStorage.DrawerType.X_4) {
            if (woodType.getName().equals("oak")) {
                TitaniumShapedRecipeBuilder.shapedRecipe(this, 4)
                        .setName(com.buuz135.functionalstorage.util.Utils.resourceLocation(FunctionalStorage.MOD_ID,
                                "oak_drawer_alternate_x4"))
                        .pattern("CPC").pattern("PPP").pattern("CPC")
                        .define('P', new DrawerlessWoodIngredient().toVanilla())
                        .define('C', Tags.Items.CHESTS_WOODEN)
                        .save(consumer);
            } else {
                TitaniumShapedRecipeBuilder.shapedRecipe(this, 4)
                        .pattern("CPC").pattern("PPP").pattern("CPC")
                        .define('P', woodType.getPlanks())
                        .define('C', Tags.Items.CHESTS_WOODEN)
                        .save(consumer);
            }
        }
    }

    public FunctionalStorage.DrawerType getType() {
        return type;
    }

    public IWoodType getWoodType() {
        return woodType;
    }

    public static class DrawerItem extends BlockItem {

        private final DrawerBlock drawerBlock;

        public DrawerItem(DrawerBlock drawerBlock, Properties properties, TitaniumTab tab) {
            super(drawerBlock, properties);
            this.drawerBlock = drawerBlock;
        }

        @Nullable
        public IItemHandler initCapabilities(ItemStack stack) {
            return new DrawerStackItemHandler(stack, this.drawerBlock.getType());
        }

        @Override
        public void initializeClient(Consumer<IClientItemExtensions> consumer) {
            consumer.accept(new IClientItemExtensions() {
                @Override
                public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                    return switch (drawerBlock.getType()) {
                        case X_2 -> DrawerISTER.SLOT_2;
                        case X_4 -> DrawerISTER.SLOT_4;
                        default -> DrawerISTER.SLOT_1;
                    };
                }
            });
        }

    }

}
