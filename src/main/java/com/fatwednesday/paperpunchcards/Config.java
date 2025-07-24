package com.fatwednesday.paperpunchcards;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config
{
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.ConfigValue<Integer> PaperTapePageCount;
    public static final ModConfigSpec.ConfigValue<Integer> CardReaderDelayTicks;

    static
    {
        var builder = new ModConfigSpec.Builder();

        PaperTapePageCount = builder.define("PaperTapePageCount",4);
        CardReaderDelayTicks = builder.define("CardReaderDelayTicks",15);

        SPEC = builder.build();
    }
}
