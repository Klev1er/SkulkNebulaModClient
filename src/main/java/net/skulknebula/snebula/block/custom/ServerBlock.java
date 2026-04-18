package net.skulknebula.snebula.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
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
import net.skulknebula.snebula.block.ModBlocks;
import org.jetbrains.annotations.Nullable;

public class ServerBlock extends BlockWithEntity {
    public static final IntProperty TIER = IntProperty.of("tier", 0, 3);
    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty BROKEN = BooleanProperty.of("broken");

    public static final MapCodec<ServerBlock> CODEC = createCodec(ServerBlock::new);

    public ServerBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState()
                .with(TIER, 0)
                .with(FACING, Direction.NORTH)
                .with(BROKEN, false));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(TIER, FACING, BROKEN);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        World world = ctx.getWorld();
        BlockPos pos = ctx.getBlockPos();

        if (!world.getBlockState(pos.up()).canReplace(ctx) ||
                !world.getBlockState(pos.up(2)).canReplace(ctx)) {
            return null;
        }

        Direction playerFacing = ctx.getHorizontalPlayerFacing();
        Direction blockFacing = playerFacing.rotateYCounterclockwise();
        return this.getDefaultState()
                .with(TIER, 0)
                .with(FACING, blockFacing)
                .with(BROKEN, false);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (!world.isClient()) {
            world.setBlockState(pos.up(), ModBlocks.SERVER_EXTENSION_CENTER_BLOCK.getDefaultState(), Block.NOTIFY_ALL);
            world.setBlockState(pos.up(2), ModBlocks.SERVER_EXTENSION_UP_BLOCK.getDefaultState(), Block.NOTIFY_ALL);
        }
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient()) {
            breakAllThreeBlocks(world, pos, player);
        }
        return super.onBreak(world, pos, state, player);
    }

    private void breakAllThreeBlocks(World world, BlockPos pos, PlayerEntity player) {
        BlockPos mainPos = findMainBlock(world, pos);
        if (mainPos == null) {
            mainPos = pos;
        }

        world.breakBlock(mainPos, false, player);

        BlockPos centerPos = mainPos.up();
        if (world.getBlockState(centerPos).isOf(ModBlocks.SERVER_EXTENSION_CENTER_BLOCK)) {
            world.breakBlock(centerPos, false, player);
        }

        BlockPos upPos = mainPos.up(2);
        if (world.getBlockState(upPos).isOf(ModBlocks.SERVER_EXTENSION_UP_BLOCK)) {
            world.breakBlock(upPos, false, player);
        }
    }

    private BlockPos findMainBlock(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);

        if (state.isOf(ModBlocks.SERVER_BLOCK)) {
            return pos;
        }
        if (state.isOf(ModBlocks.SERVER_EXTENSION_CENTER_BLOCK)) {
            return pos.down();
        }
        if (state.isOf(ModBlocks.SERVER_EXTENSION_UP_BLOCK)) {
            return pos.down(2);
        }
        return null;
    }

    // ПУБЛИЧНЫЙ метод для вызова из расширений
    public ActionResult handleUse(World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient()) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof ServerBlockEntity server) {
                return server.onUse(player, player.getActiveHand());
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        return handleUse(world, pos, player, hit);
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
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
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, 2.625, 1.0);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.fullCube();
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

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }
}