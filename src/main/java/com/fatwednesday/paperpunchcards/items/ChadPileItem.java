package com.fatwednesday.paperpunchcards.items;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ChadPileItem extends Item
{
    public ChadPileItem(Properties properties)
    {
        super(properties);
    }

    public static ItemStack dye(ItemStack stack, DyeColor color)
    {
        return stack;
    }
}
