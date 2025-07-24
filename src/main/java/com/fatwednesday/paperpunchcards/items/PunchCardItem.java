package com.fatwednesday.paperpunchcards.items;

import net.minecraft.world.item.Item;

public class PunchCardItem extends Item implements PaperPunchable
{
    public PunchCardItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public boolean showAsCards()
    {
        return true;
    }

    @Override
    public int pageCount()
    {
        return 1;
    }
}
