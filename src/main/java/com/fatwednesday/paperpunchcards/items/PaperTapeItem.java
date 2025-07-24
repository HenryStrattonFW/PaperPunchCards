package com.fatwednesday.paperpunchcards.items;

import com.fatwednesday.paperpunchcards.Config;
import net.minecraft.world.item.Item;

public class PaperTapeItem extends Item implements PaperPunchable
{
    public PaperTapeItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public boolean showAsCards()
    {
        return false;
    }

    @Override
    public int pageCount()
    {
        return Config.PaperTapePageCount.get();
    }
}
