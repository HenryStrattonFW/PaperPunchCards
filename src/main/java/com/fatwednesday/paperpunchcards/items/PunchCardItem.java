package com.fatwednesday.paperpunchcards.items;

import com.fatwednesday.paperpunchcards.readers.CardReaderBlock;
import com.fatwednesday.paperpunchcards.readers.CardReaderBlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.NotNull;

public class PunchCardItem extends Item implements PaperPunchable
{
    public PunchCardItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public boolean showAsCards()
    {
        return true;
    }

    @Override
    public int pageCount()
    {
        return 1;
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context)
    {
        var player = context.getPlayer();
        if(player == null)
            return InteractionResult.PASS;

        var level = context.getLevel();
        var pos = context.getClickedPos();
        var blockEntity = level.getBlockEntity(pos);
        var state = level.getBlockState(pos);
        var stack = player.getMainHandItem();

        if(!(blockEntity instanceof CardReaderBlockEntity readerEntity))
            return InteractionResult.PASS;

        if (player.isCrouching() && state.getBlock() instanceof CardReaderBlock reader)
        {
            reader.tryConfigureReader(player, stack, level, readerEntity);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
