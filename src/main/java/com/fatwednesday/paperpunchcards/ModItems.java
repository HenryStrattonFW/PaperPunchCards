package com.fatwednesday.paperpunchcards;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems
{
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(PaperPunchCards.MOD_ID);

    public static void register(IEventBus modBus)
    {
        ITEMS.register(modBus);
    }

    public static void onClientSetup(FMLClientSetupEvent event)
    {

    }
}
