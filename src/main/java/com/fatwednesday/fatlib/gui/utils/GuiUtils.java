package com.fatwednesday.fatlib.gui.utils;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class GuiUtils
{
    public static void drawCenteredLabel(GuiGraphics guiGraphics, Font font, Component text, int x, int y, int colour, boolean dropShadow)
    {
        int textWidth = font.width(text);
        guiGraphics.drawString(
                font,
                text,
                x - (textWidth / 2),
                y,
                colour,
                dropShadow
        );
    }
}
