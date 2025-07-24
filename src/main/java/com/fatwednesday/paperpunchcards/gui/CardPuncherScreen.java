package com.fatwednesday.paperpunchcards.gui;

import com.fatwednesday.fatlib.gui.components.SpriteButton;
import com.fatwednesday.fatlib.gui.components.SpriteToggle;
import com.fatwednesday.paperpunchcards.PaperPunchCards;
import com.fatwednesday.paperpunchcards.utils.NibbleStore;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;

public class CardPuncherScreen extends AbstractContainerScreen<CardPuncherMenu>
{
    private static final ResourceLocation BACKGROUND = PaperPunchCards.getResource("textures/gui/card_punch_screen.png");

    private final ArrayList<SpriteToggle> toggleGrid = new ArrayList<>();
    private NibbleStore sequenceData;
    private SpriteButton confirmButton;

    public CardPuncherScreen(CardPuncherMenu menu, Inventory playerInventory, Component title)
    {
        super(menu, playerInventory, title);
        imageWidth = 288;
        imageHeight = 223;
    }

    @Override
    protected void init()
    {
        super.init();

        var toggleBuilder = new SpriteToggle.Builder()
                .withToggledSprite(PaperPunchCards.getResource("toggle_paper_on"))
                .withNormalSprite(PaperPunchCards.getResource("toggle_paper_off"));

        var offsetLeft = leftPos + 49;
        var offsetTop = topPos + 34;

        sequenceData = new NibbleStore(20);
        for(var x=0;x<20;x++)
        {
            for(var y=0;y<4;y++)
            {
                var finalY = y;
                var finalX = x;

                var nibble = sequenceData.getNibble(x);
                var toggle = toggleBuilder
                        .withInitialValue((nibble & (1 << y)) != 0)
                        .withBounds(offsetLeft +(x * 10),offsetTop+(y * 10),10,10)
                        .withCallback((val)-> onToggleChanged(finalX, finalY, val))
                        .build();
                toggle.visible = false; // default to invisible.
                this.addRenderableWidget(toggle);
                toggleGrid.add(toggle);
            }
        }

        confirmButton = new SpriteButton.Builder()
                .withLabel(Component.translatable("menu.button.paperpunchcards.confirm"))
                .withBounds(leftPos+112, topPos+96, 64, 16)
                .withNormalSprite(PaperPunchCards.getResource("button_normal"))
                .withFocusedSprite(PaperPunchCards.getResource("button_hovered"))
                .withDisabledSprite(PaperPunchCards.getResource("button_disabled"))
                .withPressedSprite(PaperPunchCards.getResource("button_pressed"))
                .withCallback((b)->onConfirmPressed())
                .build();
        this.addRenderableWidget(confirmButton);
    }

    private void onToggleChanged(int x, int y, boolean value)
    {
        var nibbleValue = sequenceData.getNibble(x);
        if(value)
        {
            nibbleValue |= (1 << y);
        }
        else
        {
            nibbleValue &= ~(1 << y);
        }
        sequenceData.setNibble(x, nibbleValue);
    }

    private void configureForCard()
    {
        configureTogglePositions(leftPos + 49, topPos + 34, 10,10);
    }

    private void configureForTape()
    {
        configureTogglePositions(leftPos + 44, topPos + 34, 10, 10);
    }

    private void configureTogglePositions(int left, int top, int xSpacing, int ySpacing)
    {
        for (var x = 0; x < 20; x++)
        {
            for (var y = 0; y < 4; y++)
            {
                var toggle = toggleGrid.get((y * 20) + x);
                toggle.setPosition(left +(x * xSpacing),top+(y * ySpacing));
            }
        }
    }

    private void onConfirmPressed()
    {
        this.setFocused(null);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1)
    {
        guiGraphics.blit(
                BACKGROUND,
                leftPos, topPos,
                0,0,
                imageWidth,imageHeight,
                imageWidth,256
        );
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY)
    {
        graphics.drawString(font, playerInventoryTitle, 64, imageHeight - 94, 0x404040, false);
    }
}
