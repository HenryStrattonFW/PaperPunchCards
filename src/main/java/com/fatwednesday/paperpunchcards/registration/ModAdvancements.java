package com.fatwednesday.paperpunchcards.registration;

import com.fatwednesday.paperpunchcards.PaperPunchCards;
import com.fatwednesday.paperpunchcards.advancement.ModEventTrigger;
import com.fatwednesday.paperpunchcards.advancement.PunchedItemTrigger;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModAdvancements
{
    public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS = DeferredRegister.create(
        Registries.TRIGGER_TYPE,
        PaperPunchCards.MOD_ID
    );

    public static final DeferredHolder<CriterionTrigger<?>, PunchedItemTrigger> PUNCHED_ITEM_TRIGGER =
            TRIGGERS.register("punched_item", PunchedItemTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, ModEventTrigger> MOD_EVENT_TRIGGER =
            TRIGGERS.register("mod_event", ModEventTrigger::new);

    public static final ResourceLocation LACE_CREATED_EVENT = PaperPunchCards.getResource("lace_created");
    public static final ResourceLocation READER_CONFIGURED_EVENT = PaperPunchCards.getResource("reader_configured");

    public static void register(IEventBus modBus)
    {
        TRIGGERS.register(modBus);
    }
}
