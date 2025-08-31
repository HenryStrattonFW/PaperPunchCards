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
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CardPuncherMenu
        extends MenuWithInventory
        implements FatLibMenuRenamePayload.Handler
{
    public static final int MAX_NAME_LENGTH = 16;

    private static final int INPUT_INDEX = 0;
    private static final int OUTPUT_INDEX = 1;
    private static final int INV_INDEX_START = 2;
    private static final int HOTBAR_INDEX_START = INV_INDEX_START + 27;
    private static final int HOTBAR_INDEX_END = HOTBAR_INDEX_START + 9;

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
        container = new SimpleContainer(2);
        player = playerInventory.player;

        inputSlot = new FilterableSlot(container, 0, 64,  96);
        inputSlot.allow(PaperPunchable.class);
        inputSlot.setChangeListener(this::inputSlotChanged);
        addSlot(inputSlot);

        outputSlot = new OutputSlot(container, 1, 208, 96, this::onOutputTaken);
        addSlot(outputSlot);

        CreateInventorySlots(playerInventory, 64, 142);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index)
    {
        var slot = slots.get(index);
        if (!slot.hasItem())
            return ItemStack.EMPTY;

        var rawStack = slot.getItem();
        var quickMoveStack = rawStack.copy();

        if(index == OUTPUT_INDEX)
        {
            if(!this.moveItemStackTo(rawStack, INV_INDEX_START, HOTBAR_INDEX_END, true))
            {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(rawStack, quickMoveStack);
        }
        else if (index == INPUT_INDEX)
        {
            if (!moveItemStackTo(rawStack, INV_INDEX_START, HOTBAR_INDEX_END, true))
            {
                return ItemStack.EMPTY;
            }
        }
        else
        {
            // Player > Container
            if (!moveItemStackTo(rawStack, 0, 1, false))
            {
                return ItemStack.EMPTY;
            }
        }

        if (rawStack.isEmpty())
        {
            slot.set(ItemStack.EMPTY);
        }
        else
        {
            slot.setChanged();
        }

        if (rawStack.getCount() == quickMoveStack.getCount())
        {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, rawStack);

        broadcastChanges();
        return quickMoveStack;
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

        updateOutputSlotContents();

        if(changeListener != null)
        {
            changeListener.inputChanged();
        }
    }

    private void onOutputTaken(Player player, ItemStack stack)
    {
        stack.onCraftedBy(player.level(), player, stack.getCount());

        inputSlot.remove(1);
        if(changeListener != null)
            changeListener.inputChanged();

        // May be able to repopulate right away.
        updateOutputSlotContents();
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

    @Override
    public void removed(Player player)
    {
        super.removed(player);

        if (player.level().isClientSide())
            return;

        var stack = container.removeItemNoUpdate(INPUT_INDEX);
        if (!stack.isEmpty())
        {
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
        updateOutputSlotContents();
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
        updateOutputSlotContents();
        return true;
    }

    @Override
    public void handleRenameData(String name)
    {
        PaperPunchCards.log("Applying name in handleRenameData: " + name);
        itemName = name;
        updateOutputSlotContents();
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

    private void updateOutputSlotContents()
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
