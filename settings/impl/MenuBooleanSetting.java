
package vurst.visual.client.screens.menu.settings.impl;

import lombok.Generated;
import net.minecraft.ItemStack;
import vurst.visual.base.animations.base.Animation;
import vurst.visual.base.animations.base.Easing;
import vurst.visual.base.font.Font;
import vurst.visual.base.font.Fonts;
import vurst.visual.base.theme.Theme;
import vurst.visual.client.modules.api.setting.ItemIconProvider;
import vurst.visual.client.modules.api.setting.impl.BooleanSetting;
import vurst.visual.client.screens.menu.settings.api.MenuSetting;
import vurst.visual.utility.game.other.MouseButton;
import vurst.visual.utility.render.display.base.BorderRadius;
import vurst.visual.utility.render.display.base.Rect;
import vurst.visual.utility.render.display.base.UIContext;
import vurst.visual.utility.render.display.base.color.ColorRGBA;

public class MenuBooleanSetting
extends MenuSetting {
    private static final float ITEM_ICON_SCALE = 0.6f;
    private static final float ITEM_ICON_SIZE = 9.6f;
    private static final float ITEM_ICON_GAP = 3.0f;
    private static final float ROW_HEIGHT = 12.0f;
    private final BooleanSetting setting;
    private final Animation animation = new Animation(300L, Easing.QUARTIC_OUT);
    private Rect bounds;

    public MenuBooleanSetting(BooleanSetting setting) {
        this.setting = setting;
    }

    @Override
    public void render(UIContext ctx, float mouseX, float mouseY, float x, float settingY, float moduleWidth, float alpha, float animEnable, ColorRGBA themeColor, ColorRGBA textColor, ColorRGBA descriptionColor, Theme theme) {
        ItemIconProvider provider;
        float settingX = x + 8.0f;
        Font settingFont = Fonts.MEDIUM.getFont(7.0f);
        Font descFont = Fonts.MEDIUM.getFont(6.0f);
        float textY = settingY + (12.0f - settingFont.height()) / 2.0f - 0.5f;
        float textX = x + 8.0f + 10.0f;
        float iconY = textY - 1.0f;
        Font iconFont = Fonts.ICONS.getFont(6.0f);
        BooleanSetting booleanSetting = this.setting;
        if (booleanSetting instanceof ItemIconProvider && this.drawItemIcon(ctx, (provider = (ItemIconProvider)((Object)booleanSetting)).getMenuIconStack(), settingX, settingY - 1.0f)) {
            textX = x + 8.0f + 9.6f + 3.0f;
        } else {
            float iconSize = 6.0f;
            ctx.drawRoundedRect(settingX, iconY, iconSize, iconSize, BorderRadius.all(1.0f), themeColor);
            ctx.drawText(Fonts.ICONS.getFont(5.5f), "S", settingX + 1.2f, iconY + 0.5f, theme.getForegroundDark().mulAlpha(alpha));
        }
        float toggleSize = 8.0f;
        float toggleX = x + moduleWidth - toggleSize - 8.0f;
        float toggleY = settingY + (12.0f - toggleSize) / 2.0f;
        float labelMaxWidth = Math.max(12.0f, toggleX - textX - 6.0f);
        String settingName = MenuBooleanSetting.fitText(this.setting.getDisplayName(), settingFont, labelMaxWidth);
        ctx.drawText(settingFont, settingName, textX, textY, textColor);
        this.animation.animateTo(this.setting.isEnabled() ? 1.0f : 0.0f);
        float progress = this.animation.update();
        ColorRGBA colorEnable = theme.getWhiteGray().mix(theme.getColor(), animEnable);
        ColorRGBA colorToggle = theme.getForegroundLight().mix(colorEnable, this.animation.getValue()).mulAlpha(alpha);
        ColorRGBA borderColor = theme.getForegroundLightStroke().mix(new ColorRGBA(0, 0, 0, 0), this.animation.getValue()).mulAlpha(alpha);
        ColorRGBA golochakaColor = theme.getGrayLight().mix(theme.getWhite(), animEnable);
        ColorRGBA golochakaFinalColor = new ColorRGBA(0, 0, 0, 0).mix(golochakaColor, this.animation.getValue()).mulAlpha(alpha);
        ctx.drawRoundedRect(toggleX, toggleY, toggleSize, toggleSize, BorderRadius.all(2.0f), colorToggle);
        ctx.drawRoundedBorder(toggleX, toggleY, toggleSize, toggleSize, -0.1f, BorderRadius.all(2.0f), borderColor);
        ctx.drawText(iconFont, "S", toggleX + 2.0f, toggleY + 1.0f, golochakaFinalColor);
        this.bounds = new Rect(settingX, settingY, moduleWidth - 16.0f, 12.0f);
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        if (this.bounds != null && this.bounds.contains(mouseX, mouseY) && button == MouseButton.LEFT) {
            this.setting.toggle();
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
    public BooleanSetting getSetting() {
        return this.setting;
    }
}

