package com.fatwednesday.paperpunchcards.crafting;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.ArrayList;
import java.util.List;

public class GuillotineRecipeInput implements RecipeInput
{
    public final List<ItemStack> inputs;

    public GuillotineRecipeInput(List<ItemStack> inputs)
    {
        this.inputs = inputs;
    }

    public GuillotineRecipeInput(Slot... slots)
    {
        inputs = new ArrayList<ItemStack>();
        for(var slot:slots)
        {
            inputs.add(slot.getItem());
        }
    }

    public GuillotineRecipeInput(Container container, int fromSlot, int toSlot)
    {
        inputs = new ArrayList<ItemStack>();
        for(var i = fromSlot; i <= toSlot; i++)
        {
            var stack = container.getItem(i);
            inputs.add(stack);
        }
    }

    @Override
    public ItemStack getItem(int i)
    {
        if(i< 0|| i>size())
        {
            throw new IllegalArgumentException("No item for index " + i);
        }
        return inputs.get(i);
    }

    @Override
    public int size()
    {
        return inputs.size();
    }
}
