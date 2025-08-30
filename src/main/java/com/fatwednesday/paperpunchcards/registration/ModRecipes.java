package com.fatwednesday.paperpunchcards.registration;

import com.fatwednesday.paperpunchcards.PaperPunchCards;
import com.fatwednesday.paperpunchcards.crafting.GuillotineRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRecipes
{
    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, PaperPunchCards.MOD_ID);

    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, PaperPunchCards.MOD_ID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<GuillotineRecipe>> GUILLOTINE_RECIPE =
            TYPES.register(
                    "guillotine_recipe",
                    ()-> new RecipeType<>()
                    {
                        @Override
                        public String toString()
                        {
                            return "guillotine_recipe";
                        }
                    }
            );

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GuillotineRecipe>> GUILLOTINE_RECIPE_SERIALIZER =
            SERIALIZERS.register(
                    "guillotine_recipe",
                    GuillotineRecipe.Serializer::new
            );



    public static void register(IEventBus modEventBus)
    {
        TYPES.register(modEventBus);
        SERIALIZERS.register(modEventBus);
    }

}
