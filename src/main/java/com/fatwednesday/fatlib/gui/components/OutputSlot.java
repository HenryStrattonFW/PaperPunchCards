package com.fatwednesday.fatlib.gui.components;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class OutputSlot extends ObservableSlot
{
    public OutputSlot(Container container, int slot, int x, int y)
    {
        super(container, slot, x, y);
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack)
    {
        return false;
    }
}
