package com.fatwednesday.paperpunchcards.gui;

import com.fatwednesday.fatlib.gui.components.ObservableSlot;
import com.fatwednesday.fatlib.gui.components.OutputSlot;
import com.fatwednesday.fatlib.gui.menus.MenuWithInventory;
import com.fatwednesday.paperpunchcards.PaperPunchCards;
import com.fatwednesday.paperpunchcards.crafting.GuillotineRecipe;
import com.fatwednesday.paperpunchcards.registration.ModMenus;
import com.fatwednesday.paperpunchcards.registration.ModRecipes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

public class GuillotineMenu extends MenuWithInventory
{
    private static final int ContainerSlots = 4;
    private static final int INPUT_INDEX_A = 0;
    private static final int INPUT_INDEX_B = 1;
    private static final int INPUT_INDEX_C = 2;
    private static final int OUTPUT_INDEX = 3;
    private static final int INV_INDEX_START = 4;
    private static final int HOTBAR_INDEX_START = INV_INDEX_START + 27;
    private static final int HOTBAR_INDEX_END = HOTBAR_INDEX_START + 9;

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

        inputSlots = new ObservableSlot[INPUT_INDEX_C+1];
        for(var i=0;i<=INPUT_INDEX_C;i++)
        {
            inputSlots[i] = new ObservableSlot(container, i,20,15 + (i * 19));
            inputSlots[i].setChangeListener(this::onInputSlotChanged);
            addSlot(inputSlots[i]);
        }

        outputSlot = new OutputSlot(container, OUTPUT_INDEX, 143, 33, this::onOutputTake);
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
        var recipes = getAllRecipes();
        PaperPunchCards.log("Discovered " + recipes.size() + " guillotine recipes");
        for(var r : recipes)
        {
            PaperPunchCards.log(r.id().getPath());
        }
        // for each recipe, add a visible button, and enable based on
        // validity against current input slot contents.
        // See StoneCutter for example.
    }


    @Override
    public ItemStack quickMoveStack(Player player, int i)
    {
        var originalStack = ItemStack.EMPTY;
        var slot = getSlot(i);
        if(!slot.hasItem())
        {
            return originalStack;
        }

        var currentStack = slot.getItem();
        originalStack = currentStack.copy();

        if(i == OUTPUT_INDEX)
        {
            // take result.
        }
        else if(i > OUTPUT_INDEX)
        {
            // player > container.
            if(!this.moveItemStackTo(currentStack, INPUT_INDEX_A, INPUT_INDEX_C, false))
            {
                return ItemStack.EMPTY;
            }
        }
        else
        {
            // container > player
            if (!this.moveItemStackTo(currentStack, INV_INDEX_START, HOTBAR_INDEX_END, false))
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

        return originalStack;
    }

    @Override
    public boolean stillValid(Player player)
    {
        return true;
    }


    private void setupResultSlot()
    {
    }

    private List<RecipeHolder<GuillotineRecipe>> getAllRecipes()
    {
        return player.level().getRecipeManager().getAllRecipesFor(ModRecipes.GUILLOTINE_RECIPE.get());
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
