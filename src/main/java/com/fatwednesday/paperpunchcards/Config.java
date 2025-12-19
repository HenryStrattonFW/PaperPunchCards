package com.fatwednesday.paperpunchcards;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.ModConfigSpec;

@Mod(value = PaperPunchCards.MOD_ID,  dist = Dist.CLIENT)
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

    public Config(IEventBus modEventBus, ModContainer modContainer)
    {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
