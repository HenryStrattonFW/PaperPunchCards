package com.fatwednesday.fatlib.gui.components;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class OutputSlot extends ObservableSlot
{
    private final OutputTakeListener takeCallback;

    public OutputSlot(Container container, int slot, int x, int y, OutputTakeListener takeCallback)
    {
        super(container, slot, x, y);
        this.takeCallback = takeCallback;
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack)
    {
        return false;
    }


    @Override
    public void onTake(Player player, ItemStack stack)
    {
        if(takeCallback != null)
        {
            takeCallback.onTake(player, stack);
        }
        super.onTake(player, stack);
    }

    @FunctionalInterface
    public interface OutputTakeListener
    {
        void onTake(Player player, ItemStack stack);
    }
}
