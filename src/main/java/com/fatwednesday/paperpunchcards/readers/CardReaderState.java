package com.fatwednesday.paperpunchcards.readers;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum CardReaderState implements StringRepresentable
{
    UNSET("unset"),
    EMPTY("empty"),
    BAD("bad"),
    GOOD("good");

    private final String name;

    CardReaderState(String name)
    {
        this.name = name;
    }

    @Override
    public @NotNull String getSerializedName()
    {
        return name;
    }
}