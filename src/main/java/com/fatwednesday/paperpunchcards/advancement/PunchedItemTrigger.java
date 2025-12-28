package com.fatwednesday.paperpunchcards.advancement;

import com.fatwednesday.paperpunchcards.PaperPunchCards;
import com.fatwednesday.paperpunchcards.compat.jei.JEIPaperPunchCardsPlugin;
import com.fatwednesday.paperpunchcards.registration.ModDataComponents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class PunchedItemTrigger extends SimpleCriterionTrigger<PunchedItemTrigger.TriggerInstance>
{
    public static final ResourceLocation ID = PaperPunchCards.getResource("punched_item_trigger");

    @Override
    public Codec<TriggerInstance> codec()
    {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, ItemStack itemStack)
    {
        this.trigger(player, inst -> inst.matches(itemStack));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, ItemPredicate item) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                        ItemPredicate.CODEC.fieldOf("item").forGetter(TriggerInstance::item)
                ).apply(instance, TriggerInstance::new)
        );

        public boolean matches(ItemStack itemStack)
        {
            if (itemStack != null)
            {
                PaperPunchCards.log("Testing item for PunchedItemTrigger match.");

                if (itemStack.has(ModDataComponents.SIGNAL_SEQUENCE))
                {
                    var seq = itemStack.get(ModDataComponents.SIGNAL_SEQUENCE);
                    if (seq != null && !seq.isBlank())
                        return true;
                }
            }
            return false;
        }
    }
}
