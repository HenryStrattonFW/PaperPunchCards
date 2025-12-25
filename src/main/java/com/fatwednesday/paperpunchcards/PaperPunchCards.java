package com.fatwednesday.paperpunchcards;

import com.fatwednesday.paperpunchcards.registration.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Mod(PaperPunchCards.MOD_ID)
public class PaperPunchCards
{
    private static final String LOG_STRING = "[paperpunchcards] %s";
    public static final String MOD_ID = "paperpunchcards";
    public static final Logger LOGGER = LogUtils.getLogger();

    public PaperPunchCards(IEventBus modEventBus, ModContainer modContainer)
    {
        modEventBus.addListener(this::commonSetup);
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeTab.register(modEventBus);
        ModMenus.register(modEventBus);
        ModDataComponents.register(modEventBus);
        ModRecipes.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);

        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
    }

    public static ResourceLocation getResource(String path)
    {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static String getTag(String tag)
    {
        return ("%s:%s").formatted(MOD_ID, tag);
    }

    public static MutableComponent getTranslation(String key)
    {
        return Component.translatable("%s.%s".formatted(MOD_ID, key));
    }

    public static Component getTranslationFormatted(String key, Object... args)
    {
        return Component.translatable("%s.%s".formatted(MOD_ID, key), args);
    }

    public static void log(String message)
    {
        LOGGER.info(LOG_STRING.formatted(message));
    }

    public static void error(String message)
    {
        LOGGER.error(LOG_STRING.formatted(message));
    }

    private void commonSetup(FMLCommonSetupEvent event)
    {
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
    }

    @EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            ModItems.onClientSetup(event);
        }

        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event)
        {
            ModBlocks.registerEntityRenderers(event);
        }

        @SubscribeEvent
        public static void onRegisterPayloadEvents(RegisterPayloadHandlersEvent event)
        {
            ModNetworking.registerPayloads(event);
        }

        @SubscribeEvent
        public static void onRegisterScreens(RegisterMenuScreensEvent event)
        {
            ModMenus.onRegisterScreens(event);
        }
    }
}
