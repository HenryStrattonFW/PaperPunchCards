package com.fatwednesday.paperpunchcards.gui;

import com.fatwednesday.fatlib.gui.components.GuiTexture;
import com.fatwednesday.fatlib.gui.components.SpriteButton;
import com.fatwednesday.fatlib.gui.components.SpriteToggle;
import com.fatwednesday.paperpunchcards.PaperPunchCards;
import com.fatwednesday.paperpunchcards.items.PaperPunchable;
import com.fatwednesday.paperpunchcards.payloads.CardPunchMenuCreatePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;

public class CardPuncherScreen extends AbstractContainerScreen<CardPuncherMenu>
{
    private static final GuiTexture BG_TEXTURE = new GuiTexture(
            PaperPunchCards.getResource("textures/gui/card_puncher.png"),
            288, 118
    );

    private static final int SPACING = 13;

    private static final GuiTexture INV_TEXTURE = new GuiTexture(
            PaperPunchCards.getResource("textures/gui/default_inventory.png"),
            176, 100
    );

    private static final GuiTexture CARD_TEXTURE = new GuiTexture(
            PaperPunchCards.getResource("textures/gui/punch_card.png"),
            240, 86
    );
    private static final GuiTexture TAPE_TEXTURE = new GuiTexture(
            PaperPunchCards.getResource("textures/gui/paper_tape.png"),
            240, 71
    );

    private final ArrayList<SpriteToggle> toggleGrid = new ArrayList<>();
    private SpriteButton confirmButton;
    private PaperPunchable currentInput;

    public CardPuncherScreen(CardPuncherMenu menu, Inventory playerInventory, Component title)
    {
        super(menu, playerInventory, title);
        imageWidth = BG_TEXTURE.width();
        imageHeight = BG_TEXTURE.height() + SPACING + INV_TEXTURE.height();
        currentInput = null;
    }

    @Override
    protected void init()
    {
        super.init();

        menu.setChangeListener(this::onMenuInputChanged);

        var toggleBuilder = new SpriteToggle.Builder()
                .withToggledSprite(PaperPunchCards.getResource("toggle_paper_on"))
                .withNormalSprite(PaperPunchCards.getResource("toggle_paper_off"));

        var offsetLeft = leftPos + 49;
        var offsetTop = topPos + 34;

        var sequenceData = menu.sequenceData();
        for (var x = 0; x < 20; x++)
        {
            for (var y = 0; y < 4; y++)
            {
                int finalX = x;
                int finalY = y;

                var nibble = sequenceData.getNibble(x);
                var toggle = toggleBuilder
                        .withInitialValue((nibble & (1 << y)) != 0)
                        .withBounds(offsetLeft + (x * 10), offsetTop + (y * 10), 10, 10)
                        .withCallback((val) -> onToggleChanged(finalX, finalY, val))
                        .build();
                toggle.visible = false; // default to invisible.
                addRenderableWidget(toggle);
                toggleGrid.add(toggle);
            }
        }

        confirmButton = new SpriteButton.Builder()
                .withLabel(Component.translatable("menu.button.paperpunchcards.confirm"))
                .withBounds(leftPos + 112, topPos + 96, 64, 16)
                .withNormalSprite(PaperPunchCards.getResource("button_normal"))
                .withFocusedSprite(PaperPunchCards.getResource("button_hovered"))
                .withDisabledSprite(PaperPunchCards.getResource("button_disabled"))
                .withPressedSprite(PaperPunchCards.getResource("button_pressed"))
                .withCallback((b) -> onConfirmPressed())
                .build();
        addRenderableWidget(confirmButton);
        refreshConfirmButtonState();
    }

    private void onToggleChanged(int x, int y, boolean value)
    {
        var sequenceData = menu.sequenceData();
        var nibbleValue = sequenceData.getNibble(x);
        if (value)
        {
            nibbleValue |= (1 << y);
        }
        else
        {
            nibbleValue &= ~(1 << y);
        }
        sequenceData.setNibble(x, nibbleValue);

        refreshConfirmButtonState();
    }

    private void configureForCard()
    {
        configureTogglePositions(leftPos + 49, topPos + 34, 10, 10);
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
                var idx = y + (x * 4);
                var toggle = toggleGrid.get(idx);
                toggle.setPosition(left + (x * xSpacing), top + (y * ySpacing));
            }
        }
    }

    private void setToggleGridVisible(boolean visible)
    {
        for (var toggle : toggleGrid)
        {
            toggle.visible = visible;
        }
    }

    private void onConfirmPressed()
    {
        setFocused(null);
        PacketDistributor.sendToServer(
                new CardPunchMenuCreatePayload(
                        menu.containerId,
                        menu.sequenceData().bytes()
                )
        );
    }

    private void onMenuInputChanged(PaperPunchable input)
    {
        if (input == null)
        {
            currentInput = null;
            setToggleGridVisible(false);
            return;
        }
        if (input.showAsCards())
        {
            configureForCard();
        }
        else
        {
            configureForTape();
        }
        setToggleGridVisible(true);
        currentInput = input;
        refreshConfirmButtonState();
    }

    private void refreshConfirmButtonState()
    {
        confirmButton.active = menu.canTriggerConfirm();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1)
    {
        BG_TEXTURE.blit(guiGraphics, leftPos, topPos);
        INV_TEXTURE.blit(guiGraphics, leftPos + 56, topPos + 124);
        if (currentInput == null)
        {
            return;
        }
        if (currentInput.showAsCards())
        {
            CARD_TEXTURE.blit(guiGraphics, leftPos + 24, topPos + 4);
        }
        else
        {
            TAPE_TEXTURE.blit(guiGraphics, leftPos + 24, topPos + 15);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY)
    {
        graphics.drawString(font, playerInventoryTitle, 64, imageHeight - 102, 0x404040, false);
    }
}
