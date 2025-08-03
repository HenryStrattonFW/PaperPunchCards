package com.fatwednesday.paperpunchcards.readers;

import com.fatwednesday.paperpunchcards.PaperPunchCards;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum TapePlayerMode implements StringRepresentable
{
    LOOP("loop"),
    PLAY_ONCE("play_once"),
    STEP("step");

    private final String name;

    TapePlayerMode(String name)
    {
        this.name = name;
    }

    @Override
    public @NotNull String getSerializedName()
    {
        return name;
    }

    public Component getMessage()
    {
        return PaperPunchCards.getTranslation("message.player_mode_"+name);
    }

    public TapePlayerMode cycle()
    {
        if(this == LOOP)
            return PLAY_ONCE;
        if(this == PLAY_ONCE)
            return STEP;

        return LOOP;
    }

    public static TapePlayerMode tryGetValue(String value ,TapePlayerMode fallback)
    {
        try
        {
            return TapePlayerMode.valueOf(value);
        }
        catch (IllegalArgumentException e)
        {
            return fallback;
        }
    }
}