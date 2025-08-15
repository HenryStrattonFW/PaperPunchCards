package com.fatwednesday.paperpunchcards.gui;

import com.fatwednesday.fatlib.gui.components.FilterableSlot;
import com.fatwednesday.fatlib.gui.components.ObservableSlot;
import com.fatwednesday.fatlib.gui.components.OutputSlot;
import com.fatwednesday.fatlib.gui.menus.MenuWithInventory;
import com.fatwednesday.fatlib.payloads.FatLibMenuRenamePayload;
import com.fatwednesday.fatlib.utils.LogoutItemGuard;
import com.fatwednesday.paperpunchcards.PaperPunchCards;
import com.fatwednesday.paperpunchcards.items.PaperPunchable;
import com.fatwednesday.paperpunchcards.registration.ModDataComponents;
import com.fatwednesday.paperpunchcards.registration.ModMenus;
import com.fatwednesday.paperpunchcards.utils.NibbleStore;
import com.fatwednesday.paperpunchcards.utils.SignalSequence;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public class CardPuncherMenu
        extends MenuWithInventory
        implements FatLibMenuRenamePayload.Handler
{
    private static final int ContainerSlots = 2;
    public static final int MAX_NAME_LENGTH = 16;

    private static final int INPUT_INDEX = 0;
    private static final int OUTPUT_INDEX = 1;

    private Container container;
    private Player player;
    private OutputSlot outputSlot;
    private FilterableSlot inputSlot;
    // default to something empty even before we have an input.
    private NibbleStore sequenceData = new NibbleStore(20);
    private InputChangeListener changeListener;
    private String itemName;


    public CardPuncherMenu(int id, Inventory inventory)
    {
        super(ModMenus.CARD_PUNCHER_MENU.get(), id);
        initMenu(inventory);
    }

    public void initMenu(Inventory playerInventory)
    {
        container = new SimpleContainer(ContainerSlots);
        player = playerInventory.player;

        inputSlot = new FilterableSlot(container, 0, 64,  96);
        inputSlot.allow(PaperPunchable.class);
        inputSlot.setChangeListener(this::inputSlotChanged);
        addSlot(inputSlot);

        outputSlot = new OutputSlot(container, 1, 208, 96)
        {
            public void onTake(Player player, ItemStack stack)
            {
                CardPuncherMenu.this.onOutputTaken(player, stack);
            }
        };
        addSlot(outputSlot);

        CreateInventorySlots(playerInventory, 64, 142);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index)
    {
        var originalStack = ItemStack.EMPTY;
        var slot = slots.get(index);

        if (slot.hasItem())
        {
            var currentStack = slot.getItem();
            originalStack = currentStack.copy();

            if (index < 2)
            {
                // Container > Player
                if (!moveItemStackTo(currentStack, ContainerSlots, slots.size(), true))
                {
                    return ItemStack.EMPTY;
                }
            }
            else
            {
                // Player > Container
                if (!moveItemStackTo(currentStack, 0, ContainerSlots, false))
                {
                    return ItemStack.EMPTY;
                }
            }

            if (currentStack.isEmpty())
            {
                slot.set(ItemStack.EMPTY);
            }
            else
            {
                slot.setChanged();
            }
            if(index == OUTPUT_INDEX)
            {
                // fake the pull from the output slot onTake callback
                // so that we still trigger updates to input, screen, etc.
                onOutputTaken(player, currentStack);
            }
        }

        return originalStack;
    }

    @Override
    public boolean stillValid(Player player)
    {
        return true;
    }

    private void inputSlotChanged(ObservableSlot slot)
    {
        var data = getInputAsPunchable();

        LogoutItemGuard.clear(player);

        if(data.punchable != null)
        {
            LogoutItemGuard.queue(player, data.stack);

            NibbleStore newSeq = null;
            if(data.stack.has(ModDataComponents.SIGNAL_SEQUENCE))
            {
                var seq = data.stack.get(ModDataComponents.SIGNAL_SEQUENCE);
                if(seq != null)
                    newSeq = new NibbleStore(seq.bytes());
            }
            if(newSeq != null)
            {
                sequenceData = newSeq;
            }
            else
            {
                var targetSize = 20 * data.punchable.pageCount();
                if(sequenceData.size() != targetSize)
                {
                    sequenceData = new NibbleStore(20 * data.punchable.pageCount());
                }
            }
        }
        else
        {
            sequenceData.clear();
        }

        updateOutputItem();

        if(changeListener != null)
        {
            changeListener.inputChanged();
        }
    }

    private void onOutputTaken(Player player, ItemStack stack)
    {
        if(stack.has(DataComponents.CUSTOM_NAME))
        {
            PaperPunchCards.log("NAME ON OUTPUT: "+stack.get(DataComponents.CUSTOM_NAME).toString());
        }
        else
        {
            PaperPunchCards.log("NO CUSTOM NAME!");
        }

        if(inputSlot.hasItem())
        {
            inputSlot.getItem().shrink(1);
            if(changeListener != null)
                changeListener.inputChanged();
        }
        // May be able to repopulate right away.
        updateOutputItem();
    }

    public PaperPunchableStack getInputAsPunchable()
    {
        var stack = inputSlot.getItem();
        var item = stack.getItem();
        return (item instanceof PaperPunchable punchable)
                ? new PaperPunchableStack(stack, punchable)
                : new PaperPunchableStack(stack, null);
    }

    public void setChangeListener(InputChangeListener listener)
    {
        this.changeListener = listener;
    }

    public NibbleStore sequenceData()
    {
        return sequenceData;
    }

    public void tryCreateOutput(byte[] bytes)
    {
        if(player.level().isClientSide())
            return;

        var inputStack = container.getItem(inputSlot.index);
        if (inputStack.isEmpty())
            return;

        var output = container.getItem(outputSlot.index);
        if(output.isEmpty())
        {
            output = inputStack.copy();
            output.setCount(1);

            PaperPunchable.createAssignment()
                    .withSequence(new SignalSequence(bytes))
                    .withStack(output)
                    .assign();
        }
        else
        {
            output.grow(1);
        }
        container.removeItem(inputSlot.index, 1);

        outputSlot.set(output);
        container.setChanged();
        broadcastChanges();
    }

    @Override
    public void removed(Player player)
    {
        super.removed(player);

        if (player.level().isClientSide())
            return;

        for (var i = 0; i < container.getContainerSize(); ++i)
        {
            var stack = container.removeItemNoUpdate(i);
            if (stack.isEmpty())
                continue;
            player.getInventory().placeItemBackInInventory(stack);
        }
    }

    public boolean trySetSequenceData(byte[] bytes)
    {
        if(sequenceData == null || sequenceData.size() != bytes.length)
        {
            sequenceData = new NibbleStore(bytes);
        }
        else
        {
            sequenceData.copyFrom(bytes);
        }
        updateOutputItem();
        return true;
    }

    public boolean trySetItemName(String itemName)
    {
        var name = StringUtil.filterText(itemName);
        if (name.length() > MAX_NAME_LENGTH || name.equals(this.itemName))
        {
            return false;
        }

        if (outputSlot.hasItem())
        {
            this.itemName = name;
            var stack = outputSlot.getItem();
            if (StringUtil.isBlank(name))
            {
                stack.remove(DataComponents.CUSTOM_NAME);
            }
            else
            {
                stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
            }
        }
        else
        {
            this.itemName = "";
            return false;
        }
        updateOutputItem();
        return true;
    }

    @Override
    public void handleRenameData(String name)
    {
        PaperPunchCards.log("Applying name in handleRenameData: " + name);
        itemName = name;
        updateOutputItem();
    }

    public static void openMenuForPlayer( Player player)
    {
        player.openMenu(
            new SimpleMenuProvider(
                (id, inventory, p) -> new CardPuncherMenu(id, inventory),
                Component.empty()
            )
        );
    }

    private void updateOutputItem()
    {
        var inputStack = container.getItem(inputSlot.index);
        if (inputStack.isEmpty())
        {
            outputSlot.set(ItemStack.EMPTY);
            container.setChanged();
            broadcastChanges();
            return;
        }

        var output = inputStack.copy();
        output.setCount(1);

        var defaultNameComponent = Component.translatable(output.getDescriptionId());
        var defaultNameString = defaultNameComponent.getString();
        if (StringUtil.isBlank(itemName) || itemName.equals(defaultNameString))
        {
            output.remove(DataComponents.CUSTOM_NAME);
        }
        else
        {
            output.set(DataComponents.CUSTOM_NAME, Component.literal(itemName));
        }

        PaperPunchable.createAssignment()
                .withSequence(new SignalSequence(sequenceData.bytes()))
                .withStack(output)
                .assign();

        outputSlot.set(output);
        container.setChanged();
        broadcastChanges();
    }

    public String getItemName()
    {
        return itemName;
    }

    @FunctionalInterface
    public interface InputChangeListener
    {
        void inputChanged();
    }

    public record PaperPunchableStack(ItemStack stack, PaperPunchable punchable){}
}
