package com.fatwednesday.paperpunchcards.gui;

import com.fatwednesday.fatlib.gui.components.GuiTexture;
import com.fatwednesday.fatlib.gui.components.SpriteButton;
import com.fatwednesday.fatlib.gui.components.SpriteToggle;
import com.fatwednesday.fatlib.gui.utils.Utils;
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
    private SpriteButton prevPageButton;
    private SpriteButton nextPageButton;
    private PaperPunchable currentInput;
    private int currentPage;

    public CardPuncherScreen(CardPuncherMenu menu, Inventory playerInventory, Component title)
    {
        super(menu, playerInventory, title);
        imageWidth = BG_TEXTURE.width();
        imageHeight = BG_TEXTURE.height() + SPACING + INV_TEXTURE.height();
        currentInput = null;
    }

    private int halfWidth()
    {
        return (imageWidth / 2);
    }
    private int centerX()
    {
        return leftPos + halfWidth();
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

        confirmButton = addRenderableWidget(
            new SpriteButton.Builder()
                .withLabel(PaperPunchCards.getTranslation("menu.button.confirm"))
                .withBounds(leftPos + 112, topPos + 96, 64, 16)
                .withNormalSprite(PaperPunchCards.getResource("button_normal"))
                .withFocusedSprite(PaperPunchCards.getResource("button_hovered"))
                .withDisabledSprite(PaperPunchCards.getResource("button_disabled"))
                .withPressedSprite(PaperPunchCards.getResource("button_pressed"))
                .withCallback((b) -> onConfirmPressed())
                .build()
        );

        prevPageButton = addRenderableWidget(
            new SpriteButton.Builder()
                .withLabel(Component.empty())
                .withNormalSprite(PaperPunchCards.getResource("paper_arrow_left_normal"))
                .withPressedSprite(PaperPunchCards.getResource("paper_arrow_left_pressed"))
                .withBounds(centerX() - 64,topPos + 18,12,10)
                .withCallback((b)->changePage(-1))
                .build()
        );

        nextPageButton = addRenderableWidget(
            new SpriteButton.Builder()
                .withLabel(Component.empty())
                .withNormalSprite(PaperPunchCards.getResource("paper_arrow_right_normal"))
                .withPressedSprite(PaperPunchCards.getResource("paper_arrow_right_pressed"))
                .withBounds(centerX() + 64,topPos + 18,12,10)
                .withCallback((b)->changePage(1))
                .build()
        );

        refreshConfirmButtonState();
    }

    private void onToggleChanged(int x, int y, boolean value)
    {
        var sequenceData = menu.sequenceData();
        var nibbleIndex = (currentPage * 20) + x;
        var nibbleValue = sequenceData.getNibble(nibbleIndex);
        if (value)
        {
            nibbleValue |= (1 << y);
        }
        else
        {
            nibbleValue &= ~(1 << y);
        }
        sequenceData.setNibble(nibbleIndex, nibbleValue);

        refreshConfirmButtonState();
    }

    private void configureForCard()
    {
        configureToggles(leftPos + 49, topPos + 34, 10, 10);
    }

    private void configureForTape()
    {
        configureToggles(leftPos + 44, topPos + 34, 10, 10);
    }

    private void configureToggles(int left, int top, int xSpacing, int ySpacing)
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

    private void matchToggleStatesToData()
    {
        var data = menu.sequenceData();
        var pageOffset = currentPage * 20;
        for (var x = 0; x < 20; x++)
        {
            for (var y = 0; y < 4; y++)
            {
                var nibble = data.getNibble(pageOffset + x);
                var idx = y + (x * 4);
                var toggle = toggleGrid.get(idx);
                toggle.setValueWithoutNotify((nibble & (1 << y)) != 0);
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
        currentInput = input;
        currentPage = 0;
        if (input != null)
        {
            if (input.showAsCards())
            {
                configureForCard();
            }
            else
            {
                configureForTape();
            }
            matchToggleStatesToData();
        }
        setToggleGridVisible(input != null);
        refreshConfirmButtonState();
    }

    private void refreshConfirmButtonState()
    {
        confirmButton.active = menu.canTriggerConfirm();
        if(currentInput == null || currentInput.showAsCards())
        {
            prevPageButton.visible = false;
            nextPageButton.visible = false;
        }
        else
        {
            prevPageButton.visible = currentPage > 0;
            nextPageButton.visible = currentPage < currentInput.pageCount()-1;
        }
    }

    private void changePage(int delta)
    {
        currentPage = Math.clamp(currentPage + delta, 0, currentInput.pageCount()-1);
        refreshConfirmButtonState();
        matchToggleStatesToData();
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
        if (currentInput != null && !currentInput.showAsCards())
        {
            Utils.drawCenteredLabel(
                    graphics,
                    font,
                    PaperPunchCards.getTranslationFormatted(
                            "menu.tape_page",
                            currentPage + 1,
                            currentInput.pageCount()
                    ),
                    halfWidth(),
                    20,
                    0x746558,
                    false
            );
        }
    }
}
