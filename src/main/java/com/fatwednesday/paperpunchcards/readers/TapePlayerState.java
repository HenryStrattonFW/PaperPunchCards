package com.fatwednesday.paperpunchcards.readers;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum TapePlayerState implements StringRepresentable
{
    EMPTY("empty"),
    FULL("full"),
    ACTIVE("active");

    private final String name;

    TapePlayerState(String name)
    {
        this.name = name;
    }

    @Override
    public @NotNull String getSerializedName()
    {
        return this.name;
    }
}