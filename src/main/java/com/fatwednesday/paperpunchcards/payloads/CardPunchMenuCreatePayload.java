package com.fatwednesday.paperpunchcards.payloads;

import com.fatwednesday.paperpunchcards.PaperPunchCards;
import com.fatwednesday.paperpunchcards.gui.CardPuncherMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record CardPunchMenuCreatePayload(int containerId, byte[] bytes) implements CustomPacketPayload
{
    public static final Type<CardPunchMenuCreatePayload> TYPE =
            new Type<>(
                    PaperPunchCards.getResource("card_punch_menu_create")
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, CardPunchMenuCreatePayload> STREAM_CODEC =
            StreamCodec.composite(
                ByteBufCodecs.INT,
                CardPunchMenuCreatePayload::containerId,
                ByteBufCodecs.BYTE_ARRAY,
                CardPunchMenuCreatePayload::bytes,
                CardPunchMenuCreatePayload::new
        );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    public static void handleServerDataOnMain(final CardPunchMenuCreatePayload payload, final IPayloadContext context)
    {
        context.enqueueWork(() ->
        {
            var player = context.player();
            if (player.containerMenu instanceof CardPuncherMenu menu && menu.containerId == payload.containerId)
            {
                menu.tryCreateOutput(payload.bytes);
            }
        });
    }
}