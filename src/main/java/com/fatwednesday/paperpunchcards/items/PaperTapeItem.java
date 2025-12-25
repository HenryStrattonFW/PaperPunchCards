package com.fatwednesday.paperpunchcards.items;

import com.fatwednesday.paperpunchcards.Config;
import com.fatwednesday.paperpunchcards.PaperPunchCards;
import com.fatwednesday.paperpunchcards.registration.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

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

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag)
    {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        var seq = stack.get(ModDataComponents.SIGNAL_SEQUENCE);
        if(seq != null && seq.isLaceSequence())
        {
            var label = PaperPunchCards.getTranslation("lace_card_warning");
            tooltipComponents.add(label);
        }
    }
}
