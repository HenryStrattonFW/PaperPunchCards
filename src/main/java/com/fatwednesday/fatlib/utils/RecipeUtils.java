package com.fatwednesday.fatlib.utils;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeUtils
{
    public static boolean matchByCounts(RecipeInput input, List<ItemStack> ingredients)
    {
        var pool = countInputs(input);

        // For each ingredient, try to consume the required amount from any matching key(s).
        for(var ingredient : ingredients)
        {
            var needed = ingredient.getCount();
            if (ingredient.isEmpty() || needed <= 0)
                continue;

            var taken = 0;
            for (var e : pool.entrySet())
            {
                var probe = e.getKey().sample();
                if (!ItemStack.isSameItemSameComponents(ingredient, probe))
                    continue;

                var canTake = Math.min(needed - taken, e.getValue());
                if (canTake > 0)
                {
                    e.setValue(e.getValue() - canTake);
                    taken += canTake;
                    if (taken == needed)
                        break;
                }
            }
            if (taken < needed)
                return false;
        }

        return true;
    }

    private record StackKey(ItemStack sample)
    {
        public boolean same(ItemStack other)
        {
            return ItemStack.isSameItemSameComponents(sample, other);
        }
    }

    private static Map<StackKey, Integer> countInputs(RecipeInput input)
    {
        var counts = new HashMap<StackKey, Integer>();
        for (var i = 0; i < input.size(); i++)
        {
            var stack = input.getItem(i);
            if (stack.isEmpty())
                continue;

            StackKey existing = null;
            for (var k : counts.keySet())
            {
                if (!k.same(stack))
                    continue;

                existing = k;
                break;
            }
            if (existing == null)
            {
                counts.put(new StackKey(stack.copy()), stack.getCount());
            }
            else
            {
                counts.merge(existing, stack.getCount(), Integer::sum);
            }
        }
        return counts;
    }

    public static void consumeIngredients(List<ItemStack> ingredients, Slot...inputSlots)
    {
        for(var ingredient : ingredients)
        {
            var needed = ingredient.getCount();
            for(var slot : inputSlots)
            {
                var item = slot.getItem();
                if (!ItemStack.isSameItemSameComponents(ingredient, item))
                    continue;

                var canTake = Math.min(needed, item.getCount());
                if(canTake == 0)
                    continue;

                needed -= canTake;
                item.shrink(canTake);

                if(needed == 0)
                    break;
            }
        }
    }
}
