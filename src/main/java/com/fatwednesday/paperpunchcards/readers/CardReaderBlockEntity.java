package com.fatwednesday.paperpunchcards.readers;

import com.fatwednesday.paperpunchcards.Config;
import com.fatwednesday.paperpunchcards.PaperPunchCards;
import com.fatwednesday.paperpunchcards.items.PunchCardItem;
import com.fatwednesday.paperpunchcards.registration.ModBlocks;
import com.fatwednesday.paperpunchcards.registration.ModDataComponents;
import com.fatwednesday.paperpunchcards.utils.SignalSequence;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Clearable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;


public class CardReaderBlockEntity extends BlockEntity implements Clearable
{
    private static final String PunchCardTag = PaperPunchCards.getTag("punch_card");
    private static final String SequenceTag = PaperPunchCards.getTag("signal_sequence");
    private static final String ResidualTag = PaperPunchCards.getTag("residualTicks");

    private ItemStack currentItem = ItemStack.EMPTY;
    private CardReaderState cachedState;
    private SignalSequence bakedSequence;
    private int residualSignalTicks;

    public CardReaderBlockEntity(BlockPos pos, BlockState blockState)
    {
        super(ModBlocks.CARD_READER_BLOCK_ENTITY.get(), pos, blockState);
        cachedState = CardReaderState.UNSET;
    }

    @Override
    public void clearContent()
    {
        currentItem = ItemStack.EMPTY;
        cachedState = CardReaderState.EMPTY;
        CardReaderBlock.refreshState(level, getBlockPos());
        setChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CardReaderBlockEntity blockEntity)
    {
        if(blockEntity.residualSignalTicks > 0)
        {
            blockEntity.residualSignalTicks--;
            if(blockEntity.residualSignalTicks <= 0)
            {
                CardReaderBlock.refreshState(level, pos);
            }
        }
    }

    public boolean trySetBakedSequence(Player player, SignalSequence bakedSequence)
    {
        this.bakedSequence = bakedSequence;
        cachedState = bakedSequence == null
                ? CardReaderState.UNSET
                : CardReaderState.EMPTY;
        CardReaderBlock.refreshState(level, getBlockPos());
        setChanged();
        return true;
    }

    public void setItem(ItemStack stack)
    {
        if(!(stack.getItem() instanceof PunchCardItem))
        {
            PaperPunchCards.error("Invalid item stack, must be stack of PunchCardItem");
            return;
        }

        currentItem = stack;
        if(currentItem.has(ModDataComponents.SIGNAL_SEQUENCE))
        {
            var seq = currentItem.get(ModDataComponents.SIGNAL_SEQUENCE);
            if(seq == null)
            {
                cachedState = CardReaderState.BAD;
            }
            else
            {
                cachedState = seq.matches(bakedSequence)
                        ? CardReaderState.GOOD
                        : CardReaderState.BAD;
            }
        }
        else
        {
            cachedState = CardReaderState.BAD;
        }

        CardReaderBlock.refreshState(level, getBlockPos());
        setChanged();
    }

    public ItemStack removeItem()
    {
        if(cachedState == CardReaderState.GOOD)
        {
            residualSignalTicks = Config.CardReaderDelayTicks.get();
        }
        var toReturn = currentItem;
        currentItem = ItemStack.EMPTY;
        cachedState = CardReaderState.EMPTY;
        setChanged();
        CardReaderBlock.refreshState(level, getBlockPos());
        return toReturn;
    }

    public ItemStack getItem()
    {
        return currentItem;
    }

    public boolean isConfigured()
    {
        return cachedState != CardReaderState.UNSET;
    }

    public CardReaderState getState()
    {
        return cachedState;
    }

    public boolean hasItem()
    {
        return !currentItem.isEmpty();
    }

    public boolean shouldOutputSignal()
    {
        return cachedState ==  CardReaderState.GOOD || residualSignalTicks > 0;
    }


    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries)
    {
        super.saveAdditional(tag, registries);
        if(bakedSequence != null)
        {
            tag.putByteArray(SequenceTag, bakedSequence.bytes());
        }
        if(!currentItem.isEmpty())
        {
            tag.put(PunchCardTag, currentItem.save(registries, new CompoundTag()));
        }
        tag.putInt(ResidualTag, residualSignalTicks);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries)
    {
        super.loadAdditional(tag, registries);
        if(tag.contains(SequenceTag))
        {
            var seq = tag.getByteArray(SequenceTag);
            bakedSequence = new SignalSequence(seq);
            cachedState = CardReaderState.EMPTY;
        }
        else
        {
            cachedState = CardReaderState.UNSET;
        }
        if(tag.contains(PunchCardTag))
        {
            var parsed = ItemStack.parse(registries, tag.getCompound(PunchCardTag));
            if(parsed.isPresent())
            {
                setItem(parsed.get());
            }
            else
            {
                PaperPunchCards.error("Failed to parse PunchCard data.");
            }
        }
        else
        {
            // Only care about residual ticks if we don't have a card.
            residualSignalTicks = tag.getInt(ResidualTag);
        }
        setChanged();
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
