package com.buuz135.functionalstorage.block;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.DrawerTile;
import com.buuz135.functionalstorage.client.item.DrawerISTER;
import com.buuz135.functionalstorage.inventory.item.DrawerStackItemHandler;
import com.buuz135.functionalstorage.recipe.DrawerlessWoodIngredient;
import com.buuz135.functionalstorage.util.IWoodType;
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
import org.joml.Matrix4d;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class DrawerBlock extends Drawer<DrawerTile> {

    public static final BooleanProperty LOCKED = BooleanProperty.create("locked");
    public static final HashMap<FunctionalStorage.DrawerType, HashMap<Direction, List<VoxelShape>>> CACHED_SHAPES = new HashMap<>();

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

    static {
        for (var dir : Direction.Plane.HORIZONTAL) {
            CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_1,
                    t -> new HashMap<>()).put(dir, FRONT_SHAPE_X1.stream().map(a -> rotateShapes(a, dir)).map(Shapes::create).toList());
            CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_2,
                    t -> new HashMap<>()).put(dir, FRONT_SHAPE_X2.stream().map(a -> rotateShapes(a, dir)).map(Shapes::create).toList());
            CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_4,
                    t -> new HashMap<>()).put(dir, FRONT_SHAPE_X4.stream().map(a -> rotateShapes(a, dir)).map(Shapes::create).toList());
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
        List<VoxelShape> boxes = new ArrayList<>();
        CACHED_SHAPES.get(this.type).get(state.getValue(RotatableBlock.FACING_HORIZONTAL)).forEach(boxes::add);
        // TODO: We lose the outer hitbox without this, but with it we lose access to X4 left side?!?
        // boxes.add(Shapes.block());

        return boxes;
    }

    public static AABB rotateShapes(AABB in, Direction direction) {
        var center = new Vector3d(0.5f, 0.5f, 0.5f);
        var min = in.getMinPosition();
        var max = in.getMaxPosition();
        var resMin = new Vector3d(min.x, min.y, min.z);
        var resMax = new Vector3d(max.x, max.y, max.z);

        var transformer = new Matrix4d().translate(center)
                .rotate((float) Math.toRadians(-direction.getOpposite().toYRot()), 0f, 1f, 0f)
                .translate(center.negate());
        transformer.transformPosition(resMin);
        transformer.transformPosition(resMax);

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
