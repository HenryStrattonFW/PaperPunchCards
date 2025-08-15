package com.fatwednesday.paperpunchcards.registration;

import com.fatwednesday.fatlib.payloads.FatLibMenuRenamePayload;
import com.fatwednesday.paperpunchcards.payloads.CardPunchMenuDataPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public class ModNetworking
{
    public static void registerPayloads(RegisterPayloadHandlersEvent event)
    {
        final var registrar = event.registrar("1");

        registrar.playToServer(
                FatLibMenuRenamePayload.TYPE,
                FatLibMenuRenamePayload.STREAM_CODEC,
                FatLibMenuRenamePayload::handleServerDataOnMain
        );

        registrar.playToServer(
                CardPunchMenuDataPayload.TYPE,
                CardPunchMenuDataPayload.STREAM_CODEC,
                CardPunchMenuDataPayload::handleServerDataOnMain
        );
    }
}
