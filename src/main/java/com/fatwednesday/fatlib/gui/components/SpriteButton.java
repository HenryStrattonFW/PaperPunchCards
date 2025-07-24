package com.fatwednesday.fatlib.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class SpriteButton extends Button
{
    private final ResourceLocation normalSprite;
    private final ResourceLocation focusSprite;
    private final ResourceLocation pressedSprite;
    private final ResourceLocation disabledSprite;

    private boolean isPressed;

    protected SpriteButton(
            int x, int y,
            int width, int height,
            Component message,
            OnPress onPress,
            ResourceLocation normalSprite,
            ResourceLocation focusSprite,
            ResourceLocation pressedSprite,
            ResourceLocation disabledSprite)
    {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        this.normalSprite = normalSprite;
        this.focusSprite = focusSprite;
        this.pressedSprite = pressedSprite;
        this.disabledSprite = disabledSprite;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        isPressed = true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onRelease(double mouseX, double mouseY)
    {
        super.onRelease(mouseX, mouseY);
        isPressed = false;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        guiGraphics.blitSprite(getDesiredSprite(), getX(), getY(), getWidth(), getHeight());
    }

    private ResourceLocation getDesiredSprite()
    {
        var bestCase = normalSprite;
        if(!isActive())
        {
            bestCase = disabledSprite;
        }
        else if(isPressed)
        {
            bestCase = pressedSprite;
        }
        else if(isHovered())
        {
            bestCase = focusSprite;
        }
        // Fall back to normal if we don't have a sprite.
        // assume we should ALWAYS have a normal sprite;
        return bestCase == null
                ? normalSprite
                : bestCase;
    }

    public static class Builder
    {
        private int x, y, width, height;
        private OnPress onPress;
        private ResourceLocation normalSprite;
        private ResourceLocation focusSprite;
        private ResourceLocation pressedSprite;
        private ResourceLocation disabledSprite;
        private Component label;

        public Builder(){}

        public Builder withLabel(Component label)
        {
            this.label = label;
            return this;
        }

        public Builder withBounds(int x, int y, int width, int height)
        {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder withNormalSprite(ResourceLocation normalSprite)
        {
            this.normalSprite = normalSprite;
            return this;
        }

        public Builder withPressedSprite(ResourceLocation pressedSprite)
        {
            this.pressedSprite = pressedSprite;
            return this;
        }

        public Builder withFocusedSprite(ResourceLocation focusSprite)
        {
            this.focusSprite = focusSprite;
            return this;
        }

        public Builder withDisabledSprite(ResourceLocation disabledSprite)
        {
            this.disabledSprite = disabledSprite;
            return this;
        }

        public Builder withSprites(ResourceLocation normalSprite, ResourceLocation focusSprite, ResourceLocation disabledSprite)
        {
            this.normalSprite = normalSprite;
            this.focusSprite = focusSprite;
            this.disabledSprite = disabledSprite;
            return this;
        }

        public Builder withCallback(OnPress onPress)
        {
            this.onPress = onPress;
            return this;
        }

        public SpriteButton build()
        {
            return new SpriteButton(
                    x, y,
                    width, height,
                    label,
                    onPress,
                    normalSprite,
                    focusSprite,
                    pressedSprite,
                    disabledSprite
            );
        }
    }
}
