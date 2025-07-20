package com.fatwednesday.paperpunchcards.crafting;

import com.fatwednesday.fatlib.utils.DirectionalVoxelShape;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class CardPuncherBlock extends Block
{
    private static final DirectionProperty FACING = BlockStateProperties.FACING;
    private static final DirectionalVoxelShape SHAPE = new DirectionalVoxelShape(
            Block.box(0, 0, 3, 16, 10, 13),
            Direction.NORTH,
            Direction.EAST,
            Direction.SOUTH,
            Direction.WEST
    );

    public CardPuncherBlock(Properties properties)
    {
        super(properties);
        this.registerDefaultState(
                defaultBlockState()
                .setValue(FACING, Direction.NORTH)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection());
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
