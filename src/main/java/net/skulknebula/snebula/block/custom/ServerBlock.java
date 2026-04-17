package net.skulknebula.snebula.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ServerBlock extends BlockWithEntity {
    public static final IntProperty TIER = IntProperty.of("tier", 0, 3);
    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty BROKEN = BooleanProperty.of("broken"); // НОВЫЙ ФЛАГ

    public static final MapCodec<ServerBlock> CODEC = createCodec(ServerBlock::new);

    private static final VoxelShape MAIN_SHAPE = VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, 2.6, 1.0);
    private static final VoxelShape NORTH_BUTTON = VoxelShapes.cuboid(0.4, 2.3, 0.0, 0.6, 2.5, 0.2);
    private static final VoxelShape SOUTH_BUTTON = VoxelShapes.cuboid(0.4, 2.3, 0.8, 0.6, 2.5, 1.0);
    private static final VoxelShape EAST_BUTTON = VoxelShapes.cuboid(0.8, 2.3, 0.4, 1.0, 2.5, 0.6);
    private static final VoxelShape WEST_BUTTON = VoxelShapes.cuboid(0.0, 2.3, 0.4, 0.2, 2.5, 0.6);

    public ServerBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState()
                .with(TIER, 0)
                .with(FACING, Direction.NORTH)
                .with(BROKEN, false)); // По умолчанию не сломан
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(TIER, FACING, BROKEN); // Добавляем BROKEN
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction playerFacing = ctx.getHorizontalPlayerFacing();
        Direction blockFacing = playerFacing.rotateYCounterclockwise();
        return this.getDefaultState()
                .with(TIER, 0)
                .with(FACING, blockFacing)
                .with(BROKEN, false);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }

        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof ServerBlockEntity server) {
            return server.onUse(player, player.getActiveHand());
        }

        return ActionResult.PASS;
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof ServerBlockEntity serverEntity) {
            ServerBlockEntity.tick(world, pos, state, serverEntity);
        }
        world.scheduleBlockTick(pos, this, 1);
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!world.isClient()) {
            world.scheduleBlockTick(pos, this, 1);
        }
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ServerBlockEntity(pos, state);
    }

    private VoxelShape getButtonShape(Direction facing) {
        return switch (facing) {
            case NORTH -> NORTH_BUTTON;
            case SOUTH -> SOUTH_BUTTON;
            case EAST -> EAST_BUTTON;
            case WEST -> WEST_BUTTON;
            default -> NORTH_BUTTON;
        };
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Direction facing = state.get(FACING);
        return VoxelShapes.union(MAIN_SHAPE, getButtonShape(facing));
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }
}