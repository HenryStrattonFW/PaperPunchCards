package com.fatwednesday.paperpunchcards.gui;

import com.fatwednesday.fatlib.gui.components.FilterableSlot;
import com.fatwednesday.fatlib.gui.components.ObservableSlot;
import com.fatwednesday.fatlib.gui.components.OutputSlot;
import com.fatwednesday.fatlib.gui.menus.MenuWithInventory;
import com.fatwednesday.fatlib.utils.LogoutItemGuard;
import com.fatwednesday.paperpunchcards.items.PaperPunchable;
import com.fatwednesday.paperpunchcards.registration.ModItems;
import com.fatwednesday.paperpunchcards.registration.ModMenus;
import com.fatwednesday.paperpunchcards.utils.NibbleStore;
import com.fatwednesday.paperpunchcards.utils.SignalSequence;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CardPuncherMenu extends MenuWithInventory
{
    private static final int ContainerSlots = 2;

    private final Container container;
    private final Player player;
    private final OutputSlot outputSlot;
    private final FilterableSlot inputSlot;
    private final NibbleStore sequenceData = new NibbleStore(20);
    private InputChangeListener changeListener;


    public CardPuncherMenu(int id, Inventory playerInventory)
    {
        super(ModMenus.CARD_PUNCHER_MENU.get(), id);
        container = new SimpleContainer(ContainerSlots);
        player = playerInventory.player;

        inputSlot = new FilterableSlot(container, 0, 64, 96);
        inputSlot.allow(PaperPunchable.class);
        inputSlot.setChangeListener(this::inputSlotChanged);
        addSlot(inputSlot);

        outputSlot = new OutputSlot(container, 1, 208, 96);
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
        var stack = slot.getItem();
        var item = stack.getItem();
        var inputItem = (item instanceof PaperPunchable punchable)
                ? punchable
                : null;

        LogoutItemGuard.clear(player);
        if(inputItem != null)
        {
            LogoutItemGuard.queue(player, stack);
        }

        if(changeListener != null)
        {
            changeListener.inputChanged(inputItem);
        }
    }

    public void setChangeListener(InputChangeListener listener)
    {
        this.changeListener = listener;
    }

    public NibbleStore sequenceData()
    {
        return sequenceData;
    }

    public void tryCreatePunchCard(int containerId, byte[] bytes)
    {
        if(this.containerId != containerId)
            return;

        if(player.level().isClientSide())
            return;

        var inputStack = container.getItem(inputSlot.index);
        if (inputStack.isEmpty())
            return;

        container.removeItem(inputSlot.index, 1);

        var output = container.getItem(outputSlot.index);
        if(output.isEmpty())
        {
            output = new ItemStack(ModItems.PUNCH_CARD_ITEM.get(), 1);
        }
        else
        {
            output.grow(1);
        }

        PaperPunchable.createAssignment()
                .withSequence(new SignalSequence(bytes))
                .withStack(output)
                .assign();

        outputSlot.set(output);

        LogoutItemGuard.clear(player);
        LogoutItemGuard.queue(player.getUUID(), inputSlot.getItem());

        container.setChanged();
        broadcastChanges();
    }

    @Override
    public void removed(@NotNull Player player)
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
}
