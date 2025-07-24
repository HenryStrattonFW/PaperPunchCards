package com.fatwednesday.paperpunchcards.gui;

import com.fatwednesday.fatlib.gui.components.FilterableSlot;
import com.fatwednesday.fatlib.gui.components.ObservableSlot;
import com.fatwednesday.fatlib.gui.components.OutputSlot;
import com.fatwednesday.fatlib.gui.menus.MenuWithInventory;
import com.fatwednesday.fatlib.utils.LogoutItemGuard;
import com.fatwednesday.paperpunchcards.items.PaperPunchable;
import com.fatwednesday.paperpunchcards.registration.ModMenus;
import com.fatwednesday.paperpunchcards.utils.NibbleStore;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CardPuncherMenu extends MenuWithInventory
{
    private static final int ContainerSlots = 2;

    private final Container container;
    private final Player player;
    private final OutputSlot outputSlot;
    private final FilterableSlot inputSlot;

    private InputChangeListener changeListener;
    private NibbleStore currentSequenceData;


    public CardPuncherMenu(int id, Inventory playerInventory)
    {
        super(ModMenus.CARD_PUNCHER_MENU.get(), id);
        this.container = new SimpleContainer(ContainerSlots);
        player = playerInventory.player;

        inputSlot = new FilterableSlot(this.container, 0, 64, 96);
        inputSlot.allow(PaperPunchable.class);
        inputSlot.setChangeListener(this::inputSlotChanged);
        addSlot(inputSlot);

        outputSlot = new OutputSlot(this.container, 1, 208, 96);
        addSlot(outputSlot);

        CreateInventorySlots(playerInventory, 64, 142);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index)
    {
        var originalStack = ItemStack.EMPTY;
        var slot = this.slots.get(index);

        if (slot.hasItem())
        {
            var currentStack = slot.getItem();
            originalStack = currentStack.copy();

            if (index < 2)
            {
                // Container > Player
                if (!this.moveItemStackTo(currentStack, ContainerSlots, this.slots.size(), true))
                {
                    return ItemStack.EMPTY;
                }
            }
            else
            {
                // Player > Container
                if (!this.moveItemStackTo(currentStack, 0, ContainerSlots, false))
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

    @FunctionalInterface
    public interface InputChangeListener
    {
        void inputChanged(PaperPunchable input);
    }
}
