package com.fatwednesday.paperpunchcards.readers;

import com.fatwednesday.paperpunchcards.PaperPunchCards;
import com.fatwednesday.paperpunchcards.items.PaperTapeItem;
import com.fatwednesday.paperpunchcards.registration.ModBlocks;
import com.fatwednesday.paperpunchcards.registration.ModDataComponents;
import com.fatwednesday.paperpunchcards.utils.NibbleStore;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Clearable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;


public class TapePlayerBlockEntity extends BlockEntity implements Clearable
{
    private static final String TickCounterTag = PaperPunchCards.getTag("tick_counter");
    private static final String PaperTapeTag = PaperPunchCards.getTag("paper_tape");

    private ItemStack currentItem = ItemStack.EMPTY;
    private NibbleStore sequence;
    private int tickCounter = 0;
    private boolean hasPower = false;

    public TapePlayerBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlocks.TAPE_PLAYER_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TapePlayerBlockEntity blockEntity)
    {
        if(blockEntity.currentItem.isEmpty())
            return;

        var currentlyPowered = level.hasNeighborSignal(pos);
        if(currentlyPowered != blockEntity.hasPower)
        {
            // reset counter when power lost.
            if(!currentlyPowered)
            {
                blockEntity.tickCounter = 0;
                level.updateNeighborsAt(pos, state.getBlock());
            }
            blockEntity.hasPower = currentlyPowered;
            blockEntity.setChanged();
            TapePlayerBlock.refreshState(level, pos);
        }

        if(!currentlyPowered || blockEntity.sequence == null)
            return;

        blockEntity.tickCounter++;
        if(blockEntity.tickCounter >= blockEntity.sequence.size())
        {
            blockEntity.tickCounter = 0;
        }
        level.updateNeighborsAt(pos, state.getBlock());
    }

    public int getSignalStrength()
    {
        if(currentItem.isEmpty() || !hasPower || sequence == null)
            return 0;

        var wrapped = tickCounter % sequence.size();
        return sequence.getNibble(wrapped);
    }


    @Override
    public void clearContent()
    {
        currentItem = ItemStack.EMPTY;
        setChanged();
    }

    public void setItem(ItemStack stack)
    {
        if(!(stack.getItem() instanceof PaperTapeItem))
        {
            PaperPunchCards.error("Invalid item stack, must be stack of PaperTapeItem");
            return;
        }

        currentItem = stack;
        var seq = currentItem.get(ModDataComponents.SIGNAL_SEQUENCE);
        sequence = seq == null
                ? new NibbleStore(20)
                : new NibbleStore(seq.bytes());

        tickCounter = 0;
        setChanged();
    }

    public TapePlayerState getState()
    {
        if(currentItem.isEmpty())
            return TapePlayerState.EMPTY;

        return hasPower
                ? TapePlayerState.ACTIVE
                : TapePlayerState.FULL;
    }

    public ItemStack removeItem()
    {
        var toReturn = currentItem;
        currentItem = ItemStack.EMPTY;
        setChanged();
        return toReturn;
    }

    public boolean hasItem()
    {
        return !currentItem.isEmpty();
    }


    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries)
    {
        super.saveAdditional(tag, registries);
        tag.putInt(TickCounterTag, tickCounter);
        if(!currentItem.isEmpty())
        {
            tag.put(PaperTapeTag, currentItem.save(registries, new CompoundTag()));
        }
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries)
    {
        super.loadAdditional(tag, registries);
        tickCounter = tag.getInt(TickCounterTag);
        if(tag.contains(PaperTapeTag))
        {
            var parsed = ItemStack.parse(registries, tag.getCompound(PaperTapeTag));
            if(parsed.isPresent())
            {
                currentItem = parsed.get();
                setChanged();
                return;
            }
            else
            {
                PaperPunchCards.error("Failed to parse PaperTape data.");
            }
        }
        currentItem = ItemStack.EMPTY;
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider registries)
    {
        var tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider)
    {
        loadAdditional(tag, lookupProvider);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries)
    {
        loadAdditional(pkt.getTag(), registries);
    }
}
