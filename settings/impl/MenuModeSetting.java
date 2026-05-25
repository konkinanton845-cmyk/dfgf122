
package vurst.visual.client.screens.menu.settings.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.RotationAxis;
import vurst.visual.base.animations.base.Animation;
import vurst.visual.base.animations.base.Easing;
import vurst.visual.base.font.Font;
import vurst.visual.base.font.Fonts;
import vurst.visual.base.theme.Theme;
import vurst.visual.client.modules.api.setting.impl.ModeSetting;
import vurst.visual.client.screens.menu.settings.api.MenuSetting;
import vurst.visual.utility.game.other.MouseButton;
import vurst.visual.utility.render.display.base.BorderRadius;
import vurst.visual.utility.render.display.base.Rect;
import vurst.visual.utility.render.display.base.UIContext;
import vurst.visual.utility.render.display.base.color.ColorRGBA;
import vurst.visual.utility.render.display.shader.DrawUtil;

public class MenuModeSetting
extends MenuSetting {
    private static final float DROPDOWN_SIDE_MARGIN = 8.0f;
    private static final float LABEL_DROPDOWN_GAP = 12.0f;
    private static final float DROPDOWN_TEXT_PADDING = 6.0f;
    private static final float DROPDOWN_ARROW_PADDING = 16.0f;
    private final ModeSetting setting;
    private final Map<ModeSetting.Value, Rect> modeSettingOptionBounds = new HashMap<ModeSetting.Value, Rect>();
    private Rect bounds;
    private boolean expanded;
    private float maxWidthText;
    private final Animation expandedAnimation = new Animation(200L, 0.0f, Easing.QUAD_IN_OUT);

    public MenuModeSetting(ModeSetting setting) {
        this.setting = setting;
        this.maxWidthText = -1.0f;
    }

    @Override
    public void render(UIContext ctx, float mouseX, float mouseY, float x, float settingY, float moduleWidth, float alpha, float animEnable, ColorRGBA themeColor, ColorRGBA textColor, ColorRGBA descriptionColor, Theme theme) {
        if (this.maxWidthText == -1.0f) {
            this.maxWidthText = (float)this.setting.getValues().stream().mapToDouble(value -> Fonts.MEDIUM.getFont(6.0f).width(value.getDisplayName())).max().orElse(0.0);
        }
        Font settingFont = Fonts.MEDIUM.getFont(7.0f);
        Font optionFont = Fonts.MEDIUM.getFont(6.0f);
        Font iconFont = Fonts.ICONS.getFont(6.0f);
        float nameX = x + 18.0f;
        ctx.drawText(iconFont, "K", x + 9.0f, settingY + (13.0f - iconFont.height()) / 2.0f - 1.0f, themeColor);
        float minDropdownWidth = Math.max(moduleWidth / 2.0f, this.maxWidthText + 24.0f);
        float maxDropdownWidth = Math.max(90.0f, moduleWidth - 16.0f);
        float dropdownWidth = Math.min(maxDropdownWidth, minDropdownWidth);
        float dropdownHeight = 13.0f + this.expandedAnimation.update(this.expanded ? 1.0f : 0.0f) * (float)this.setting.getValues().size() * 13.0f;
        float dropdownX = x + moduleWidth - dropdownWidth - 8.0f;
        float labelRight = dropdownX - 12.0f;
        float labelMaxWidth = Math.max(8.0f, labelRight - nameX);
        float textMaxWidth = Math.max(12.0f, dropdownWidth - 16.0f - 12.0f);
        String settingName = MenuModeSetting.fitText(this.setting.getDisplayName(), settingFont, labelMaxWidth);
        ctx.enableScissor((int)nameX, (int)settingY, (int)Math.ceil(labelRight), (int)Math.ceil(settingY + 13.0f));
        ctx.drawText(settingFont, settingName, nameX, settingY + (13.0f - settingFont.height()) / 2.0f - 0.5f, textColor);
        ctx.disableScissor();
        ctx.drawRoundedRect(dropdownX, settingY, dropdownWidth, dropdownHeight, BorderRadius.all(3.0f), theme.getForegroundColor().mulAlpha(alpha));
        ctx.drawRoundedRect(dropdownX, settingY, dropdownWidth, 13.0f, this.expanded ? BorderRadius.top(3.0f, 3.0f) : BorderRadius.all(3.0f), theme.getForegroundLight().mulAlpha(alpha));
        String currentModeText = MenuModeSetting.fitText(this.setting.getValue().getDisplayName(), optionFont, textMaxWidth);
        ctx.drawText(optionFont, currentModeText, dropdownX + 6.0f, settingY + (13.0f - optionFont.height()) / 2.0f, textColor);
        float thickness = 2.0f;
        float length1 = 4.0f;
        float length2 = 4.0f;
        float arrowX = dropdownX + dropdownWidth - 12.0f;
        float arrowY = settingY + 5.5f;
        BorderRadius radius = BorderRadius.ZERO;
        ColorRGBA color = theme.getGray().mix(theme.getGrayLight(), animEnable).mulAlpha(alpha);
        float angle1 = -45.0f;
        float angle2 = 45.0f;
        ctx.pushMatrix();
        float endX = arrowX + iconFont.width("Q") / 2.0f - 1.0f;
        float endY = arrowY + iconFont.height() / 2.0f - 1.0f;
        ctx.getMatrices().translate(endX, endY, 0.0f);
        ctx.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0f * this.expandedAnimation.getValue()));
        ctx.getMatrices().translate(-endX, -endY, 0.0f);
        ctx.drawText(iconFont, "Q", (float)((int)arrowX), (float)((int)arrowY), color);
        ctx.popMatrix();
        ctx.enableScissor((int)dropdownX - 1, (int)settingY, (int)(dropdownX + dropdownWidth + 1.0f), (int)(settingY + dropdownHeight + 1.0f));
        this.bounds = new Rect(dropdownX, settingY, dropdownWidth, dropdownHeight);
        this.modeSettingOptionBounds.clear();
        if (this.expandedAnimation.getValue() != 0.0f) {
            List<ModeSetting.Value> modes = this.setting.getValues();
            ColorRGBA disableColor = theme.getGray().mix(theme.getGrayLight(), animEnable).mulAlpha(alpha);
            ColorRGBA enabledColor = theme.getForegroundGray().mix(theme.getColor(), animEnable).mulAlpha(alpha);
            float optionY = settingY + 13.0f;
            for (ModeSetting.Value mode : modes) {
                Rect optionRect = new Rect(dropdownX, optionY, dropdownWidth, 13.0f);
                if (optionY > settingY + dropdownHeight) break;
                String optionText = MenuModeSetting.fitText(mode.getDisplayName(), optionFont, textMaxWidth);
                if (mode == this.setting.getValue()) {
                    ctx.drawRoundedRect(dropdownX + 1.0f, optionY, dropdownWidth - 2.0f, 13.0f, mode == modes.getLast() ? BorderRadius.bottom(3.0f, 3.0f) : BorderRadius.all(0.0f), enabledColor.mulAlpha(this.expandedAnimation.getValue()));
                    ctx.drawText(optionFont, optionText, dropdownX + 6.0f, optionY + (13.0f - optionFont.height()) / 2.0f, textColor.mulAlpha(this.expandedAnimation.getValue()));
                } else {
                    ctx.drawText(optionFont, optionText, dropdownX + 6.0f, optionY + (13.0f - optionFont.height()) / 2.0f, disableColor.mulAlpha(this.expandedAnimation.getValue()));
                }
                this.modeSettingOptionBounds.put(mode, optionRect);
                optionY += 13.0f;
            }
        }
        ctx.disableScissor();
        DrawUtil.drawRoundedBorder(ctx.getMatrices(), dropdownX, settingY, dropdownWidth, dropdownHeight, 0.2f, BorderRadius.all(3.0f), theme.getForegroundLightStroke().mulAlpha(alpha));
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        if (this.bounds != null && button == MouseButton.RIGHT && this.bounds.contains(mouseX, mouseY)) {
            this.expanded = !this.expanded;
            return true;
        }
        if (this.expanded && button == MouseButton.LEFT) {
            for (Map.Entry<ModeSetting.Value, Rect> entry : this.modeSettingOptionBounds.entrySet()) {
                if (!entry.getValue().contains(mouseX, mouseY)) continue;
                this.setting.setValue(entry.getKey());
                return true;
            }
        }
        return false;
    }

    @Override
    public float getWidth() {
        return 0.0f;
    }

    @Override
    public float getHeight() {
        return 13.0f + this.expandedAnimation.getValue() * (float)this.setting.getValues().size() * 13.0f;
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
}

