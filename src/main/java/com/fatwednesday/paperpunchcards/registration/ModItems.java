package com.fatwednesday.paperpunchcards.registration;

import com.fatwednesday.paperpunchcards.PaperPunchCards;
import com.fatwednesday.paperpunchcards.items.PaperTapeItem;
import com.fatwednesday.paperpunchcards.items.PunchCardItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems
{
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(PaperPunchCards.MOD_ID);

    public static final DeferredItem<BlockItem> TAPE_PLAYER_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.TAPE_PLAYER_BLOCK);
    public static final DeferredItem<BlockItem> CARD_PUNCHER_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.CARD_PUNCHER_BLOCK);
    public static final DeferredItem<BlockItem> CARD_READER_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.CARD_READER_BLOCK);
    public static final DeferredItem<BlockItem> GUILLOTINE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.GUILLOTINE_BLOCK);

    public static final DeferredItem<Item> PUNCH_CARD_ITEM = ITEMS.registerItem("punch_card_item", PunchCardItem::new);
    public static final DeferredItem<Item> PAPER_TAPE_ITEM = ITEMS.registerItem("paper_tape_item", PaperTapeItem::new);

    public static void register(IEventBus modBus)
    {
        ITEMS.register(modBus);
    }

    public static void onClientSetup(FMLClientSetupEvent event)
    {
        ItemProperties.register(
                ModItems.PUNCH_CARD_ITEM.get(),
                PaperPunchCards.getResource("has_data"),
                ModItems::hasDataCondition
        );

        ItemProperties.register(
                ModItems.PAPER_TAPE_ITEM.get(),
                PaperPunchCards.getResource("has_data"),
                ModItems::hasDataCondition
        );
    }

    private static float hasDataCondition(ItemStack stack, ClientLevel level, LivingEntity entity, int seed)
    {
        var seq = stack.get(ModDataComponents.SIGNAL_SEQUENCE);
        return (seq == null || seq.isBlank()) ? 0.0f : 1.0f;
    }
}
