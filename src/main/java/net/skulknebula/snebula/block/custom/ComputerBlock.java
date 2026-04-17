package net.skulknebula.snebula.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.skulknebula.snebula.block.custom.screen.ComputerScreenHandler;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ComputerBlock extends BlockWithEntity {
    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;
    public static final MapCodec<ComputerBlock> CODEC = createCodec(ComputerBlock::new);

    private static final VoxelShape MAIN_SHAPE = VoxelShapes.cuboid(0.1, 0.0, 0.1, 0.9, 0.8, 0.9);

    public ComputerBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction playerFacing = ctx.getHorizontalPlayerFacing();
        Direction blockFacing = playerFacing.getOpposite();
        return this.getDefaultState().with(FACING, blockFacing);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }

        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof ComputerBlockEntity computerEntity)) {
            return ActionResult.PASS;
        }

        // Проверяем, не истек ли срок занятости (игрок вышел)
        if (computerEntity.isOccupied()) {
            UUID occupyingUUID = computerEntity.getCurrentUserUUID();
            PlayerEntity occupyingPlayer = world.getPlayerByUuid(occupyingUUID);

            if (occupyingPlayer == null || !occupyingPlayer.isAlive()) {
                // Игрок вышел или умер - очищаем
                computerEntity.clearUser();
            } else if (!occupyingPlayer.getUuid().equals(player.getUuid())) {
                player.sendMessage(Text.literal("§cКомпьютер уже занят другим игроком!"), true);
                return ActionResult.FAIL;
            }
        }

        computerEntity.setCurrentUser(player);

        NamedScreenHandlerFactory screenHandlerFactory = new SimpleNamedScreenHandlerFactory(
                (syncId, inv, p) -> new ComputerScreenHandler(syncId, inv, pos),
                Text.translatable("gui.snebula.computer")
        );

        player.openHandledScreen(screenHandlerFactory);

        return ActionResult.SUCCESS;
    }

    @Override
    protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof ComputerBlockEntity computerEntity) {
            computerEntity.clearUser();
        }
        super.onStateReplaced(state, world, pos, moved);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ComputerBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return MAIN_SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return MAIN_SHAPE;
    }

    @Override
    protected VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
        return MAIN_SHAPE;
    }

    @Override
    protected VoxelShape getCameraCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return MAIN_SHAPE;
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