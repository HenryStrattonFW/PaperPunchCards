package com.fatwednesday.paperpunchcards.registration;

import com.fatwednesday.paperpunchcards.PaperPunchCards;
import com.fatwednesday.paperpunchcards.utils.SignalSequence;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponents
{
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
        DeferredRegister.createDataComponents(
                Registries.DATA_COMPONENT_TYPE,
                PaperPunchCards.MOD_ID
        );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SignalSequence>> SIGNAL_SEQUENCE =
        DATA_COMPONENTS.registerComponentType(
                "signal_sequence",
                builder -> builder
                        .persistent(SignalSequence.BASIC_CODEC)
                        .networkSynchronized(SignalSequence.BASIC_STREAM_CODEC)
        );

    public static void register(IEventBus modBus)
    {
        DATA_COMPONENTS.register(modBus);
    }

}
