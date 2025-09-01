package com.fatwednesday.paperpunchcards.compat.jei;

import com.fatwednesday.paperpunchcards.PaperPunchCards;
import com.fatwednesday.paperpunchcards.crafting.GuillotineRecipe;
import com.fatwednesday.paperpunchcards.registration.ModBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class GuillotineCategory implements IRecipeCategory<GuillotineRecipe>
{
    public static final ResourceLocation UID = PaperPunchCards.getResource("guillotine_category");
    public static final ResourceLocation TEXTURE = PaperPunchCards.getResource("textures/gui/jei/guillotine.png");
    public static final RecipeType<GuillotineRecipe> GUILLOTINE_RECIPE_TYPE = new RecipeType<>(UID, GuillotineRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public GuillotineCategory(IGuiHelper helper)
    {
        background = helper.drawableBuilder(TEXTURE,0,0,96,70)
                .setTextureSize(96,70)
                .build();

        icon = helper.createDrawableIngredient(
                VanillaTypes.ITEM_STACK,
                new ItemStack(ModBlocks.GUILLOTINE_BLOCK.get())
        );
    }

    @Override
    public RecipeType<GuillotineRecipe> getRecipeType()
    {
        return GUILLOTINE_RECIPE_TYPE;
    }

    @Override
    public Component getTitle()
    {
        return PaperPunchCards.getTranslation("menu.guillotine_title");
    }

    @Override
    public @Nullable IDrawable getIcon()
    {
        return icon;
    }

    @Nullable
    @Override
    @SuppressWarnings("removal")
    public IDrawable getBackground()
    {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, GuillotineRecipe recipe, IFocusGroup focuses)
    {
        var ingredients = recipe.ingredients();
        for(var i = 0; i < ingredients.size(); i++)
        {
            builder.addInputSlot(15,8 + (i*19)).addItemStack(ingredients.get(i));
        }
        builder.addOutputSlot(63,26).addItemStack(recipe.getResultItem(null));
    }
}
