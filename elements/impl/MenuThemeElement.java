
package vurst.visual.client.screens.menu.elements.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import vurst.visual.VurstVisual;
import vurst.visual.base.animations.base.Animation;
import vurst.visual.base.animations.base.Easing;
import vurst.visual.base.font.Font;
import vurst.visual.base.font.Fonts;
import vurst.visual.base.theme.Theme;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.setting.impl.ButtonSetting;
import vurst.visual.client.modules.api.setting.impl.ColorSetting;
import vurst.visual.client.modules.api.setting.impl.ModeSetting;
import vurst.visual.client.screens.menu.elements.api.AbstractMenuElement;
import vurst.visual.client.screens.menu.settings.api.MenuSetting;
import vurst.visual.client.screens.menu.settings.impl.MenuButtonSetting;
import vurst.visual.client.screens.menu.settings.impl.MenuColorSetting;
import vurst.visual.client.screens.menu.settings.impl.MenuModeSetting;
import vurst.visual.utility.game.other.MouseButton;
import vurst.visual.utility.render.display.base.BorderRadius;
import vurst.visual.utility.render.display.base.Rect;
import vurst.visual.utility.render.display.base.UIContext;
import vurst.visual.utility.render.display.base.color.ColorRGBA;
import vurst.visual.utility.render.display.shader.DrawUtil;

