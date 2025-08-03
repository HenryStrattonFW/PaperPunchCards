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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;


public class TapePlayerBlockEntity extends BlockEntity implements Clearable
{
    private static final String TickCounterTag = PaperPunchCards.getTag("tick_counter");
    private static final String PaperTapeTag = PaperPunchCards.getTag("paper_tape");
    private static final String ModeTag = PaperPunchCards.getTag("player_mode");
    private static final String PoweredTag = PaperPunchCards.getTag("powered");

    private ItemStack currentItem = ItemStack.EMPTY;
    private NibbleStore sequence;
    private int tickCounter = -1;
    private boolean hasPower = false;
    private TapePlayerMode mode = TapePlayerMode.LOOP;

    public TapePlayerBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlocks.TAPE_PLAYER_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TapePlayerBlockEntity blockEntity)
    {
        if(blockEntity.currentItem.isEmpty())
            return;

        var prevHadPower = blockEntity.hasPower;
        var prevSignal = blockEntity.getSignalStrength();
        var currentlyPowered = level.hasNeighborSignal(pos);
        var mode = blockEntity.mode;

        if(currentlyPowered)
        {
            switch (mode)
            {
                case LOOP:
                    blockEntity.tickCounter++;
                    if(blockEntity.tickCounter >= blockEntity.sequence.size())
                    {
                        blockEntity.tickCounter = 0;
                    }
                    break;

                case PLAY_ONCE:
                    if (blockEntity.tickCounter < blockEntity.sequence.size())
                    {
                        blockEntity.tickCounter++;
                    }
                    break;

                case STEP:
                    if (!prevHadPower)
                    {
                        blockEntity.tickCounter++;
                        if(blockEntity.tickCounter >= blockEntity.sequence.size())
                        {
                            blockEntity.tickCounter = 0;
                        }
                    }
                    break;
            }
        }
        else if(prevHadPower && mode != TapePlayerMode.STEP)
        {
            blockEntity.tickCounter = -1;
        }

        var newSignal = blockEntity.getSignalStrength();
        if(prevHadPower != currentlyPowered || prevSignal != newSignal)
        {
            blockEntity.hasPower = currentlyPowered;
            blockEntity.setChanged();
            level.updateNeighborsAt(pos, state.getBlock());
            TapePlayerBlock.refreshState(level, pos);
        }
    }

    public int getSignalStrength()
    {
        if(currentItem.isEmpty() || !hasPower || sequence == null)
            return 0;

        var index = mode == TapePlayerMode.PLAY_ONCE
                ? Math.clamp(tickCounter, 0, sequence.size()-1)
                : tickCounter % sequence.size();
        return sequence.getNibble(index);
    }

    public void cycleMode(Player player)
    {
        mode = mode.cycle();
        setChanged();
        if(player != null)
        {
            player.displayClientMessage(
                    mode.getMessage(),
                    true
            );
        }
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

    public ItemStack getItem()
    {
        return currentItem;
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
        tag.putString(ModeTag, mode.toString());
        tag.putBoolean(PoweredTag, hasPower);
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
        hasPower = tag.getBoolean(PoweredTag);
        if(tag.contains(ModeTag))
        {
            mode = TapePlayerMode.tryGetValue(tag.getString(ModeTag), TapePlayerMode.LOOP);
        }
        if(tag.contains(PaperTapeTag))
        {
            var parsed = ItemStack.parse(registries, tag.getCompound(PaperTapeTag));
            if(parsed.isPresent())
            {
                currentItem = parsed.get();
                var seq = currentItem.get(ModDataComponents.SIGNAL_SEQUENCE);
                sequence = seq == null
                        ? new NibbleStore(20)
                        : new NibbleStore(seq.bytes());
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
