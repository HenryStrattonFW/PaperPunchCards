package com.fatwednesday.paperpunchcards.readers;

import com.fatwednesday.paperpunchcards.PaperPunchCards;
import com.fatwednesday.paperpunchcards.registration.ModBlocks;
import com.fatwednesday.paperpunchcards.registration.ModItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TapePlayerBlock extends BaseEntityBlock
{
    public static final MapCodec<CardReaderBlock> CODEC = simpleCodec(CardReaderBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final EnumProperty<TapePlayerState> STATE = EnumProperty.create("state", TapePlayerState.class);

    public TapePlayerBlock(Properties properties)
    {
        super(properties);
        registerDefaultState(
                defaultBlockState()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(STATE, TapePlayerState.EMPTY)
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
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState)
    {
        return new TapePlayerBlockEntity(blockPos, blockState);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        if(level.isClientSide)
            return null;

        return createTickerHelper(
                type,
                ModBlocks.TAPE_PLAYER_BLOCK_ENTITY.get(),
                TapePlayerBlockEntity::serverTick
        );
    }

    @Override
    protected RenderShape getRenderShape(BlockState state)
    {
        return RenderShape.MODEL;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult)
    {
        var blockEntity = level.getBlockEntity(pos);
        if(blockEntity == null)
        {
            return ItemInteractionResult.FAIL;
        }
        if (blockEntity instanceof TapePlayerBlockEntity tapePlayer)
        {
            if(stack.isEmpty() && player.isCrouching())
            {
                cycleMode(player, level, state, pos);
                return ItemInteractionResult.SUCCESS;
            }
            if (!tapePlayer.hasItem() && !stack.isEmpty() && stack.is(ModItems.PAPER_TAPE_ITEM))
            {
                tapePlayer.setItem(stack.split(1));
                refreshState(level, pos);
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
            else if (tapePlayer.hasItem())
            {
                player.getInventory().placeItemBackInInventory(tapePlayer.removeItem());
                refreshState(level, pos);
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    private void cycleMode(Player player, Level level, BlockState state, BlockPos pos)
    {
        var entity = level.getBlockEntity(pos);
        if(entity instanceof TapePlayerBlockEntity tapePlayer)
        {
            tapePlayer.cycleMode(player);
        }
    }

    public static void refreshState(Level level, BlockPos pos)
    {
        var entity = level.getBlockEntity(pos);
        if(entity instanceof TapePlayerBlockEntity tapePlayer)
        {
            level.setBlock(
                    pos,
                    level.getBlockState(pos).setValue(STATE, tapePlayer.getState()),
                    Block.UPDATE_ALL
            );
        }
    }

    @Override
    protected boolean isSignalSource(BlockState state)
    {
        return false;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state)
    {
        return true;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction)
    {
        return this.getSignal(state, level, pos, direction);
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter blockAccess, BlockPos pos, Direction side)
    {
        var front = state.getValue(FACING);
        if(side != front)
        {
            return 0;
        }

        var blockEntity = blockAccess.getBlockEntity(pos);
        if(blockEntity instanceof TapePlayerBlockEntity tapePlayer)
        {
            return tapePlayer.getSignalStrength();
        }
        return 0;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos)
    {
        var blockEntity = level.getBlockEntity(pos);
        if(blockEntity instanceof TapePlayerBlockEntity tapePlayer)
        {
            return tapePlayer.getSignalStrength();
        }
        return 0;
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, Direction direction)
    {
        return false;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston)
    {
        if (!state.is(newState.getBlock()))
        {
            var blockentity = level.getBlockEntity(pos);
            if (blockentity instanceof TapePlayerBlockEntity tapePlayer)
            {
                Containers.dropContents(level, pos, NonNullList.of(ItemStack.EMPTY, tapePlayer.getItem()));
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
