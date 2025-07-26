package com.fatwednesday.fatlib.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public record GuiTexture(ResourceLocation resource, int width, int height)
{
    public float halfWidth()
    {
        return width * 0.5f;
    }

    public float halfHeight()
    {
        return height * 0.5f;
    }

    public void blit(GuiGraphics graphics, int x, int y)
    {
        graphics.blit(
                resource,
                x, y,
                0,0,
                width, height,
                width, height
        );
    }

}
