package com.fatwednesday.paperpunchcards.items;

import com.fatwednesday.paperpunchcards.registration.ModDataComponents;
import com.fatwednesday.paperpunchcards.PaperPunchCards;
import com.fatwednesday.paperpunchcards.payloads.ItemStackSyncPayload;
import com.fatwednesday.paperpunchcards.utils.NibbleStore;
import com.fatwednesday.paperpunchcards.utils.SignalSequence;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public interface PaperPunchable
{
    boolean showAsCards();
    int pageCount();

    static Assignment createAssignment()
    {
        return new Assignment();
    }

    class Assignment
    {
        private ItemStack stack;
        private InteractionHand hand;
        private boolean syncStack;
        private SignalSequence sequence;

        public Assignment()
        {
            this.stack = ItemStack.EMPTY;
            this.hand = InteractionHand.MAIN_HAND;
            this.syncStack = false;
        }

        public Assignment withSequence(SignalSequence sequence)
        {
            this.sequence = sequence;
            return this;
        }

        public Assignment syncStackInHand(InteractionHand hand)
        {
            this.hand = hand;
            this.syncStack = true;
            return this;
        }

        public Assignment withStack(ItemStack stack)
        {
            this.stack = stack;
            return this;
        }

        public void assign()
        {
            if(stack.isEmpty())
            {
                PaperPunchCards.error("Stack is empty, cannot assign sequence");
                return;
            }
            if(sequence == null)
            {
                PaperPunchCards.error("Sequence is null, cannot assign.");
                return;
            }

            var item = stack.getItem();
            if(!(item instanceof PaperPunchable))
            {
                PaperPunchCards.error("Invalid item stack, must be stack of PaperPunchable");
                return;
            }

            stack.set(ModDataComponents.SIGNAL_SEQUENCE, sequence);

            if(syncStack)
            {
                PacketDistributor.sendToServer(
                        new ItemStackSyncPayload(
                                stack,
                                hand == InteractionHand.MAIN_HAND
                        )
                );
            }
        }
    }
}