public class MenuThemeElement
extends AbstractMenuElement {
    private ColorSetting color;
    private ColorSetting secondColor;
    private ColorSetting gray;
    private ColorSetting grayLight;
    private ColorSetting foregroundLight;
    private ColorSetting whiteGray;
    private ColorSetting foregroundGray;
    private ColorSetting foregroundLightStroke;
    private ColorSetting foregroundColor;
    private ColorSetting foregroundStroke;
    private ColorSetting foregroundDark;
    private ColorSetting white;
    private ColorSetting backgroundColor;
    private MenuButtonSetting button;
    private final List<MenuSetting> settings = new ArrayList<MenuSetting>();
    private final Theme theme;
    private final ModeSetting colorMode;
    private final Animation animation;
    private final Animation animationPosition;
    private final Animation animationY;
    private Rect bounds;
    private int lastColum = -1;
    boolean animated = false;
    private boolean colorModeSynced = false;

    public MenuThemeElement(Theme theme) {
        this.theme = theme;
        this.animation = new Animation(200L, VurstVisual.getInstance().getThemeManager().is(theme) ? 1.0f : 0.0f, Easing.LINEAR);
        this.animationPosition = new Animation(150L, 1.0f, Easing.QUAD_IN_OUT);
        this.animationY = new Animation(150L, 1.0f, Easing.QUAD_IN_OUT);
        this.color = new ColorSetting("Первый цвет", theme.getColor(), Theme.DARK::getColor);
        this.secondColor = new ColorSetting("Второй цвет", theme.getSecondColorRaw(), Theme.DARK::getSecondColorRaw);
        this.colorMode = new ModeSetting("Цвета", "2 цвета", "1 цвет");
        if (!theme.isUseSecondColor()) {
            this.colorMode.set("1 цвет");
        }
        this.secondColor.setVisible(() -> this.colorMode.is("2 цвета"));
        if (this.theme == Theme.CUSTOM_THEME) {
            this.backgroundColor = new ColorSetting("GUI background", theme.getBackgroundColor(), Theme.DARK::getBackgroundColor);
            this.foregroundColor = new ColorSetting("Foreground", theme.getForegroundColor(), Theme.DARK::getForegroundColor);
            this.foregroundLight = new ColorSetting("Foreground light", theme.getForegroundLight(), Theme.DARK::getForegroundLight);
            this.foregroundDark = new ColorSetting("Foreground dark", theme.getForegroundDark(), Theme.DARK::getForegroundDark);
            this.foregroundGray = new ColorSetting("Foreground gray", theme.getForegroundGray(), Theme.DARK::getForegroundGray);
            this.white = new ColorSetting("Primary text", theme.getWhite(), Theme.DARK::getWhite);
            this.whiteGray = new ColorSetting("Disabled icons", theme.getWhiteGray(), Theme.DARK::getWhiteGray);
            this.gray = new ColorSetting("Disabled text", theme.getGray(), Theme.DARK::getGray);
            this.grayLight = new ColorSetting("Muted text", theme.getGrayLight(), Theme.DARK::getGrayLight);
            this.foregroundLightStroke = new ColorSetting("Bright outline", theme.getForegroundLightStroke(), Theme.DARK::getForegroundLightStroke);
            this.foregroundStroke = new ColorSetting("Outline", theme.getForegroundStroke(), Theme.DARK::getForegroundStroke);
            Collections.addAll(this.settings, new MenuModeSetting(this.colorMode), new MenuColorSetting(this.color), new MenuColorSetting(this.secondColor), new MenuColorSetting(this.backgroundColor), new MenuColorSetting(this.foregroundColor), new MenuColorSetting(this.foregroundLight), new MenuColorSetting(this.foregroundDark), new MenuColorSetting(this.foregroundGray), new MenuColorSetting(this.white), new MenuColorSetting(this.whiteGray), new MenuColorSetting(this.gray), new MenuColorSetting(this.grayLight), new MenuColorSetting(this.foregroundLightStroke), new MenuColorSetting(this.foregroundStroke));
            this.button = new MenuButtonSetting(new ButtonSetting("Reset", () -> {
                for (MenuSetting setting : this.settings) {
                    if (!(setting instanceof MenuColorSetting)) continue;
                    MenuColorSetting colorSetting = (MenuColorSetting)setting;
                    colorSetting.getSetting().reset();
                }
            }));
            this.settings.add(this.button);
        } else {
            Collections.addAll(this.settings, new MenuModeSetting(this.colorMode), new MenuColorSetting(this.color), new MenuColorSetting(this.secondColor));
        }
    }

    @Override
    public void render(UIContext ctx, float mouseX, float mouseY, Font font, float x, float y, float moduleWidth, float alpha, int colum) {
        if (this.lastColum == -1) {
            this.lastColum = colum;
        }
        if (this.lastColum != colum) {
            this.animated = true;
            this.animationPosition.animateTo(x);
            this.animationY.animateTo(y);
            this.lastColum = colum;
        }
        if (this.animated) {
            x = this.animationPosition.update(x);
            y = this.animationY.update(y);
            if (this.animationPosition.isDone() && this.animationY.isDone()) {
                this.animated = false;
            }
        } else {
            this.animationPosition.reset(x);
            this.animationY.reset(y);
        }
        if (!this.colorModeSynced) {
            this.colorMode.set(this.theme.isUseSecondColor() ? "2 цвета" : "1 цвет");
            this.colorModeSynced = true;
        }
        this.animation.update(VurstVisual.getInstance().getThemeManager().is(this.theme) ? 1.0f : 0.0f);
        float moduleHeight = 22.0f;
        Theme theme = VurstVisual.getInstance().getThemeManager().getCurrentTheme();
        ColorRGBA moduleBg = theme.getForegroundColor().mulAlpha(alpha);
        boolean hasSettings = this.hasSettings();
        float settingAreaHeight = this.getHeight();
        ColorRGBA settingBg = theme.getForegroundDark().mulAlpha(alpha);
        this.bounds = new Rect(x, y, moduleWidth, moduleHeight);
        if (hasSettings) {
            ctx.drawRoundedRect(x, y, moduleWidth, settingAreaHeight, BorderRadius.all(8.0f), settingBg);
            ctx.drawRoundedRect(x, y, moduleWidth, moduleHeight, BorderRadius.top(8.0f, 8.0f), moduleBg);
            DrawUtil.drawRoundedBorder(ctx.getMatrices(), x, y, moduleWidth, settingAreaHeight, -0.1f, BorderRadius.all(8.0f), theme.getForegroundStroke().mulAlpha(alpha));
        } else {
            ctx.drawRoundedRect(x, y, moduleWidth, moduleHeight, BorderRadius.all(8.0f), moduleBg);
            DrawUtil.drawRoundedBorder(ctx.getMatrices(), x, y, moduleWidth, moduleHeight, -0.1f, BorderRadius.all(8.0f), theme.getForegroundStroke().mulAlpha(alpha));
        }
        ColorRGBA enabledColor = theme.getGray().mix(theme.getColor(), this.animation.getValue()).mulAlpha(alpha);
        ColorRGBA textColor = theme.getGrayLight().mix(theme.getWhite(), this.animation.getValue()).mulAlpha(alpha);
        ctx.drawText(Fonts.ICONS.getFont(5.5f), "B", x + 8.0f, y + 9.0f, enabledColor);
        ctx.drawText(font, this.theme.getDisplayName(), x + 18.0f, y + 9.0f, textColor);
        float keyBoxWidth = 22.5f;
        float keyBoxX = x + moduleWidth - keyBoxWidth;
        ColorRGBA badgeColor = this.theme.getColor().mulAlpha(alpha);
        ctx.drawRoundedRect(keyBoxX, y, keyBoxWidth, moduleHeight, hasSettings ? BorderRadius.topRight(8.0f) : new BorderRadius(0.0f, 6.0f, 6.0f, 0.0f), badgeColor);
        float padding = 8.0f;
        float startY = y + moduleHeight + padding;
        ColorRGBA descriptionColor = theme.getWhiteGray().mix(theme.getGrayLight(), this.animation.getValue()).mulAlpha(alpha);
        for (MenuSetting setting : this.settings) {
            setting.render(ctx, mouseX, mouseY, x, startY, moduleWidth, alpha, this.animation.getValue(), enabledColor, textColor, descriptionColor, theme);
            startY += setting.getHeight() + 8.0f;
        }
        if (hasSettings) {
            DrawUtil.drawRoundedBorder(ctx.getMatrices(), x, y, moduleWidth, settingAreaHeight, -0.1f, BorderRadius.all(8.0f), theme.getForegroundStroke().mulAlpha(alpha));
        } else {
            DrawUtil.drawRoundedBorder(ctx.getMatrices(), x, y, moduleWidth, moduleHeight, -0.1f, BorderRadius.all(8.0f), theme.getForegroundStroke().mulAlpha(alpha));
        }
        if (this.theme == Theme.CUSTOM_THEME) {
            this.theme.setColor(this.color.getColor());
            this.theme.setSecondColor(this.secondColor.getColor());
            this.theme.setUseSecondColor(this.colorMode.is("2 цвета"));
            this.theme.setGray(this.gray.getColor());
            this.theme.setGrayLight(this.grayLight.getColor());
            this.theme.setForegroundLight(this.foregroundLight.getColor());
            this.theme.setWhiteGray(this.whiteGray.getColor());
            this.theme.setForegroundGray(this.foregroundGray.getColor());
            this.theme.setForegroundLightStroke(this.foregroundLightStroke.getColor());
            this.theme.setForegroundColor(this.foregroundColor.getColor());
            this.theme.setForegroundStroke(this.foregroundStroke.getColor());
            this.theme.setForegroundDark(this.foregroundDark.getColor());
            this.theme.setWhite(this.white.getColor());
            this.theme.setBackgroundColor(this.backgroundColor.getColor());
        } else {
            this.theme.setColor(this.color.getColor());
            this.theme.setSecondColor(this.secondColor.getColor());
            this.theme.setUseSecondColor(this.colorMode.is("2 цвета"));
        }
    }

    @Override
    public float getHeight() {
        return (float)(22.0 + (this.hasSettings() ? this.settings.stream().mapToDouble(MenuSetting::getHeight).sum() + 8.0 + (double)(8 * this.settings.size()) : 0.0));
    }

    private boolean hasSettings() {
        return !this.settings.isEmpty();
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        MenuSetting setting;
        if (this.bounds != null && this.bounds.contains(mouseX, mouseY)) {
            VurstVisual.getInstance().getThemeManager().switchTheme(this.theme);
        }
        Iterator<MenuSetting> iterator2 = this.settings.iterator();
        while (!(!iterator2.hasNext() || (setting = iterator2.next()).isVisible() && setting.onMouseClicked(mouseX, mouseY, button))) {
        }
    }

    @Override
    public void onMouseReleased(double mouseX, double mouseY, MouseButton button) {
    }

    @Override
    public void onMouseDragged(double mouseX, double mouseY, MouseButton button, double deltaX, double deltaY) {
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return false;
    }

    @Override
    public Category getCategory() {
        return Category.THEMES;
    }

    @Override
    public String getName() {
        return this.theme.getDisplayName();
    }
}

