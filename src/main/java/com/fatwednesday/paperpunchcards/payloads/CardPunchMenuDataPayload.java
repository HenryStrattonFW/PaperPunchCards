package com.fatwednesday.paperpunchcards.payloads;

import com.fatwednesday.paperpunchcards.PaperPunchCards;
import com.fatwednesday.paperpunchcards.gui.CardPuncherMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CardPunchMenuDataPayload(int containerId, byte[] bytes) implements CustomPacketPayload
{
    public static final Type<CardPunchMenuDataPayload> TYPE =
            new Type<>(
                    PaperPunchCards.getResource("card_punch_menu_data")
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, CardPunchMenuDataPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    CardPunchMenuDataPayload::containerId,
                    ByteBufCodecs.BYTE_ARRAY,
                    CardPunchMenuDataPayload::bytes,
                    CardPunchMenuDataPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    public static void handleServerDataOnMain(final CardPunchMenuDataPayload payload, final IPayloadContext context)
    {
        context.enqueueWork(() ->
        {
            var player = context.player();
            if (player.containerMenu instanceof CardPuncherMenu menu &&
                menu.containerId == payload.containerId)
            {
                menu.trySetSequenceData(payload.bytes);
            }
        });
    }
}
