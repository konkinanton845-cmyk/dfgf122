
package vurst.visual.client.screens.menu.settings.impl;

import lombok.Generated;
import net.minecraft.ItemStack;
import vurst.visual.VurstVisual;
import vurst.visual.base.font.Font;
import vurst.visual.base.font.Fonts;
import vurst.visual.base.theme.Theme;
import vurst.visual.client.modules.api.setting.ItemIconProvider;
import vurst.visual.client.modules.api.setting.impl.ColorSetting;
import vurst.visual.client.screens.menu.settings.api.MenuSetting;
import vurst.visual.client.screens.menu.settings.impl.popup.MenuColorPopupSetting;
import vurst.visual.utility.game.other.MouseButton;
import vurst.visual.utility.render.display.base.BorderRadius;
import vurst.visual.utility.render.display.base.ChangeRect;
import vurst.visual.utility.render.display.base.Rect;
import vurst.visual.utility.render.display.base.UIContext;
import vurst.visual.utility.render.display.base.color.ColorRGBA;

public class MenuColorSetting
extends MenuSetting {
    private static final float ITEM_ICON_SCALE = 0.6f;
    private static final float ITEM_ICON_SIZE = 9.6f;
    private static final float ITEM_ICON_GAP = 3.0f;
    private static final float ROW_HEIGHT = 12.0f;
    private final ColorSetting setting;
    private Rect bounds;
    private final ChangeRect boundsColor;

    public MenuColorSetting(ColorSetting setting) {
        this.setting = setting;
        this.boundsColor = new ChangeRect(0.0f, 0.0f, 78.0f, 48.0f);
    }

    @Override
    public void render(UIContext ctx, float mouseX, float mouseY, float x, float settingY, float moduleWidth, float alpha, float animEnable, ColorRGBA themeColor, ColorRGBA textColor, ColorRGBA descriptionColor, Theme theme) {
        ItemIconProvider provider;
        float settingX = x + 8.0f;
        Font settingFont = Fonts.MEDIUM.getFont(7.0f);
        float textY = settingY + (12.0f - settingFont.height()) / 2.0f - 0.5f;
        float textX = x + 8.0f + 10.0f;
        ColorSetting colorSetting = this.setting;
        if (colorSetting instanceof ItemIconProvider && this.drawItemIcon(ctx, (provider = (ItemIconProvider)((Object)colorSetting)).getMenuIconStack(), settingX, settingY - 1.0f)) {
            textX = x + 8.0f + 9.6f + 3.0f;
        } else {
            float iconY = textY - 1.0f;
            ctx.drawText(Fonts.ICONS.getFont(6.0f), "V", settingX + 1.5f, iconY + 1.0f, themeColor);
        }
        float toggleSize = 8.0f;
        float toggleX = x + moduleWidth - toggleSize - 8.0f;
        float toggleY = settingY + (12.0f - toggleSize) / 2.0f;
        float labelMaxWidth = Math.max(12.0f, toggleX - textX - 6.0f);
        String settingName = MenuColorSetting.fitText(this.setting.getDisplayName(), settingFont, labelMaxWidth);
        ColorRGBA colorEnable = theme.getWhiteGray().mix(this.setting.getColor(), animEnable).mulAlpha(alpha);
        ctx.drawRoundedBorder(toggleX - 0.8f, toggleY - 0.8f, toggleSize + 1.6f, toggleSize + 1.6f, 0.1f, BorderRadius.all(3.0f), themeColor);
        ctx.drawRoundedRect(toggleX, toggleY, toggleSize, toggleSize, BorderRadius.all(3.0f), colorEnable);
        ctx.drawText(settingFont, settingName, textX, textY, textColor);
        this.bounds = new Rect(settingX, settingY, moduleWidth - 16.0f, 12.0f);
        this.boundsColor.setX(toggleX + 20.0f);
        this.boundsColor.setY(toggleY + toggleSize - this.boundsColor.getHeight() / 2.0f);
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        if (this.bounds != null && this.bounds.contains(mouseX, mouseY)) {
            VurstVisual.getInstance().getMenuScreen().addPopupMenuSetting(new MenuColorPopupSetting(this.boundsColor, this.setting));
            return true;
        }
        return false;
    }

    @Override
    public float getWidth() {
        return 0.0f;
    }

    @Override
    public float getHeight() {
        return 12.0f;
    }

    @Override
    public boolean isVisible() {
        return this.setting.getVisible().get();
    }

    private boolean drawItemIcon(UIContext ctx, ItemStack stack, float x, float y) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        ctx.pushMatrix();
        ctx.getMatrices().translate(x, y, 0.0f);
        ctx.getMatrices().scale(0.6f, 0.6f, 1.0f);
        ctx.drawItem(stack, 0, 0);
        ctx.popMatrix();
        return true;
    }

    private static String fitText(String input, Font font, float maxWidth) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        if (font.width(input) <= maxWidth) {
            return input;
        }
        String ellipsis = "...";
        String base = input;
        while (!base.isEmpty() && font.width(base + ellipsis) > maxWidth) {
            base = base.substring(0, base.length() - 1);
        }
        return base.isEmpty() ? ellipsis : base + ellipsis;
    }

    @Generated
    public ColorSetting getSetting() {
        return this.setting;
    }
}

