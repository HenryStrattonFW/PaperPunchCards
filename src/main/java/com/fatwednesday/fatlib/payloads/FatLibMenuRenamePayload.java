package com.fatwednesday.fatlib.payloads;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record FatLibMenuRenamePayload(int containerId, String name) implements CustomPacketPayload
{
    public static final Type<FatLibMenuRenamePayload> TYPE =
            new Type<>(
                    ResourceLocation.fromNamespaceAndPath("fatlib","menu_rename_payload")
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, FatLibMenuRenamePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    FatLibMenuRenamePayload::containerId,
                    ByteBufCodecs.STRING_UTF8,
                    FatLibMenuRenamePayload::name,
                    FatLibMenuRenamePayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    public static void handleServerDataOnMain(final FatLibMenuRenamePayload payload, final IPayloadContext context)
    {
        context.enqueueWork(() ->
        {
            var player = context.player();
            if (player.containerMenu instanceof Handler handler &&
                player.containerMenu.containerId == payload.containerId)
            {
                handler.handleRenameData(payload.name);
            }
        });
    }


    @FunctionalInterface
    public interface Handler
    {
        void handleRenameData(String name);
    }
}
