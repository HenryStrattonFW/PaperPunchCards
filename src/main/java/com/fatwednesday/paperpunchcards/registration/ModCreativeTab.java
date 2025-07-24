package com.fatwednesday.paperpunchcards.registration;

import com.fatwednesday.paperpunchcards.PaperPunchCards;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTab
{
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(
            Registries.CREATIVE_MODE_TAB,
            PaperPunchCards.MOD_ID
    );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CREATIVE_TAB = CREATIVE_MODE_TABS.register(
            "paperpunchcards_tab",
            ()->CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.paperpunchcards"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(()->ModItems.TAPE_PLAYER_BLOCK_ITEM.get().getDefaultInstance())
                    .displayItems((parameters, output)->{
                        output.accept(ModBlocks.TAPE_PLAYER_BLOCK.get());
                        output.accept(ModBlocks.CARD_PUNCHER_BLOCK.get());
                        output.accept(ModBlocks.CARD_READER_BLOCK.get());
                        output.accept(ModItems.PAPER_TAPE_ITEM.get());
                        output.accept(ModItems.PUNCH_CARD_ITEM.get());
                    })
                    .build()
    );

    public static void register(IEventBus modBus)
    {
        CREATIVE_MODE_TABS.register(modBus);
    }
}
