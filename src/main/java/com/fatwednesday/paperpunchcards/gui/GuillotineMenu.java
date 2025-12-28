package com.fatwednesday.paperpunchcards.gui;

import com.fatwednesday.fatlib.gui.components.ObservableSlot;
import com.fatwednesday.fatlib.gui.components.OutputSlot;
import com.fatwednesday.fatlib.gui.menus.MenuWithInventory;
import com.fatwednesday.fatlib.utils.RecipeUtils;
import com.fatwednesday.paperpunchcards.PaperPunchCards;
import com.fatwednesday.paperpunchcards.crafting.GuillotineRecipe;
import com.fatwednesday.paperpunchcards.crafting.GuillotineRecipeInput;
import com.fatwednesday.paperpunchcards.registration.ModAudio;
import com.fatwednesday.paperpunchcards.registration.ModMenus;
import com.fatwednesday.paperpunchcards.registration.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

/*
 * Based largely on the StoneCutter menu + screen, so a lot of code copied
 * from that and reformatted for my own readability / convenience.
 */
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
    private DataSlot selectedRecipeIndex;

    private List<RecipeHolder<GuillotineRecipe>> allRecipes;
    private List<RecipeHolder<GuillotineRecipe>> craftableRecipes;

    public GuillotineMenu(int id, Inventory inventory)
    {
        super(ModMenus.GUILLOTINE_MENU.get(), id);
        initMenu(inventory);
    }

    public void initMenu(Inventory playerInventory)
    {
        container = new SimpleContainer(ContainerSlots);
        player = playerInventory.player;
        allRecipes = getAllRecipes();

        inputSlots = new ObservableSlot[INPUT_INDEX_C + 1];
        for(var i=0;i<=INPUT_INDEX_C;i++)
        {
            inputSlots[i] = new ObservableSlot(container, i,15,15 + (i * 19));
            inputSlots[i].setChangeListener(this::onInputSlotChanged);
            addSlot(inputSlots[i]);
        }

        outputSlot = new OutputSlot(container, OUTPUT_INDEX, 143, 33, this::onOutputTake);
        addSlot(outputSlot);

        selectedRecipeIndex = DataSlot.standalone();
        addDataSlot(selectedRecipeIndex);
        selectedRecipeIndex.set(-1);

        CreateInventorySlots(playerInventory, 8, 95);
    }

    private void onOutputTake(Player player, ItemStack stack)
    {
        stack.onCraftedBy(player.level(), player, stack.getCount());
        var recipe = allRecipes.get(selectedRecipeIndex.get()).value();
        RecipeUtils.consumeIngredients(recipe.ingredients(), inputSlots);
        updateOutputSlotContents();

        var level = player.level();
        if (level instanceof ServerLevel serverLevel)
            ModAudio.tryPlaySound(ModAudio.GUILLOTINE_CUT.get(), player.blockPosition(), serverLevel, SoundSource.BLOCKS);
    }

    private void onInputSlotChanged(ObservableSlot slot)
    {
        // Update craftable recipe list.
        craftableRecipes = getAllCraftableRecipes();
        if(craftableRecipes.isEmpty())
            selectedRecipeIndex.set(-1);

        // May result in needing to update output.
        updateOutputSlotContents();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index)
    {
        var slot = slots.get(index);
        if (!slot.hasItem())
            return ItemStack.EMPTY;

        var rawStack = slot.getItem();
        var quickMoveStack = rawStack.copy();

        if (index == OUTPUT_INDEX)
        {
            // Output slot > player
            if (!this.moveItemStackTo(rawStack, INV_INDEX_START, HOTBAR_INDEX_END, true))
            {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(rawStack, quickMoveStack);
        }
        else if (index <= INPUT_INDEX_C)
        {
            if (!moveItemStackTo(rawStack, INV_INDEX_START, HOTBAR_INDEX_END, true))
            {
                return ItemStack.EMPTY;
            }
        }
        else
        {
            // player > container.
            if (!moveItemStackTo(rawStack, INPUT_INDEX_A, INPUT_INDEX_C, false))
            {
                return ItemStack.EMPTY;
            }
        }
        if (rawStack.isEmpty())
        {
            slot.set(ItemStack.EMPTY);
        }
        else
        {
            slot.setChanged();
        }

        if (rawStack.getCount() == quickMoveStack.getCount())
        {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, rawStack);
        broadcastChanges();
        return quickMoveStack;
    }

    @Override
    public boolean stillValid(Player player)
    {
        return true;
    }

    @Override
    public void removed(Player player)
    {
        super.removed(player);

        if (player.level().isClientSide())
            return;

        for (var i = 0; i <= INPUT_INDEX_C; ++i)
        {
            var stack = container.removeItemNoUpdate(i);
            if (stack.isEmpty())
                continue;

            player.getInventory().placeItemBackInInventory(stack);
        }
    }

    public boolean clickMenuButton(Player player, int index)
    {
        if (isCraftable(index))
        {
            selectedRecipeIndex.set(index);
            updateOutputSlotContents();
        }
        return true;
    }

    private void updateOutputSlotContents()
    {
        if(!isCraftable(selectedRecipeIndex.get()))
        {
            outputSlot.set(ItemStack.EMPTY);
            outputSlot.setChanged();
            broadcastChanges();
        }
        else
        {
            var recipe = allRecipes.get(selectedRecipeIndex.get()).value();
            outputSlot.set(recipe.result().copy());
            outputSlot.setChanged();
            broadcastChanges();
        }
    }

    public int getRecipeCount()
    {
        return allRecipes != null
                ? allRecipes.size()
                : 0;
    }

    public List<RecipeHolder<GuillotineRecipe>> getRecipes()
    {
        return allRecipes;
    }

    public int getSelectedRecipeIndex()
    {
        return selectedRecipeIndex.get();
    }

    private List<RecipeHolder<GuillotineRecipe>> getAllRecipes()
    {
        return player.level().getRecipeManager().getAllRecipesFor(ModRecipes.GUILLOTINE_RECIPE.get());
    }

    public boolean isCraftable(int recipeIndex)
    {
        if(recipeIndex < 0 ||
            recipeIndex >= getRecipeCount() ||
            craftableRecipes == null ||
            craftableRecipes.isEmpty())
        {
            return false;
        }
        var recipe = allRecipes.get(recipeIndex).value();
        for(var craftable:craftableRecipes)
        {
            if(craftable.value() == recipe)
                return true;
        }
        return false;
    }

    private List<RecipeHolder<GuillotineRecipe>> getAllCraftableRecipes()
    {
        var input = new GuillotineRecipeInput(inputSlots);

        return player.level().getRecipeManager().getRecipesFor(ModRecipes.GUILLOTINE_RECIPE.get(), input, player.level());
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
