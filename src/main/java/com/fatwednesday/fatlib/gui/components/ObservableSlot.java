package com.fatwednesday.fatlib.gui.components;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

public class ObservableSlot extends Slot
{
    private SlotChangeListener changeListener;

    public ObservableSlot(Container container, int index, int x, int y)
    {
        super(container, index, x, y);
    }

    public void setChangeListener(SlotChangeListener changeListener)
    {
        this.changeListener = changeListener;
    }

    @Override
    public void setChanged()
    {
        super.setChanged();
        if(changeListener != null)
        {
            changeListener.slotChanged(this);
        }
    }

    @FunctionalInterface
    public interface SlotChangeListener
    {
        void slotChanged(ObservableSlot slot);
    }
}
