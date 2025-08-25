package com.fatwednesday.paperpunchcards.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.List;

public class GuillotineRecipeInput implements RecipeInput
{
    private final List<ItemStack> inputs;

    public GuillotineRecipeInput(List<ItemStack> inputs)
    {
        this.inputs = inputs;
    }

    @Override
    public ItemStack getItem(int i)
    {
        if(i<= 0|| i>size())
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
