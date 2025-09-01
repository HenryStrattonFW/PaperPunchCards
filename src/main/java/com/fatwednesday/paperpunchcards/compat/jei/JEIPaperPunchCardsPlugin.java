package com.fatwednesday.paperpunchcards.compat.jei;

import com.fatwednesday.paperpunchcards.PaperPunchCards;
import com.fatwednesday.paperpunchcards.registration.ModRecipes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

@JeiPlugin
public class JEIPaperPunchCardsPlugin implements IModPlugin
{
    @Override
    public ResourceLocation getPluginUid()
    {
        return PaperPunchCards.getResource("jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration)
    {
        registration.addRecipeCategories(
                new GuillotineCategory(registration.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration)
    {
        var recipeManager = Minecraft.getInstance().level.getRecipeManager();
        var recipes = recipeManager.getAllRecipesFor(ModRecipes.GUILLOTINE_RECIPE.get())
                .stream()
                .map(RecipeHolder::value)
                .toList();
        registration.addRecipes(GuillotineCategory.GUILLOTINE_RECIPE_TYPE, recipes);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration)
    {
        IModPlugin.super.registerGuiHandlers(registration);
    }
}
