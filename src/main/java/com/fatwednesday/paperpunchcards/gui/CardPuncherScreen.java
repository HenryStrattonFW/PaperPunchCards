package com.fatwednesday.paperpunchcards.gui;

import com.fatwednesday.fatlib.gui.components.GuiTexture;
import com.fatwednesday.fatlib.gui.components.SpriteButton;
import com.fatwednesday.fatlib.gui.components.SpriteToggle;
import com.fatwednesday.fatlib.gui.utils.GuiUtils;
import com.fatwednesday.fatlib.payloads.FatLibMenuRenamePayload;
import com.fatwednesday.paperpunchcards.PaperPunchCards;
import com.fatwednesday.paperpunchcards.payloads.CardPunchMenuDataPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;

public class CardPuncherScreen extends AbstractContainerScreen<CardPuncherMenu>
{
    private static final int SPACING = 13;

    private static final GuiTexture BG_TEXTURE = new GuiTexture(
            PaperPunchCards.getResource("textures/gui/card_puncher.png"),
            288, 118
    );

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
            240, 81
    );

    private final ArrayList<SpriteToggle> toggleGrid = new ArrayList<>();
    private SpriteButton prevPageButton;
    private SpriteButton nextPageButton;
    private EditBox nameInput;
    private int currentPage;
    private boolean needToSyncDataChange;

    public CardPuncherScreen(CardPuncherMenu menu, Inventory playerInventory, Component title)
    {
        super(menu, playerInventory, title);
        imageWidth = BG_TEXTURE.width();
        imageHeight = BG_TEXTURE.height() + SPACING + INV_TEXTURE.height();
        inventoryLabelX = 64;
        inventoryLabelY = imageHeight - 102;
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
        var offsetTop = topPos + 42;
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

        prevPageButton = addRenderableWidget(
                new SpriteButton.Builder()
                        .withLabel(Component.empty())
                        .withNormalSprite(PaperPunchCards.getResource("paper_arrow_left_normal"))
                        .withPressedSprite(PaperPunchCards.getResource("paper_arrow_left_pressed"))
                        .withBounds(centerX() - 64,topPos + 75,12,10)
                        .withCallback((b)->changePage(-1))
                        .build()
        );

        nextPageButton = addRenderableWidget(
                new SpriteButton.Builder()
                        .withLabel(Component.empty())
                        .withNormalSprite(PaperPunchCards.getResource("paper_arrow_right_normal"))
                        .withPressedSprite(PaperPunchCards.getResource("paper_arrow_right_pressed"))
                        .withBounds(centerX() + 64,topPos + 75,12,10)
                        .withCallback((b)->changePage(1))
                        .build()
        );

        nameInput = addRenderableWidget(
                new EditBox(
                        this.font,
                        leftPos + 56, topPos + 9,
                        103, 12,
                        Component.literal("Name")
                )
        );
        nameInput.setCanLoseFocus(true);
        nameInput.setTextColor(0x746558);
        nameInput.setTextShadow(false);
        nameInput.setTextColorUneditable(0x746558);
        nameInput.setBordered(false);
        nameInput.setMaxLength(25);
        nameInput.setResponder(this::onNameChanged);
        nameInput.setValue("");

        updatePageButtonVisibility();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (keyCode == InputConstants.KEY_ESCAPE)
        {
            minecraft.player.closeContainer();
        }

        return nameInput.keyPressed(keyCode, scanCode, modifiers) ||
                nameInput.canConsumeInput() ||
                super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void onNameChanged(String name)
    {
        if(menu.trySetItemName(name))
        {
            PacketDistributor.sendToServer(
                    new FatLibMenuRenamePayload(
                            menu.containerId,
                            menu.getItemName()
                    )
            );
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        if(needToSyncDataChange)
        {
            needToSyncDataChange = false;
            PacketDistributor.sendToServer(
                    new CardPunchMenuDataPayload(
                            menu.containerId,
                            menu.sequenceData().bytes()
                    )
            );
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void onToggleChanged(int x, int y, boolean value)
    {
        needToSyncDataChange = true;
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
        menu.trySetSequenceData(sequenceData.bytes());
        updatePageButtonVisibility();
    }

    private void configureForCard()
    {
        configureToggles(leftPos + 49, topPos + 38, 10, 10);
        nameInput.setPosition(leftPos + 56, topPos + 9);
    }

    private void configureForTape()
    {
        configureToggles(leftPos + 44, topPos + 30, 10, 10);
        nameInput.setPosition(leftPos + 56, topPos + 15);
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

    private void onMenuInputChanged()
    {
        var data = menu.getInputAsPunchable();
        var stack = data.stack();
        var punchable = data.punchable();
        currentPage = 0;
        if (punchable != null)
        {
            if (punchable.showAsCards())
            {
                configureForCard();
            }
            else
            {
                configureForTape();
            }
            matchToggleStatesToData();
            if(stack.has(DataComponents.CUSTOM_NAME))
            {
                nameInput.setValue(stack.get(DataComponents.CUSTOM_NAME).getString());
            }
            else
            {
                nameInput.setValue(stack.getHoverName().getString());
            }
            nameInput.setEditable(true);
        }
        else
        {
            nameInput.setValue("");
            nameInput.setEditable(false);
        }
        setToggleGridVisible(punchable != null);
        updatePageButtonVisibility();
        nameInput.setFocused(false);
        setFocused(null);
    }

    private void updatePageButtonVisibility()
    {
        var punchable = menu.getInputAsPunchable().punchable();
        if(punchable == null || punchable.showAsCards())
        {
            prevPageButton.visible = false;
            nextPageButton.visible = false;
        }
        else
        {
            prevPageButton.visible = currentPage > 0;
            nextPageButton.visible = currentPage < punchable.pageCount()-1;
        }
    }

    private void changePage(int delta)
    {
        var punchable = menu.getInputAsPunchable().punchable();
        if(punchable == null || punchable.showAsCards())
            return;
        currentPage = Math.clamp(currentPage + delta, 0, punchable.pageCount()-1);
        updatePageButtonVisibility();
        matchToggleStatesToData();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1)
    {
        BG_TEXTURE.blit(guiGraphics, leftPos, topPos);
        INV_TEXTURE.blit(guiGraphics, leftPos + 56, topPos + 124);

        var punchable = menu.getInputAsPunchable().punchable();
        if (punchable == null)
        {
            return;
        }
        if (punchable.showAsCards())
        {
            CARD_TEXTURE.blit(guiGraphics, leftPos + 24, topPos + 4);
        }
        else
        {
            TAPE_TEXTURE.blit(guiGraphics, leftPos + 24, topPos + 9);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY)
    {
        super.renderLabels(graphics, mouseX, mouseY);

        var punchable = menu.getInputAsPunchable().punchable();
        if (punchable != null && !punchable.showAsCards())
        {
            GuiUtils.drawCenteredLabel(
                    graphics,
                    font,
                    PaperPunchCards.getTranslationFormatted(
                            "menu.tape_page",
                            currentPage + 1,
                            punchable.pageCount()
                    ),
                    halfWidth(),
                    76,
                    0x746558,
                    false
            );
        }
    }
}