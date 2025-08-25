package com.fatwednesday.paperpunchcards.gui;

import com.fatwednesday.fatlib.gui.components.GuiTexture;
import com.fatwednesday.paperpunchcards.PaperPunchCards;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.gui.widget.ScrollPanel;

public class GuillotineScreen extends AbstractContainerScreen<GuillotineMenu>
{
    private static final Component menuTitle = PaperPunchCards.getTranslation("menu.guillotine_title");
    private ScrollPanel scrollPanel;
    private static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.withDefaultNamespace("container/stonecutter/scroller");
    private static final ResourceLocation SCROLLER_DISABLED_SPRITE = ResourceLocation.withDefaultNamespace("container/stonecutter/scroller_disabled");
    private static final ResourceLocation RECIPE_SELECTED_SPRITE = ResourceLocation.withDefaultNamespace("container/stonecutter/recipe_selected");
    private static final ResourceLocation RECIPE_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("container/stonecutter/recipe_highlighted");
    private static final ResourceLocation RECIPE_SPRITE = ResourceLocation.withDefaultNamespace("container/stonecutter/recipe");

    private static final int RECIPES_X = 52;
    private static final int RECIPES_Y = 14;
    private static final int RECIPES_COLUMNS = 4;
    private static final int RECIPES_ROWS = 3;
    private static final int RECIPES_IMAGE_SIZE_WIDTH = 16;
    private static final int RECIPES_IMAGE_SIZE_HEIGHT = 18;

    private static final GuiTexture BG_TEXTURE = new GuiTexture(
            PaperPunchCards.getResource("textures/gui/guillotine.png"),
            176, 166
    );

    public GuillotineScreen(GuillotineMenu menu, Inventory playerInventory, Component title)
    {
        super(menu, playerInventory, title);
        imageWidth = BG_TEXTURE.width();
        imageHeight = BG_TEXTURE.height();
        inventoryLabelX = 8;
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    protected void init()
    {
        super.init();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1)
    {
        BG_TEXTURE.blit(guiGraphics, leftPos, topPos);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY)
    {
        super.renderLabels(graphics, mouseX, mouseY);
        graphics.drawString(font, menuTitle, inventoryLabelX, 4, 0x404040, false);
    }
}
