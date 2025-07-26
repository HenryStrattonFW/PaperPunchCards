package com.fatwednesday.fatlib.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public class SpriteToggle extends Button
{
    private static boolean draggingActive;
    private static boolean draggingState;

    private final ResourceLocation normalSprite;
    private final ResourceLocation toggledSprite;
    private final Consumer<Boolean> onStateChanged;

    private boolean toggled;

    protected SpriteToggle(
            int x, int y,
            int width, int height,
            Consumer<Boolean> onStateChanged,
            boolean initialState,
            ResourceLocation normalSprite,
            ResourceLocation toggledSprite)
    {
        super(x, y, width, height, Component.empty(), (b)->{}, DEFAULT_NARRATION);
        this.normalSprite = normalSprite;
        this.toggledSprite = toggledSprite;
        this.onStateChanged = onStateChanged;
        this.toggled = initialState;
    }

    @Override
    public void onPress()
    {
        setValue(!toggled);
        draggingState = toggled;
        draggingActive = true;
    }


    @Override
    public void onRelease(double mouseX, double mouseY)
    {
        draggingActive = false;
    }

    public void setValueWithoutNotify(boolean toggled)
    {
        this.toggled = toggled;
    }

    public void setValue(boolean toggled)
    {
        this.toggled = toggled;

        if(onStateChanged != null)
            onStateChanged.accept(toggled);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        guiGraphics.blitSprite(
                toggled ? toggledSprite : normalSprite,
                getX(), getY(),
                width, height
        );

        if(draggingActive && isHoveredOrFocused() && toggled != draggingState)
        {
            setValue(draggingState);
        }
    }

    public static class Builder
    {
        private int x, y, width, height;
        private boolean initialState;
        private Consumer<Boolean> onStateChanged;
        private ResourceLocation normalSprite;
        private ResourceLocation toggledSprite;

        public Builder(){}

        public Builder withBounds(int x, int y, int width, int height)
        {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder withInitialValue(boolean toggled)
        {
            this.initialState = toggled;
            return this;
        }

        public Builder withNormalSprite(ResourceLocation normalSprite)
        {
            this.normalSprite = normalSprite;
            return this;
        }

        public Builder withToggledSprite(ResourceLocation toggledSprite)
        {
            this.toggledSprite = toggledSprite;
            return this;
        }

        public Builder withSprites(ResourceLocation normalSprite, ResourceLocation toggledSprite)
        {
            this.normalSprite = normalSprite;
            this.toggledSprite = toggledSprite;
            return this;
        }

        public Builder withCallback(Consumer<Boolean> onStateChanged)
        {
            this.onStateChanged = onStateChanged;
            return this;
        }

        public SpriteToggle build()
        {
            return new SpriteToggle(
                    x, y, width, height,
                    onStateChanged,
                    initialState,
                    normalSprite, toggledSprite
            );
        }
    }
}
