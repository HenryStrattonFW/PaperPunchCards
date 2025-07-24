package com.fatwednesday.paperpunchcards.registration;

import com.fatwednesday.paperpunchcards.PaperPunchCards;
import com.fatwednesday.paperpunchcards.gui.CardPuncherMenu;
import com.fatwednesday.paperpunchcards.gui.CardPuncherScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModMenus
{
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(
            Registries.MENU,
            PaperPunchCards.MOD_ID
    );

    public static final Supplier<MenuType<CardPuncherMenu>> CARD_PUNCHER_MENU = MENU_TYPES.register(
            "card_puncher_menu",
            () -> new MenuType<>(
                    CardPuncherMenu::new,
                    FeatureFlags.DEFAULT_FLAGS
            )
    );

    public static void register(IEventBus modEventBus)
    {
        MENU_TYPES.register(modEventBus);
    }

    public static void onRegisterScreens(RegisterMenuScreensEvent event)
    {
        event.register(ModMenus.CARD_PUNCHER_MENU.get(), CardPuncherScreen::new);
    }
}
