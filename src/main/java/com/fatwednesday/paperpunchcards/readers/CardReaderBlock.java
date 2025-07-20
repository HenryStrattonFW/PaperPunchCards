package com.fatwednesday.paperpunchcards.readers;

import com.fatwednesday.fatlib.utils.DirectionalVoxelShape;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class CardReaderBlock extends BaseEntityBlock
{
    public static final MapCodec<CardReaderBlock> CODEC = simpleCodec(CardReaderBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final EnumProperty<CardReaderState> STATE = EnumProperty.create("state", CardReaderState.class);

    private static final DirectionalVoxelShape SHAPE = new DirectionalVoxelShape(
            Block.box(2, 2, 0, 14, 14, 3),
            Direction.NORTH,
            Direction.EAST,
            Direction.SOUTH,
            Direction.WEST
    );

    public CardReaderBlock(Properties properties)
    {
        super(properties);
        this.registerDefaultState(
                this.defaultBlockState()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(STATE, CardReaderState.UNSET)
        );
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec()
    {
        return null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING);
        builder.add(STATE);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState)
    {
        return null;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state)
    {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos)
    {
        return getShapeForFacing(state.getValue(FACING));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return getShapeForFacing(state.getValue(FACING));
    }

    @Override
    protected boolean isCollisionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos)
    {
        return false;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return getShapeForFacing(state.getValue(FACING));
    }

    private VoxelShape getShapeForFacing(Direction facing)
    {
        return SHAPE.tryGet(facing).orElseGet(Shapes::empty);
    }
}
