
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
import vurst.visual.client.modules.api.setting.impl.MultiBooleanSetting;
import vurst.visual.client.screens.menu.settings.api.MenuSetting;
import vurst.visual.utility.game.other.MouseButton;
import vurst.visual.utility.render.display.base.BorderRadius;
import vurst.visual.utility.render.display.base.Rect;
import vurst.visual.utility.render.display.base.UIContext;
import vurst.visual.utility.render.display.base.color.ColorRGBA;
import vurst.visual.utility.render.display.shader.DrawUtil;

public class MenuSelectSetting
extends MenuSetting {
    private final MultiBooleanSetting setting;
    private final Map<MultiBooleanSetting.Value, Rect> modeSettingOptionBounds = new HashMap<MultiBooleanSetting.Value, Rect>();
    private Rect bounds;
    private boolean expanded;
    private final Animation expandedAnimation = new Animation(200L, 0.0f, Easing.QUAD_IN_OUT);

    public MenuSelectSetting(MultiBooleanSetting setting) {
        this.setting = setting;
    }

    @Override
    public void render(UIContext ctx, float mouseX, float mouseY, float x, float settingY, float moduleWidth, float alpha, float animEnable, ColorRGBA themeColor, ColorRGBA textColor, ColorRGBA descriptionColor, Theme theme) {
        Font settingFont = Fonts.MEDIUM.getFont(7.0f);
        Font optionFont = Fonts.MEDIUM.getFont(6.0f);
        Font iconFont = Fonts.ICONS.getFont(6.0f);
        float nameX = x + 18.0f;
        ctx.drawText(settingFont, this.setting.getDisplayName(), nameX, settingY + (13.0f - settingFont.height()) / 2.0f - 0.5f, textColor);
        ctx.drawText(iconFont, "E", x + 8.0f, settingY + (13.0f - iconFont.height()) / 2.0f - 1.0f, themeColor);
        float dropdownWidth = moduleWidth / 2.0f;
        float dropdownHeight = 13.0f + this.expandedAnimation.update(this.expanded ? 1.0f : 0.0f) * (float)this.setting.getBooleanSettings().size() * 13.0f;
        float dropdownX = x + moduleWidth - dropdownWidth - 8.0f;
        ctx.drawRoundedRect(dropdownX, settingY, dropdownWidth, dropdownHeight, BorderRadius.all(3.0f), theme.getForegroundColor().mulAlpha(alpha));
        ctx.drawRoundedRect(dropdownX, settingY, dropdownWidth, 13.0f, this.expanded ? BorderRadius.top(3.0f, 3.0f) : BorderRadius.all(3.0f), theme.getForegroundLight().mulAlpha(alpha));
        String currentModeText = this.setting.getSelectedValues().isEmpty() ? "----" : this.setting.getSelectedValues().getFirst().getDisplayName() + (String)(this.setting.getSelectedValues().size() > 1 ? " +" + (this.setting.getSelectedValues().size() - 1) : "");
        ctx.drawText(optionFont, currentModeText, dropdownX + 6.0f, settingY + (13.0f - optionFont.height()) / 2.0f, textColor);
        float arrowX = dropdownX + dropdownWidth - 8.0f - 4.0f;
        float arrowY = settingY + 5.5f;
        ColorRGBA color = theme.getGray().mix(theme.getGrayLight(), animEnable).mulAlpha(alpha);
        ctx.pushMatrix();
        float endX = arrowX + iconFont.width("Q") / 2.0f - 1.0f;
        float endY = arrowY + iconFont.height() / 2.0f - 1.0f;
        ctx.getMatrices().translate(endX, endY, 0.0f);
        ctx.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0f * this.expandedAnimation.getValue()));
        ctx.getMatrices().translate(-endX, -endY, 0.0f);
        ctx.drawText(iconFont, "Q", (float)((int)arrowX), (float)((int)arrowY), color);
        ctx.popMatrix();
        ctx.enableScissor((int)dropdownX - 1, (int)settingY, (int)(dropdownX + dropdownWidth + 1.0f), (int)(settingY + dropdownHeight));
        this.bounds = new Rect(dropdownX, settingY, dropdownWidth, dropdownHeight);
        if (this.expandedAnimation.getValue() != 0.0f) {
            List<MultiBooleanSetting.Value> modes = this.setting.getBooleanSettings();
            ColorRGBA disableColor = theme.getGray().mix(theme.getGrayLight(), animEnable).mulAlpha(alpha);
            ColorRGBA enabledColor = theme.getForegroundGray().mix(theme.getColor(), animEnable).mulAlpha(alpha);
            float optionY = settingY + 13.0f;
            for (MultiBooleanSetting.Value mode : modes) {
                Rect optionRect = new Rect(dropdownX, optionY, dropdownWidth, 13.0f);
                if (optionY > settingY + dropdownHeight) break;
                if (mode.isEnabled()) {
                    ctx.drawRoundedRect(dropdownX + 1.0f, optionY, dropdownWidth - 2.0f, 13.0f, mode == modes.getLast() ? BorderRadius.bottom(3.0f, 3.0f) : BorderRadius.all(0.0f), enabledColor.mulAlpha(this.expandedAnimation.getValue()));
                    ctx.drawText(optionFont, mode.getDisplayName(), dropdownX + 6.0f, optionY + (13.0f - optionFont.height()) / 2.0f, textColor.mulAlpha(this.expandedAnimation.getValue()));
                } else {
                    ctx.drawText(optionFont, mode.getDisplayName(), dropdownX + 6.0f, optionY + (13.0f - optionFont.height()) / 2.0f, disableColor.mulAlpha(this.expandedAnimation.getValue()));
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
            for (Map.Entry<MultiBooleanSetting.Value, Rect> entry : this.modeSettingOptionBounds.entrySet()) {
                if (!entry.getValue().contains(mouseX, mouseY)) continue;
                entry.getKey().toggle();
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
        return 13.0f + this.expandedAnimation.getValue() * (float)this.setting.getBooleanSettings().size() * 13.0f;
    }

    @Override
    public boolean isVisible() {
        return this.setting.getVisible().get();
    }
}

