package com.fatwednesday.paperpunchcards.items;

import com.fatwednesday.paperpunchcards.PaperPunchCards;
import com.fatwednesday.paperpunchcards.readers.CardReaderBlock;
import com.fatwednesday.paperpunchcards.readers.CardReaderBlockEntity;
import com.fatwednesday.paperpunchcards.registration.ModDataComponents;
import com.fatwednesday.paperpunchcards.utils.NibbleStore;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand)
    {
        var stack = player.getItemInHand(usedHand);
        if(!stack.has(ModDataComponents.SIGNAL_SEQUENCE))
        {
            player.displayClientMessage(
                    Component.literal("No signal data"),
                    true
            );
        }
        return super.use(level, player, usedHand);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag)
    {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        var seq = stack.get(ModDataComponents.SIGNAL_SEQUENCE);
        if(seq!= null && seq.isLaceSequence())
        {
            var label = PaperPunchCards.getTranslation("lace_card_warning");
            tooltipComponents.add(label);
        }
    }
}
