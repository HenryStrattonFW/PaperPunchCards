package com.fatwednesday.paperpunchcards.gui;

import com.fatwednesday.fatlib.gui.components.ObservableSlot;
import com.fatwednesday.fatlib.gui.components.OutputSlot;
import com.fatwednesday.fatlib.gui.menus.MenuWithInventory;
import com.fatwednesday.paperpunchcards.registration.ModMenus;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class GuillotineMenu extends MenuWithInventory
{
    private static final int ContainerSlots = 3;
    private Container container;
    private Player player;
    private ObservableSlot[] inputSlots;
    private OutputSlot outputSlot;

    public GuillotineMenu(int id, Inventory inventory)
    {
        super(ModMenus.GUILLOTINE_MENU.get(), id);
        initMenu(inventory);
    }

    public void initMenu(Inventory playerInventory)
    {
        container = new SimpleContainer(ContainerSlots);
        player = playerInventory.player;

        inputSlots = new ObservableSlot[ContainerSlots];
        for(var i=0;i<ContainerSlots;i++)
        {
            inputSlots[i] = new ObservableSlot(container, i,20,15 + (i * 19));
            inputSlots[i].setChangeListener(this::onInputSlotChanged);
            addSlot(inputSlots[i]);
        }

        outputSlot = new OutputSlot(container, 1, 208, 96, this::onOutputTake);
        addSlot(outputSlot);

        CreateInventorySlots(playerInventory, 8, 84);
    }


    private void onOutputTake(Player player, ItemStack stack)
    {
        //stack.onCraftedBy(player.level(), player, stack.getCount());

        // Remove inputs relevant to the cost of the crafted item.
    }

    private void onInputSlotChanged(ObservableSlot slot)
    {
        // Update available recipes.
    }


    @Override
    public ItemStack quickMoveStack(Player player, int i)
    {
        return null;
    }

    @Override
    public boolean stillValid(Player player)
    {
        return true;
    }

    private void refreshOutputSlot()
    {

    }

    public static void openMenuForPlayer( Player player)
    {
        player.openMenu(
                new SimpleMenuProvider(
                        (id, inventory, p) -> new GuillotineMenu(id, inventory),
                        Component.empty()
                )
        );
    }
}
