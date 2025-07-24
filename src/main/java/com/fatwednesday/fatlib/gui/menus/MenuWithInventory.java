package com.fatwednesday.fatlib.gui.menus;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;

public abstract class MenuWithInventory extends AbstractContainerMenu
{
    protected static final int Default_Inventory_X = 8;
    protected static final int Default_Inventory_Y = 84;

    protected MenuWithInventory(MenuType<?> menuType, int containerId)
    {
        super(menuType, containerId);
    }

    public final void CreateInventorySlots(Inventory playerInventory)
    {
        CreateInventorySlots(playerInventory, Default_Inventory_X, Default_Inventory_Y);
    }

    public final void CreateInventorySlots(Inventory playerInventory, int x, int y)
    {
        // Inventory
        for (var row = 0; row < 3; ++row)
        {
            for (var col = 0; col < 9; ++col)
            {
                this.addSlot(
                    new Slot(
                        playerInventory,
                        col + row * 9 + 9,
                        x + col * 18,
                        y + row * 18
                    )
                );
            }
        }

        // Hotbar
        for (var col = 0; col < 9; ++col)
        {
            this.addSlot(new Slot(playerInventory, col, x + col * 18, y + 58));
        }
    }
}
