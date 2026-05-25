
package vurst.visual.client.screens.menu.settings.impl;

import java.util.Locale;
import lombok.Generated;
import vurst.visual.base.font.Font;
import vurst.visual.base.font.Fonts;
import vurst.visual.base.theme.Theme;
import vurst.visual.client.modules.api.setting.impl.NumberSetting;
import vurst.visual.client.screens.menu.settings.api.MenuSetting;
import vurst.visual.utility.game.other.MouseButton;
import vurst.visual.utility.render.display.base.BorderRadius;
import vurst.visual.utility.render.display.base.Rect;
import vurst.visual.utility.render.display.base.UIContext;
import vurst.visual.utility.render.display.base.color.ColorRGBA;

public class MenuSliderSetting
extends MenuSetting {
    private final NumberSetting setting;
    private boolean dragging = false;
    private Rect rect;

    public MenuSliderSetting(NumberSetting setting) {
        this.setting = setting;
    }

    @Override
    public void render(UIContext ctx, float mouseX, float mouseY, float x, float settingY, float moduleWidth, float alpha, float animEnable, ColorRGBA themeColor, ColorRGBA textColor, ColorRGBA descriptionColor, Theme theme) {
        float padding = 8.0f;
        Font settingFont = Fonts.MEDIUM.getFont(7.0f);
        Font descFont = Fonts.REGULAR.getFont(7.0f);
        Font iconFont = Fonts.ICONS.getFont(7.0f);
        ctx.drawText(iconFont, "D", x + padding, settingY, themeColor);
        float nameX = x + padding + 10.0f;
        String description = this.setting.getDisplayDescription();
        boolean hasDescription = !description.isEmpty();
        float animatedValue = this.setting.getCurrent();
        float sliderWidth = description.isEmpty() ? moduleWidth - 20.0f : moduleWidth / 2.8f;
        float sliderX = x + moduleWidth - padding - 4.0f - sliderWidth;
        float sliderY = hasDescription ? settingY + 17.0f : settingY + 12.0f;
        String valueText = String.format(Locale.US, "%.1f", Float.valueOf(animatedValue));
        float valueTextWidth = settingFont.width(valueText);
        float valueTextX = x + moduleWidth - padding - valueTextWidth;
        float labelMaxWidth = Math.max(12.0f, valueTextX - nameX - 6.0f);
        String settingName = MenuSliderSetting.fitText(this.setting.getDisplayName(), settingFont, labelMaxWidth);
        ctx.drawText(settingFont, settingName, nameX, settingY, textColor);
        ctx.drawText(settingFont, valueText, valueTextX, settingY, themeColor);
        ctx.drawRoundedRect(sliderX, sliderY, sliderWidth, 2.0f, BorderRadius.all(0.2f), theme.getForegroundLight().mulAlpha(alpha));
        float percent = (animatedValue - this.setting.getMin()) / (this.setting.getMax() - this.setting.getMin());
        float filledWidth = sliderWidth * percent;
        ctx.drawRoundedRect(sliderX, sliderY, Math.max(0.0f, filledWidth - 2.0f), 2.0f, BorderRadius.ZERO, theme.getGray().mix(theme.getColor(), animEnable).mulAlpha(alpha));
        float handleX = sliderX + filledWidth;
        float handleY = sliderY - 1.0f;
        ColorRGBA circleColor = theme.getGrayLight().mix(theme.getWhite(), animEnable).mulAlpha(alpha);
        ctx.drawRoundedRect(handleX, handleY, 4.0f, 4.0f, BorderRadius.all(2.0f), circleColor);
        this.rect = new Rect(sliderX, sliderY - 2.0f, sliderWidth, 6.0f);
        if (hasDescription) {
            float descY = settingY + 9.0f;
            ctx.drawText(descFont, description, x + padding, descY, descriptionColor);
        }
        this.updateSlider(mouseX);
    }

    public void updateSlider(double mouseX) {
        if (!this.dragging) {
            return;
        }
        Rect sliderRect = this.rect;
        if (sliderRect == null) {
            return;
        }
        double relativeX = mouseX - (double)sliderRect.x();
        double percent = Math.max(0.0, Math.min(1.0, relativeX / (double)sliderRect.width()));
        double min = this.setting.getMin();
        double max = this.setting.getMax();
        float increment = this.setting.getIncrement();
        double newValue = min + (max - min) * percent;
        newValue = (double)((float)Math.round((newValue - min) / (double)increment) * increment) + min;
        newValue = Math.max(min, Math.min(max, newValue));
        this.setting.setCurrent((float)newValue);
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        if (button == MouseButton.LEFT && this.rect != null && this.rect.contains(mouseX, mouseY)) {
            this.dragging = true;
            return true;
        }
        return false;
    }

    @Override
    public void onMouseReleased(double mouseX, double mouseY, MouseButton button) {
        if (button == MouseButton.LEFT) {
            this.dragging = false;
        }
    }

    @Override
    public float getWidth() {
        return 0.0f;
    }

    @Override
    public float getHeight() {
        return this.setting.getDisplayDescription().isEmpty() ? 14.0f : 20.0f;
    }

    @Override
    public boolean isVisible() {
        return this.setting.getVisible().get();
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
    public NumberSetting getSetting() {
        return this.setting;
    }
}

