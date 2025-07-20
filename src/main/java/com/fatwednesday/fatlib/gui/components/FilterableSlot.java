package com.fatwednesday.fatlib.gui.components;

import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;

public class FilterableSlot extends ObservableSlot
{
    private final HashSet<Class<?>> typeFilter = new HashSet<>();
    private final HashSet<TagKey<Item>> itemTagFilter = new HashSet<>();

    public FilterableSlot(Container container, int slot, int x, int y)
    {
        super(container, slot, x, y);
    }

    public final void allow(Class<?> filter)
    {
        typeFilter.add(filter);
    }

    public final void allow(TagKey<Item> tag)
    {
        itemTagFilter.add(tag);
    }

    @Override
    public boolean mayPlace(ItemStack stack)
    {
        var itemClass = stack.getItem().getClass();

        for(var filter : typeFilter)
            if(filter.isAssignableFrom(itemClass))
                return true;

        for(var filter : itemTagFilter)
            if(stack.is(filter))
                return true;

        return false;
    }
}
