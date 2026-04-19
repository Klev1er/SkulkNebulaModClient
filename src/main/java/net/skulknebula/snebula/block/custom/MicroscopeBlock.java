package net.skulknebula.snebula.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

public class MicroscopeBlock extends BlockWithEntity {
    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;
    public static final MapCodec<MicroscopeBlock> CODEC = createCodec(MicroscopeBlock::new);

    // Хитбокс твоего микроскопа
    private static final VoxelShape MAIN_SHAPE = Block.createCuboidShape(3.0, 0.0, 3.0, 13.0, 10.0, 13.0);

    public MicroscopeBlock(Settings settings) {
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
        // При установке микроскоп поворачивается "лицом" к игроку
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        // Обязательно INVISIBLE для GeckoLib, иначе ванильный Майнкрафт будет рисовать черно-фиолетовые квадраты
        return BlockRenderType.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        // Создаем "мозги" и анимацию блока
        return new MicroscopeBlockEntity(pos, state);
    }

    // --- ФОРМА ХИТБОКСА ---

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

    // --- ВРАЩЕНИЕ БЛОКА ---

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }
}