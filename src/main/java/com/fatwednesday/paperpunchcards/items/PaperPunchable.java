package com.fatwednesday.paperpunchcards.items;

import com.fatwednesday.paperpunchcards.registration.ModDataComponents;
import com.fatwednesday.paperpunchcards.PaperPunchCards;
import com.fatwednesday.paperpunchcards.payloads.ItemStackSyncPayload;
import com.fatwednesday.paperpunchcards.utils.SignalSequence;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public interface PaperPunchable
{
    boolean showAsCards();
    int pageCount();

    static void Assign(ItemStack stack, SignalSequence signal, boolean sync)
    {
        var item = stack.getItem();
        if(!(item instanceof PaperPunchable))
        {
            PaperPunchCards.error("Invalid item stack, must be stack of PaperPunchable");
            return;
        }
        stack.set(ModDataComponents.SIGNAL_SEQUENCE, signal);
        if(sync)
        {
            PacketDistributor.sendToServer(new ItemStackSyncPayload(stack));
        }
    }
}
