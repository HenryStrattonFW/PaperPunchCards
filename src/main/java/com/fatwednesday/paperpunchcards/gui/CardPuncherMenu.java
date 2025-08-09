package com.fatwednesday.paperpunchcards.gui;

import com.fatwednesday.fatlib.gui.components.FilterableSlot;
import com.fatwednesday.fatlib.gui.components.ObservableSlot;
import com.fatwednesday.fatlib.gui.components.OutputSlot;
import com.fatwednesday.fatlib.gui.menus.MenuWithInventory;
import com.fatwednesday.fatlib.utils.LogoutItemGuard;
import com.fatwednesday.paperpunchcards.items.PaperPunchable;
import com.fatwednesday.paperpunchcards.registration.ModDataComponents;
import com.fatwednesday.paperpunchcards.registration.ModMenus;
import com.fatwednesday.paperpunchcards.utils.NibbleStore;
import com.fatwednesday.paperpunchcards.utils.SignalSequence;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


public class CardPuncherMenu extends MenuWithInventory
{
    private static final int ContainerSlots = 2;

    private Container container;
    private Player player;
    private OutputSlot outputSlot;
    private FilterableSlot inputSlot;
    // default to something empty even before we have an input.
    private NibbleStore sequenceData = new NibbleStore(20);
    private InputChangeListener changeListener;

    public CardPuncherMenu(int id, Inventory inventory)
    {
        super(ModMenus.CARD_PUNCHER_MENU.get(), id);
        initMenu(inventory);
    }

    public void initMenu(Inventory playerInventory)
    {
        container = new SimpleContainer(ContainerSlots);
        player = playerInventory.player;

        inputSlot = new FilterableSlot(container, 0, 64,  96);
        inputSlot.allow(PaperPunchable.class);
        inputSlot.setChangeListener(this::inputSlotChanged);
        addSlot(inputSlot);

        outputSlot = new OutputSlot(container, 1, 208, 96, null);//this::onOutputTaken);
        outputSlot.setChangeListener(this::outputSlotChanged);
        addSlot(outputSlot);

        CreateInventorySlots(playerInventory, 64, 142);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index)
    {
        var originalStack = ItemStack.EMPTY;
        var slot = slots.get(index);

        if (slot.hasItem())
        {
            var currentStack = slot.getItem();
            originalStack = currentStack.copy();

            if (index < 2)
            {
                // Container > Player
                if (!moveItemStackTo(currentStack, ContainerSlots, slots.size(), true))
                {
                    return ItemStack.EMPTY;
                }
            }
            else
            {
                // Player > Container
                if (!moveItemStackTo(currentStack, 0, ContainerSlots, false))
                {
                    return ItemStack.EMPTY;
                }
            }

            if (currentStack.isEmpty())
            {
                slot.set(ItemStack.EMPTY);
            }
            else
            {
                slot.setChanged();
            }
        }

        return originalStack;
    }

    @Override
    public boolean stillValid(Player player)
    {
        return true;
    }

    private void inputSlotChanged(ObservableSlot slot)
    {
        var data = getInputAsPunchable();

        if(data.punchable != null)
        {
            LogoutItemGuard.queue(player, data.stack);

            NibbleStore newSeq = null;
            if(data.stack.has(ModDataComponents.SIGNAL_SEQUENCE))
            {
                var seq = data.stack.get(ModDataComponents.SIGNAL_SEQUENCE);
                if(seq != null)
                    newSeq = new NibbleStore(seq.bytes());
            }
            if(newSeq != null)
            {
                sequenceData = newSeq;
            }
            else
            {
                var targetSize = 20 * data.punchable.pageCount();
                if(sequenceData.size() != targetSize)
                {
                    sequenceData = new NibbleStore(20 * data.punchable.pageCount());
                }
            }
        }
        else
        {
            sequenceData.clear();
        }

        if(changeListener != null)
        {
            changeListener.inputChanged(data.punchable);
        }
    }

    private void outputSlotChanged(ObservableSlot slot)
    {
        // We're only using this to refresh the screen when output is cleared.
        // so ignore if it's not empty. Should really do this properly instead
        // of piggybacking on the input change event... but for now, screw it.
        if(!slot.getItem().isEmpty())
            return;

        if(changeListener != null)
        {
            var data = getInputAsPunchable();
            changeListener.inputChanged(data.punchable);
        }
    }

    private PunchableResult getInputAsPunchable()
    {
        var stack = inputSlot.getItem();
        var item = stack.getItem();
        return (item instanceof PaperPunchable punchable)
                ? new PunchableResult(stack, punchable)
                : new PunchableResult(stack, null);
    }

    public void setChangeListener(InputChangeListener listener)
    {
        this.changeListener = listener;
    }

    public NibbleStore sequenceData()
    {
        return sequenceData;
    }

    public boolean canTriggerConfirm()
    {
        if(sequenceData == null || sequenceData.isEmpty())
            return false;

        if(!inputSlot.hasItem())
            return false;

        if(outputSlot.hasItem())
        {
            var inStack = inputSlot.getItem();
            var outStack = outputSlot.getItem();
            if(!inStack.is(outStack.getItem()))
                return false;

            var outSeq = outStack.get(ModDataComponents.SIGNAL_SEQUENCE);
            if(outSeq == null)
                return false;
            return outSeq.matches(sequenceData.bytes());
        }
        return true;
    }

    public void tryCreateOutput(byte[] bytes)
    {
        if(player.level().isClientSide())
            return;

        var inputStack = container.getItem(inputSlot.index);
        if (inputStack.isEmpty())
            return;

        var output = container.getItem(outputSlot.index);
        if(output.isEmpty())
        {
            output = inputStack.copy();
            output.setCount(1);

            PaperPunchable.createAssignment()
                    .withSequence(new SignalSequence(bytes))
                    .withStack(output)
                    .assign();
        }
        else
        {
            output.grow(1);
        }
        container.removeItem(inputSlot.index, 1);

        outputSlot.set(output);

        LogoutItemGuard.clear(player);
        LogoutItemGuard.queue(player.getUUID(), inputSlot.getItem());

        container.setChanged();
        broadcastChanges();
    }

    @Override
    public void removed(Player player)
    {
        super.removed(player);

        if (player.level().isClientSide())
            return;

        for (var i = 0; i < container.getContainerSize(); ++i)
        {
            var stack = container.removeItemNoUpdate(i);
            if (stack.isEmpty())
                continue;
            player.getInventory().placeItemBackInInventory(stack);
        }
    }

    @FunctionalInterface
    public interface InputChangeListener
    {
        void inputChanged(PaperPunchable input);
    }

    private record PunchableResult(ItemStack stack, PaperPunchable punchable){}


    public static void openMenuForPlayer( Player player)
    {
        player.openMenu(
            new SimpleMenuProvider(
                (id, inventory, p) -> new CardPuncherMenu(id, inventory),
                Component.empty()
            )
        );
    }
}
