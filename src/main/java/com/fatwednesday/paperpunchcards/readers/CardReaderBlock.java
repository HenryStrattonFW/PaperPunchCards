package com.fatwednesday.paperpunchcards.readers;

import com.fatwednesday.fatlib.utils.DirectionalVoxelShape;
import com.fatwednesday.fatlib.utils.PlacementUtils;
import com.fatwednesday.paperpunchcards.PaperPunchCards;
import com.fatwednesday.paperpunchcards.registration.ModBlocks;
import com.fatwednesday.paperpunchcards.registration.ModDataComponents;
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
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Redstone;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class CardReaderBlock extends BaseEntityBlock
{
    public static final MapCodec<CardReaderBlock> CODEC = simpleCodec(CardReaderBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final EnumProperty<CardReaderState> STATE = EnumProperty.create("state", CardReaderState.class);
    private static final int PLACEMENT_FLAGS = PlacementUtils.ALLOW_HORIZONTAL | PlacementUtils.REQUIRE_STURDY;
    private static final DirectionalVoxelShape SHAPE = new DirectionalVoxelShape(
            Block.box(2, 2, 0, 14, 14, 5),
            Direction.NORTH,
            Direction.EAST,
            Direction.SOUTH,
            Direction.WEST
    );

    public CardReaderBlock(Properties properties)
    {
        super(properties);
        registerDefaultState(
                defaultBlockState()
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
        if(PlacementUtils.validatePlacement(context, PLACEMENT_FLAGS))
        {
            return defaultBlockState().setValue(
                    FACING,
                    context.getClickedFace().getOpposite()
            );
        }
        return null;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState)
    {
        return new CardReaderBlockEntity(blockPos, blockState);
    }


    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        if(level.isClientSide)
            return null;

        return createTickerHelper(
                type,
                ModBlocks.CARD_READER_BLOCK_ENTITY.get(),
                CardReaderBlockEntity::serverTick
        );
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


    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side)
    {
        if(state.getValue(FACING) != side.getOpposite())
            return Redstone.SIGNAL_NONE;

        var entity = level.getBlockEntity(pos);
        if(entity instanceof CardReaderBlockEntity cardReader )
        {
            return cardReader.shouldOutputSignal()
                    ? Redstone.SIGNAL_MAX
                    : Redstone.SIGNAL_NONE;
        }
        return Redstone.SIGNAL_NONE;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston)
    {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);

        var attachedDir = state.getValue(FACING);
        var supportPos = pos.relative(attachedDir);
        var attachState = level.getBlockState(supportPos);
        if(!attachState.isFaceSturdy(level, supportPos, attachedDir.getOpposite()))
        {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult)
    {
        var blockEntity = level.getBlockEntity(pos);
        if(blockEntity == null)
        {
            return ItemInteractionResult.FAIL;
        }
        if (blockEntity instanceof CardReaderBlockEntity cardReader)
        {
            if (!cardReader.hasItem() && !stack.isEmpty() && stack.is(ModItems.PUNCH_CARD_ITEM))
            {
                if(!cardReader.isConfigured())
                {
                    player.displayClientMessage(
                            PaperPunchCards.getTranslation("message.reader_not_configured"),
                            true
                    );
                    return ItemInteractionResult.sidedSuccess(level.isClientSide);
                }
                var item = stack.split(1);
                cardReader.setItem(item);
                level.updateNeighbourForOutputSignal(pos, state.getBlock());
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
            else if (cardReader.hasItem())
            {
                var returnItem = cardReader.removeItem();
                player.getInventory().placeItemBackInInventory(returnItem);
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    public static void refreshState(Level level, BlockPos pos)
    {
        var entity = level.getBlockEntity(pos);
        if(entity instanceof CardReaderBlockEntity cardReader)
        {
            level.setBlock(
              pos,
              level.getBlockState(pos).setValue(STATE, cardReader.getState()),
              Block.UPDATE_ALL
            );
            var state = level.getBlockState(pos);
            var facing = state.getValue(FACING);
            var targetPos = pos.relative(facing);
            level.updateNeighborsAtExceptFromFacing(targetPos, state.getBlock(), facing.getOpposite());
            //level.blockUpdated(targetPos, state.getBlock());
        }
    }

    public void tryConfigureReader(Player player, ItemStack stack, Level level, CardReaderBlockEntity cardReader)
    {
        var sequence = stack.get(ModDataComponents.SIGNAL_SEQUENCE);
        if(cardReader.trySetBakedSequence(player, sequence))
        {
            if(sequence == null)
            {
                player.displayClientMessage(
                        PaperPunchCards.getTranslation("message.reader_cleared"),
                        true
                );
                refreshState(level, cardReader.getBlockPos());
            }
            else
            {
                player.displayClientMessage(
                        PaperPunchCards.getTranslation("message.reader_succeeded"),
                        true
                );
            }
            refreshState(level, cardReader.getBlockPos());
        }
        else
        {
            player.displayClientMessage(
                    PaperPunchCards.getTranslation("message.reader_failed"),
                    true
            );
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston)
    {
        if (!state.is(newState.getBlock()))
        {
            var blockentity = level.getBlockEntity(pos);
            if (blockentity instanceof CardReaderBlockEntity cardReader)
            {
                Containers.dropContents(level, pos, NonNullList.of(ItemStack.EMPTY, cardReader.getItem()));
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
