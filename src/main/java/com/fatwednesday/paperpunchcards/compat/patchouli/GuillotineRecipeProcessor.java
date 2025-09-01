package com.fatwednesday.paperpunchcards.compat.patchouli;

import com.fatwednesday.paperpunchcards.crafting.GuillotineRecipe;
import com.fatwednesday.paperpunchcards.registration.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import vazkii.patchouli.api.IComponentProcessor;
import vazkii.patchouli.api.IVariable;
import vazkii.patchouli.api.IVariableProvider;

public class GuillotineRecipeProcessor implements IComponentProcessor
{
    private GuillotineRecipe recipe;

    @Override
    public void setup(Level level, IVariableProvider variables)
    {
        var recipeId = variables.get("recipe", level.registryAccess()).asString();
        var manager = level.getRecipeManager();
        var resLoc = ResourceLocation.tryParse(recipeId);
        var holder = manager.byKey(resLoc);
        recipe = (GuillotineRecipe)holder.get().value();
    }

    @Override
    public IVariable process(Level level, String key)
    {
        if(key.startsWith("guillotine"))
        {
            return IVariable.from(
                    new ItemStack(ModItems.GUILLOTINE_BLOCK_ITEM.get(),1),
                    level.registryAccess()
            );
        }
        else if (key.startsWith("item"))
        {
            int index = Integer.parseInt(key.substring(4)) - 1;
            if(index < 0 || index >= recipe.ingredients().size())
            {
                return IVariable.from(
                        ItemStack.EMPTY,
                        level.registryAccess()
                );
            }

            return IVariable.from(
                    recipe.ingredients().get(index),
                    level.registryAccess()
            );
        }
        else if (key.equals("name"))
        {
            return IVariable.from(
                    recipe.result().getHoverName(),
                    level.registryAccess()
            );
        }
        else if (key.equals("result"))
        {
            return IVariable.from(
                    recipe.result(),
                    level.registryAccess()
            );
        }

        return null;
    }
}
