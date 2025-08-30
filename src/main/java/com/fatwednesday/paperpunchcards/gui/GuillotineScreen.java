package com.fatwednesday.paperpunchcards.gui;

import com.fatwednesday.fatlib.gui.components.GuiTexture;
import com.fatwednesday.paperpunchcards.PaperPunchCards;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

/*
* Based largely on the StoneCutter menu + screen, so a lot of code copied
* from that and reformatted for my own readability / convenience.
*/
public class GuillotineScreen extends AbstractContainerScreen<GuillotineMenu>
{
    private static final Component menuTitle = PaperPunchCards.getTranslation("menu.guillotine_title");
    private static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.withDefaultNamespace("container/stonecutter/scroller");
    private static final ResourceLocation SCROLLER_DISABLED_SPRITE = ResourceLocation.withDefaultNamespace("container/stonecutter/scroller_disabled");
    private static final ResourceLocation RECIPE_SELECTED_SPRITE = ResourceLocation.withDefaultNamespace("container/stonecutter/recipe_selected");
    private static final ResourceLocation RECIPE_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("container/stonecutter/recipe_highlighted");
    private static final ResourceLocation RECIPE_SPRITE = ResourceLocation.withDefaultNamespace("container/stonecutter/recipe");

    private static final int RECIPES_X = 52;
    private static final int RECIPES_Y = 14;
    private static final int RECIPES_COLUMNS = 4;
    private static final int RECIPES_IMAGE_SIZE_WIDTH = 16;
    private static final int RECIPES_IMAGE_SIZE_HEIGHT = 18;
    private static final int RECIPE_DISPLAY_MAX = 12;
    private static final GuiTexture BG_TEXTURE = new GuiTexture(
            PaperPunchCards.getResource("textures/gui/guillotine.png"),
            176, 166
    );

    private int startIndex;
    private float scrollOffset;
    private boolean scrolling;

    public GuillotineScreen(GuillotineMenu menu, Inventory playerInventory, Component title)
    {
        super(menu, playerInventory, title);
        imageWidth = BG_TEXTURE.width();
        imageHeight = BG_TEXTURE.height();
        inventoryLabelX = 8;
        inventoryLabelY = imageHeight - 94;
        startIndex = 0;
    }

    @Override
    protected void init()
    {
        super.init();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY)
    {
        BG_TEXTURE.blit(guiGraphics, leftPos, topPos);

        ResourceLocation resourcelocation = isScrollBarActive() ? SCROLLER_SPRITE : SCROLLER_DISABLED_SPRITE;
        guiGraphics.blitSprite(
                resourcelocation,
                leftPos + 119,
                topPos + 15 + (int)(41.0F * scrollOffset),
                12, 15
        );

        var recipeLeftPos = leftPos + RECIPES_X;
        var recipeTopPos = topPos + RECIPES_Y;
        renderButtons(guiGraphics, mouseX, mouseY, recipeLeftPos, recipeTopPos);
        renderRecipes(guiGraphics, recipeLeftPos, recipeTopPos);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY)
    {
        super.renderLabels(graphics, mouseX, mouseY);
        graphics.drawString(font, menuTitle, inventoryLabelX, 4, 0x404040, false);
    }

    private void renderButtons(GuiGraphics guiGraphics, int mouseX, int mouseY, int x, int y)
    {
        var endIndex = startIndex + RECIPE_DISPLAY_MAX;
        for(var i = startIndex; i < endIndex && i < menu.getRecipeCount(); ++i)
        {
            var j = i - startIndex;
            var k = x + j % RECIPES_COLUMNS * RECIPES_IMAGE_SIZE_WIDTH;
            var l = j / RECIPES_COLUMNS;
            var i1 = y + l * RECIPES_IMAGE_SIZE_HEIGHT + 2;
            ResourceLocation resourcelocation;
            if (i == menu.getSelectedRecipeIndex())
            {
                resourcelocation = RECIPE_SELECTED_SPRITE;
            }
            else if (mouseX >= k && mouseY >= i1 && mouseX < k + RECIPES_IMAGE_SIZE_WIDTH && mouseY < i1 + RECIPES_IMAGE_SIZE_HEIGHT)
            {
                resourcelocation = RECIPE_HIGHLIGHTED_SPRITE;
            }
            else
            {
                resourcelocation = RECIPE_SPRITE;
            }

            if(menu.isCraftable(i))
            {
                guiGraphics.blitSprite(resourcelocation, k, i1 - 1, RECIPES_IMAGE_SIZE_WIDTH, RECIPES_IMAGE_SIZE_HEIGHT);
            }
        }
    }

    private void renderRecipes(GuiGraphics guiGraphics, int x, int y)
    {
        if(minecraft == null || minecraft.level == null)
            return;

        var recipes = menu.getRecipes();
        var endIndex = startIndex + RECIPE_DISPLAY_MAX;
        for(var i = startIndex; i < endIndex && i < menu.getRecipeCount(); ++i)
        {
            var j = i - startIndex;
            var k = x + j % RECIPES_COLUMNS * RECIPES_IMAGE_SIZE_WIDTH;
            var l = j / RECIPES_COLUMNS;
            var i1 = y + l * RECIPES_IMAGE_SIZE_HEIGHT + 2;
            var recipe = recipes.get(i).value();
            guiGraphics.setColor(1,1,1,menu.isCraftable(i) ? 1 : 0.5f);
            guiGraphics.renderItem(recipe.getResultItem(minecraft.level.registryAccess()), k, i1);
        }
        guiGraphics.setColor(1,1,1,1);
    }

    private boolean isScrollBarActive()
    {
        return menu.getRecipeCount() > 12;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        scrolling = false;
        var xPos = leftPos + 52;
        var yPos = topPos + 14;
        var endIndex = startIndex + 12;

        for(var i = startIndex; i < endIndex; ++i)
        {
            var idx = i - startIndex;
            var d0 = mouseX - (double)(xPos + idx % 4 * 16);
            var d1 = mouseY - (double)(yPos + idx / 4 * 18);
            if (d0 >= 0.0 && d1 >= 0.0 &&
                d0 < 16.0 && d1 < 18.0 &&
                menu.clickMenuButton(minecraft.player, i))
            {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, i);
                return true;
            }
        }

        xPos = leftPos + 119;
        yPos = topPos + 9;
        if (mouseX >= (double)xPos && mouseX < (double)(xPos + 12) &&
            mouseY >= (double)yPos && mouseY < (double)(yPos + 54))
        {
            scrolling = true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY)
    {
        if (scrolling && isScrollBarActive())
        {
            var i = topPos + 14;
            var j = i + 54;
            scrollOffset = ((float)mouseY - (float)i - 7.5F) / ((float)(j - i) - 15.0F);
            scrollOffset = Mth.clamp(scrollOffset, 0.0F, 1.0F);
            startIndex = (int)((double)(scrollOffset * (float)getOffscreenRows()) + 0.5) * 4;
            return true;
        }
        else
        {
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY)
    {
        if (isScrollBarActive())
        {
            var i = getOffscreenRows();
            var f = (float)scrollY / (float)i;
            scrollOffset = Mth.clamp(scrollOffset - f, 0.0F, 1.0F);
            startIndex = (int)((double)(scrollOffset * (float)i) + 0.5) * 4;
        }

        return true;
    }

    protected int getOffscreenRows()
    {
        return (menu.getRecipeCount() + 4 - 1) / 4 - 3;
    }

}
