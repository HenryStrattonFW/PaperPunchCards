package com.fatwednesday.paperpunchcards.crafting;

import com.fatwednesday.fatlib.utils.RecipeUtils;
import com.fatwednesday.paperpunchcards.registration.ModRecipes;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;


public record GuillotineRecipe(ItemStack result, NonNullList<ItemStack> ingredients) implements Recipe<GuillotineRecipeInput>
{
    @Override
    public boolean matches(GuillotineRecipeInput guillotineRecipeInput, Level level)
    {
        return RecipeUtils.matchByCounts(guillotineRecipeInput, ingredients);
    }

    @Override
    public ItemStack assemble(GuillotineRecipeInput guillotineRecipeInput, HolderLookup.Provider registries)
    {
        return null;
    }

    @Override
    public boolean canCraftInDimensions(int i, int i1)
    {
        return false;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries)
    {
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer()
    {
        return ModRecipes.GUILLOTINE_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType()
    {
        return ModRecipes.GUILLOTINE_RECIPE.get();
    }

    public static class Serializer implements RecipeSerializer<GuillotineRecipe>
    {
        private static final MapCodec<GuillotineRecipe> CODEC = RecordCodecBuilder.mapCodec((builder) ->
                builder.group(
                        ItemStack.STRICT_CODEC.fieldOf("result").forGetter(GuillotineRecipe::result),
                        ItemStack.STRICT_CODEC.listOf().fieldOf("ingredients").flatXmap((data) ->
                        {
                            var ingredientsAsArray = data.toArray(ItemStack[]::new);
                            if (ingredientsAsArray.length == 0)
                            {
                                return DataResult.error(() -> "No ingredients for guillotine recipe");
                            }
                            else if(ingredientsAsArray.length > 3)
                            {
                                return DataResult.error(() -> "Too many ingredients for guillotine recipe. The maximum is: 3");
                            }
                            return DataResult.success(NonNullList.of(ItemStack.EMPTY, ingredientsAsArray));
                        },
                        DataResult::success).forGetter(GuillotineRecipe::ingredients)
                ).apply(builder, GuillotineRecipe::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, GuillotineRecipe> STREAM_CODEC =
                StreamCodec.of(
                        GuillotineRecipe.Serializer::toNetwork,
                        GuillotineRecipe.Serializer::fromNetwork
                );



        @Override
        public MapCodec<GuillotineRecipe> codec()
        {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, GuillotineRecipe> streamCodec()
        {
            return STREAM_CODEC;
        }

        private static GuillotineRecipe fromNetwork(RegistryFriendlyByteBuf buffer)
        {
            int size = buffer.readVarInt();
            var ingredients = NonNullList.withSize(size, ItemStack.EMPTY);
            ingredients.replaceAll((x) -> ItemStack.STREAM_CODEC.decode(buffer));
            var result = ItemStack.STREAM_CODEC.decode(buffer);
            return new GuillotineRecipe(result, ingredients);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, GuillotineRecipe recipe)
        {
            buffer.writeVarInt(recipe.ingredients.size());
            for (var ingredient : recipe.ingredients)
            {
                ItemStack.STREAM_CODEC.encode(buffer, ingredient);
            }
            ItemStack.STREAM_CODEC.encode(buffer, recipe.result);
        }

    }
}
