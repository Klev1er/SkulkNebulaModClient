package net.skulknebula.snebula.block.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.skulknebula.snebula.block.ModBlocks;

public class ServerExtensionUpBlock extends Block {

    public ServerExtensionUpBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        BlockPos mainPos = pos.down(2);
        if (world.getBlockState(mainPos).isOf(ModBlocks.SERVER_BLOCK)) {
            BlockState mainState = world.getBlockState(mainPos);
            if (mainState.getBlock() instanceof ServerBlock serverBlock) {
                return serverBlock.handleUse(world, mainPos, player, hit);
            }
        }
        return ActionResult.PASS;
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient()) {
            BlockPos mainPos = pos.down(2);
            if (world.getBlockState(mainPos).isOf(ModBlocks.SERVER_BLOCK)) {
                world.breakBlock(mainPos, true, player);
                BlockPos centerPos = pos.down();
                if (world.getBlockState(centerPos).isOf(ModBlocks.SERVER_EXTENSION_CENTER_BLOCK)) {
                    world.breakBlock(centerPos, false, player);
                }
            }
        }
        return super.onBreak(world, pos, state, player);
    }

    @Override
    public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        BlockPos mainPos = findMainBlock(world, pos);
        if (mainPos != null) {
            return ModBlocks.SERVER_BLOCK.asItem().getDefaultStack();
        }
        return super.getPickStack(world, pos, state, includeData);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        // Пустой контур - нельзя навестись
        return VoxelShapes.cuboid(0.0, -2.0, 0.0, 1.0, 0.625, 1.0);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        // Полная коллизия
        return VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, 0.625, 1.0);
    }

    @Override
    protected VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
        // Пустой рейкаст - лучи проходят сквозь
        return VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, 0.6, 1.0);
    }

    @Override
    protected boolean isTransparent(BlockState state) {
        return false;
    }

    @Override
    protected float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
        // Не затемняет сущности
        return 1.0f;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        // Не рендерится
        return BlockRenderType.MODEL;
    }

    @Override
    protected boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
        // Прозрачный со всех сторон
        return true;
    }

    private BlockPos findMainBlock(BlockView world, BlockPos pos) {
        BlockPos down1 = pos.down();
        BlockPos down2 = pos.down(2);

        if (world.getBlockState(down1).isOf(ModBlocks.SERVER_BLOCK)) {
            return down1;
        }
        if (world.getBlockState(down2).isOf(ModBlocks.SERVER_BLOCK)) {
            return down2;
        }
        return null;
    }
}