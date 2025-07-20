package com.fatwednesday.fatlib.utils;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LogoutItemGuard
{
    private static final Map<UUID, ArrayList<ItemStack>> protectedStacks = new HashMap<>();

    public static void queue(Player player, ItemStack stack)
    {
        if(player == null)
            return;
        queue(player.getUUID(), stack);
    }

    public static void queue(UUID player, ItemStack stack)
    {
        if (stack == null || stack.isEmpty())
            return;

        if(!protectedStacks.containsKey(player))
            protectedStacks.put(player, new ArrayList<>());

        var stacks = protectedStacks.get(player);
        stacks.add(stack.copy());
    }

    public static void clear(Player player)
    {
        clear(player.getUUID());
    }

    public static void clear(UUID player)
    {
        protectedStacks.remove(player);
    }

    public static void returnItemsToPlayer(Player player)
    {
        var stacks = protectedStacks.remove(player.getUUID());
        if(stacks == null)
            return;

        var inventory = player.getInventory();
        for(var stack : stacks)
            inventory.placeItemBackInInventory(stack);

        player.getInventory().setChanged();
    }
}
