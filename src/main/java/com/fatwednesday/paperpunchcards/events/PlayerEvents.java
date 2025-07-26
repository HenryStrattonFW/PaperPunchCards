package com.fatwednesday.paperpunchcards.events;

import com.fatwednesday.fatlib.utils.LogoutItemGuard;
import com.fatwednesday.paperpunchcards.PaperPunchCards;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class PlayerEvents
{
    @EventBusSubscriber(modid = PaperPunchCards.MOD_ID)
    private static class ModEvents
    {
        @SubscribeEvent
        public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event)
        {
            if (!(event.getEntity() instanceof ServerPlayer player))
                return;

            LogoutItemGuard.returnItemsToPlayer(player);
        }
    }
}
