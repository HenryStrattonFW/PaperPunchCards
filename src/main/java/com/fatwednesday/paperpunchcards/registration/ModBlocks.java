package com.fatwednesday.paperpunchcards.registration;

import com.fatwednesday.paperpunchcards.PaperPunchCards;
import com.fatwednesday.paperpunchcards.crafting.CardPuncherBlock;
import com.fatwednesday.paperpunchcards.readers.CardReaderBlock;
import com.fatwednesday.paperpunchcards.readers.TapePlayerBlock;
import com.fatwednesday.paperpunchcards.readers.TapePlayerBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks
{
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(PaperPunchCards.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
            Registries.BLOCK_ENTITY_TYPE,
            PaperPunchCards.MOD_ID
    );

    public static final DeferredBlock<Block> CARD_PUNCHER_BLOCK = BLOCKS.registerBlock(
            "card_puncher_block",
            CardPuncherBlock::new,
            BlockBehaviour.Properties.of()
                    .strength(1.0f)
    );

    public static final DeferredBlock<Block> CARD_READER_BLOCK = BLOCKS.registerBlock(
            "card_reader_block",
            CardReaderBlock::new,
            BlockBehaviour.Properties.of()
                    .strength(1.0f)
    );

    public static final DeferredBlock<Block> TAPE_PLAYER_BLOCK = BLOCKS.registerBlock(
            "tape_player_block",
            TapePlayerBlock::new,
            BlockBehaviour.Properties.of()
                    .strength(1.0f)
    );

    public static final Supplier<BlockEntityType<TapePlayerBlockEntity>> TAPE_PLAYER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register(
            "tape_player_block_entity",() ->
                            BlockEntityType.Builder.of(
                                    TapePlayerBlockEntity::new,
                                    ModBlocks.TAPE_PLAYER_BLOCK.get()
                            ).build(null)
            );


    public static void register(IEventBus modBus)
    {
        BLOCKS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
    }

    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event)
    {

    }
}
