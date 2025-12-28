package com.fatwednesday.paperpunchcards.advancement;

import com.fatwednesday.paperpunchcards.PaperPunchCards;
import com.fatwednesday.paperpunchcards.registration.ModDataComponents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class ModEventTrigger extends SimpleCriterionTrigger<ModEventTrigger.TriggerInstance>
{
    public static final ResourceLocation ID = PaperPunchCards.getResource("example_trigger");

    @Override
    public Codec<TriggerInstance> codec()
    {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, ResourceLocation triggeredEvent)
    {
        PaperPunchCards.log(("Testing mod event trigger: %s").formatted(triggeredEvent.toString()));
        this.trigger(player, inst -> inst.matches(triggeredEvent));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, ResourceLocation event) implements SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                        ResourceLocation.CODEC.fieldOf("event").forGetter(TriggerInstance::event)
                ).apply(instance, TriggerInstance::new)
        );

        public boolean matches(ResourceLocation triggeredEvent)
        {
            return triggeredEvent.equals(event);
        }
    }
}
