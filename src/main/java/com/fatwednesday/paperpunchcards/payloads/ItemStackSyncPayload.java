package com.fatwednesday.paperpunchcards.payloads;

import com.fatwednesday.paperpunchcards.PaperPunchCards;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record ItemStackSyncPayload(ItemStack stack, boolean mainHand) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<ItemStackSyncPayload> TYPE = new CustomPacketPayload.Type<>(
                    PaperPunchCards.getResource("item_stack_sync")
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemStackSyncPayload> STREAM_CODEC =
        StreamCodec.of(
                (buf, payload) -> {
                    ItemStack.STREAM_CODEC.encode(buf, payload.stack());
                    buf.writeBoolean(payload.mainHand());
                },
                buf -> new ItemStackSyncPayload(
                        ItemStack.STREAM_CODEC.decode(buf),
                        buf.readBoolean()
                )
        );


    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    public static void handleServerDataOnMain(final ItemStackSyncPayload payload, final IPayloadContext context)
    {
        context.enqueueWork(() ->
        {
            var player = context.player();
            player.setItemInHand(
                    payload.mainHand
                            ? InteractionHand.MAIN_HAND
                            : InteractionHand.OFF_HAND,
                    payload.stack()
            );
        });
    }
}
