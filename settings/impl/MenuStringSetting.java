
package vurst.visual.client.screens.menu.settings.impl;

import lombok.Generated;
import vurst.visual.base.font.Font;
import vurst.visual.base.font.Fonts;
import vurst.visual.base.theme.Theme;
import vurst.visual.client.modules.api.setting.impl.StringSetting;
import vurst.visual.client.screens.menu.settings.api.MenuSetting;
import vurst.visual.utility.game.other.MouseButton;
import vurst.visual.utility.interfaces.IMinecraft;
import vurst.visual.utility.render.display.base.BorderRadius;
import vurst.visual.utility.render.display.base.Rect;
import vurst.visual.utility.render.display.base.UIContext;
import vurst.visual.utility.render.display.base.color.ColorRGBA;

public class MenuStringSetting
extends MenuSetting
implements IMinecraft {
    private final StringSetting setting;
    private Rect inputBounds;
    private boolean editing;

    public MenuStringSetting(StringSetting setting) {
        this.setting = setting;
    }

    @Override
    public void render(UIContext ctx, float mouseX, float mouseY, float x, float settingY, float moduleWidth, float alpha, float animEnable, ColorRGBA themeColor, ColorRGBA textColor, ColorRGBA descriptionColor, Theme theme) {
        float cursorX;
        Font font = Fonts.MEDIUM.getFont(7.0f);
        float textY = settingY + (8.0f - font.height()) / 2.0f - 0.5f;
        ctx.drawText(Fonts.ICONS.getFont(5.5f), "N", x + 8.0f, textY + 1.0f, theme.getGray().mix(theme.getColor(), animEnable).mulAlpha(alpha));
        ctx.drawText(font, this.setting.getDisplayName(), x + 18.0f, textY, textColor);
        float inputWidth = Math.max(72.0f, moduleWidth * 0.48f);
        float inputHeight = 8.0f;
        float inputX = x + moduleWidth - inputWidth - 8.0f;
        float inputY = settingY;
        this.inputBounds = new Rect(inputX, inputY, inputWidth, inputHeight);
        ColorRGBA bg = theme.getForegroundLight().mulAlpha(alpha);
        ColorRGBA border = (this.editing ? theme.getColor() : theme.getForegroundLightStroke()).mulAlpha(alpha);
        ColorRGBA valueColor = theme.getGrayLight().mix(theme.getWhite(), animEnable).mulAlpha(alpha);
        ctx.drawRoundedRect(inputX, inputY, inputWidth, inputHeight, BorderRadius.all(2.0f), bg);
        ctx.drawRoundedBorder(inputX, inputY, inputWidth, inputHeight, -0.1f, BorderRadius.all(2.0f), border);
        String display = MenuStringSetting.fitFromEnd(this.setting.getValue(), font, inputWidth - 6.0f);
        float valueY = inputY + 1.0f;
        float valueX = inputX + 3.0f;
        ctx.drawText(font, display, valueX, valueY, valueColor);
        if (this.editing && System.currentTimeMillis() / 500L % 2L == 0L && (cursorX = valueX + font.width(display) + 1.0f) < inputX + inputWidth - 2.0f) {
            ctx.drawRect(cursorX, inputY + 1.0f, 1.0f, inputHeight - 2.0f, valueColor);
        }
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        if (button != MouseButton.LEFT) {
            return false;
        }
        this.editing = this.inputBounds != null && this.inputBounds.contains(mouseX, mouseY);
        return this.editing;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.editing) {
            return false;
        }
        if (keyCode == 257 || keyCode == 335 || keyCode == 256) {
            this.editing = false;
            return true;
        }
        if (keyCode == 259) {
            String value = this.setting.getValue();
            if (!value.isEmpty()) {
                this.setting.setValue(value.substring(0, value.length() - 1));
            }
            return true;
        }
        if ((modifiers & 2) != 0 && keyCode == 86 && mc != null && MenuStringSetting.mc.keyboard != null) {
            String clip = MenuStringSetting.mc.keyboard.getClipboard();
            if (clip != null && !clip.isEmpty()) {
                this.setting.setValue(this.setting.getValue() + clip);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (!this.editing) {
            return false;
        }
        if (Character.isISOControl(chr)) {
            return false;
        }
        this.setting.setValue(this.setting.getValue() + chr);
        return true;
    }

    @Override
    public float getWidth() {
        return 0.0f;
    }

    @Override
    public float getHeight() {
        return 8.0f;
    }

    @Override
    public boolean isVisible() {
        return this.setting.getVisible().get();
    }

    private static String fitFromEnd(String input, Font font, float maxWidth) {
        int start;
        if (input == null || input.isEmpty()) {
            return "";
        }
        if (font.width(input) <= maxWidth) {
            return input;
        }
        for (start = input.length() - 1; start > 0 && font.width("..." + input.substring(start)) < maxWidth; --start) {
        }
        return "..." + input.substring(Math.min(input.length() - 1, start + 1));
    }

    @Generated
    public StringSetting getSetting() {
        return this.setting;
    }
}

