package com.fatwednesday.paperpunchcards.registration;

import com.fatwednesday.paperpunchcards.payloads.CardPunchMenuCreatePayload;
import com.fatwednesday.paperpunchcards.payloads.ItemStackSyncPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public class ModNetworking
{
    public static void registerPayloads(RegisterPayloadHandlersEvent event)
    {
        final var registrar = event.registrar("1");

        registrar.playToServer(
                ItemStackSyncPayload.TYPE,
                ItemStackSyncPayload.STREAM_CODEC,
                ItemStackSyncPayload::handleServerDataOnMain
        );

        registrar.playToServer(
                CardPunchMenuCreatePayload.TYPE,
                CardPunchMenuCreatePayload.STREAM_CODEC,
                CardPunchMenuCreatePayload::handleServerDataOnMain
        );
    }
}
