
package vurst.visual.client.screens.menu.elements.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import lombok.Generated;
import vurst.visual.VurstVisual;
import vurst.visual.base.animations.base.Animation;
import vurst.visual.base.animations.base.Easing;
import vurst.visual.base.font.Font;
import vurst.visual.base.font.Fonts;
import vurst.visual.base.theme.Theme;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.Module;
import vurst.visual.client.modules.api.setting.Setting;
import vurst.visual.client.modules.api.setting.impl.BooleanSetting;
import vurst.visual.client.modules.api.setting.impl.ButtonSetting;
import vurst.visual.client.modules.api.setting.impl.ColorSetting;
import vurst.visual.client.modules.api.setting.impl.ItemSelectSetting;
import vurst.visual.client.modules.api.setting.impl.KeySetting;
import vurst.visual.client.modules.api.setting.impl.ModeSetting;
import vurst.visual.client.modules.api.setting.impl.MultiBooleanSetting;
import vurst.visual.client.modules.api.setting.impl.NumberSetting;
import vurst.visual.client.modules.api.setting.impl.StringSetting;
import vurst.visual.client.screens.menu.elements.api.AbstractMenuElement;
import vurst.visual.client.screens.menu.settings.api.MenuSetting;
import vurst.visual.client.screens.menu.settings.impl.MenuBooleanSetting;
import vurst.visual.client.screens.menu.settings.impl.MenuButtonSetting;
import vurst.visual.client.screens.menu.settings.impl.MenuColorSetting;
import vurst.visual.client.screens.menu.settings.impl.MenuItemSetting;
import vurst.visual.client.screens.menu.settings.impl.MenuKeySetting;
import vurst.visual.client.screens.menu.settings.impl.MenuModeSetting;
import vurst.visual.client.screens.menu.settings.impl.MenuSelectSetting;
import vurst.visual.client.screens.menu.settings.impl.MenuSliderSetting;
import vurst.visual.client.screens.menu.settings.impl.MenuStringSetting;
import vurst.visual.utility.game.other.MouseButton;
import vurst.visual.utility.render.display.Keyboard;
import vurst.visual.utility.render.display.base.BorderRadius;
import vurst.visual.utility.render.display.base.Rect;
import vurst.visual.utility.render.display.base.UIContext;
import vurst.visual.utility.render.display.base.color.ColorRGBA;
import vurst.visual.utility.render.display.shader.DrawUtil;

public class MenuModuleElement
extends AbstractMenuElement {
    private final Module module;
    private final List<MenuSetting> settings = new ArrayList<MenuSetting>();
    private final Animation animation;
    private final Animation animationPosition;
    private final Animation animationY;
    private Rect bounds;
    private Rect boundsBind;
    private boolean binding = false;
    private int lastColum = -1;
    private boolean expanded = false;
    boolean animated = false;

    public MenuModuleElement(Module module) {
        this.module = module;
        this.animation = new Animation(200L, module.isEnabled() ? 1.0f : 0.0f, Easing.LINEAR);
        this.animationPosition = new Animation(150L, 1.0f, Easing.QUAD_IN_OUT);
        this.animationY = new Animation(150L, 1.0f, Easing.QUAD_IN_OUT);
        for (Setting setting : module.getSettings()) {
            if (setting instanceof NumberSetting) {
                NumberSetting sliderSetting = (NumberSetting)setting;
                this.settings.add(new MenuSliderSetting(sliderSetting));
                continue;
            }
            if (setting instanceof ModeSetting) {
                ModeSetting modeSetting = (ModeSetting)setting;
                this.settings.add(new MenuModeSetting(modeSetting));
                continue;
            }
            if (setting instanceof MultiBooleanSetting) {
                MultiBooleanSetting selectSetting = (MultiBooleanSetting)setting;
                this.settings.add(new MenuSelectSetting(selectSetting));
                continue;
            }
            if (setting instanceof BooleanSetting) {
                BooleanSetting booleanSetting = (BooleanSetting)setting;
                this.settings.add(new MenuBooleanSetting(booleanSetting));
                continue;
            }
            if (setting instanceof ColorSetting) {
                ColorSetting colorSetting = (ColorSetting)setting;
                if (MenuModuleElement.isSecondaryColorSetting(colorSetting)) continue;
                this.settings.add(new MenuColorSetting(colorSetting));
                continue;
            }
            if (setting instanceof ButtonSetting) {
                ButtonSetting buttonSetting = (ButtonSetting)setting;
                this.settings.add(new MenuButtonSetting(buttonSetting));
                continue;
            }
            if (setting instanceof ItemSelectSetting) {
                ItemSelectSetting itemSelectSetting = (ItemSelectSetting)setting;
                this.settings.add(new MenuItemSetting(itemSelectSetting));
                continue;
            }
            if (setting instanceof KeySetting) {
                KeySetting keySetting = (KeySetting)setting;
                this.settings.add(new MenuKeySetting(keySetting));
                continue;
            }
            if (!(setting instanceof StringSetting)) continue;
            StringSetting stringSetting = (StringSetting)setting;
            this.settings.add(new MenuStringSetting(stringSetting));
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
        this.animation.animateTo(this.module.isEnabled() ? 1.0f : 0.0f);
        this.animation.update();
        float moduleHeight = 22.0f;
        Theme theme = VurstVisual.getInstance().getThemeManager().getCurrentTheme();
        ColorRGBA moduleBg = theme.getForegroundColor().mulAlpha(alpha);
        boolean hasSettings = this.hasSettings();
        boolean showSettings = this.expanded && hasSettings;
        float settingAreaHeight = showSettings ? this.getHeight() : moduleHeight;
        ColorRGBA settingBg = theme.getForegroundDark().mulAlpha(alpha);
        this.bounds = new Rect(x, y, moduleWidth, moduleHeight);
        if (showSettings) {
            ctx.drawRoundedRect(x, y, moduleWidth, settingAreaHeight, BorderRadius.all(8.0f), settingBg);
            ctx.drawRoundedRect(x, y, moduleWidth, moduleHeight, BorderRadius.top(8.0f, 8.0f), moduleBg);
        } else {
            ctx.drawRoundedRect(x, y, moduleWidth, moduleHeight, BorderRadius.all(8.0f), moduleBg);
        }
        ColorRGBA enabledColor = theme.getGray().mix(theme.getColor(), this.animation.getValue()).mulAlpha(alpha);
        ColorRGBA textColor = theme.getGrayLight().mix(theme.getWhite(), this.animation.getValue()).mulAlpha(alpha);
        ctx.drawText(Fonts.ICONS.getFont(5.5f), "B", x + 8.0f, y + 9.0f, enabledColor);
        ctx.drawText(font, this.module.getDisplayName(), x + 18.0f, y + 9.0f, textColor);
        String keyText = "None";
        int keyCode = this.module.getKeyCode();
        if (keyCode != -1 && keyCode != 0) {
            try {
                String name = Keyboard.getKeyName(keyCode);
                if (name != null && !name.isBlank()) {
                    keyText = name.toUpperCase();
                }
            }
            catch (Exception name) {
                
            }
        }
        Font keyFont = Fonts.MEDIUM.getFont(7.0f);
        float keyPadding = 2.0f;
        float keyBoxWidth = Math.max(22.5f, keyFont.width(keyText) + keyPadding * 2.0f + 2.0f);
        float keyBoxX = x + moduleWidth - keyBoxWidth;
        ColorRGBA badgeColor = this.isBinding() ? theme.getSecondColor() : (this.module.getKeyCode() != -1 ? theme.getWhiteGray().mix(theme.getColor(), this.animation.getValue()).mulAlpha(alpha) : theme.getForegroundLight().mulAlpha(alpha));
        ctx.drawRoundedRect(keyBoxX, y, keyBoxWidth, moduleHeight, showSettings ? BorderRadius.topRight(8.0f) : new BorderRadius(0.0f, 8.0f, 8.0f, 0.0f), badgeColor);
        float keyTextY = y + (moduleHeight - keyFont.height()) / 2.0f;
        float keyContentWidth = keyBoxWidth - keyPadding * 2.0f;
        float keyContentX = keyBoxX + keyPadding;
        ColorRGBA keyColor = (keyCode != -1 ? theme.getGrayLight().mix(theme.getWhite(), this.animation.getValue()) : theme.getGray()).mulAlpha(alpha);
        this.boundsBind = new Rect(keyBoxX, y, keyBoxWidth, moduleHeight);
        ctx.enableScissor((int)Math.floor(keyBoxX), (int)Math.floor(y), (int)Math.ceil(keyBoxX + keyBoxWidth), (int)Math.ceil(y + moduleHeight));
        this.drawScrollingText(ctx, keyFont, keyText, keyContentX, keyTextY, keyContentWidth, keyColor);
        ctx.disableScissor();
        if (showSettings) {
            float padding = 8.0f;
            float startY = y + moduleHeight + padding;
            ColorRGBA descriptionColor = theme.getWhiteGray().mix(theme.getGrayLight(), this.animation.getValue()).mulAlpha(alpha);
            for (MenuSetting setting : this.settings) {
                if (!setting.isVisible()) continue;
                setting.render(ctx, mouseX, mouseY, x, startY, moduleWidth, alpha, this.animation.getValue(), enabledColor, textColor, descriptionColor, theme);
                startY += setting.getHeight() + 8.0f;
            }
            DrawUtil.drawRoundedBorder(ctx.getMatrices(), x, y, moduleWidth, settingAreaHeight, -0.1f, BorderRadius.all(8.0f), theme.getForegroundStroke().mulAlpha(alpha));
        } else {
            DrawUtil.drawRoundedBorder(ctx.getMatrices(), x, y, moduleWidth, moduleHeight, -0.1f, BorderRadius.all(8.0f), theme.getForegroundStroke().mulAlpha(alpha));
        }
    }

    private void drawScrollingText(UIContext ctx, Font font, String text, float x, float y, float maxWidth, ColorRGBA color) {
        float offset;
        float textW = font.width(text);
        if (textW <= maxWidth) {
            float centeredX = x + (maxWidth - textW) / 2.0f;
            ctx.drawText(font, text, centeredX, y, color);
            return;
        }
        float scrollMax = textW - maxWidth;
        float pauseMs = 700.0f;
        float slideMs = 1400.0f;
        float total = pauseMs + slideMs + pauseMs + slideMs;
        long now = System.currentTimeMillis();
        float t = now % (long)total;
        if (t < pauseMs) {
            offset = 0.0f;
        } else if (t < pauseMs + slideMs) {
            float k = (t - pauseMs) / slideMs;
            k = k * k * (3.0f - 2.0f * k);
            offset = k * scrollMax;
        } else if (t < pauseMs + slideMs + pauseMs) {
            offset = scrollMax;
        } else {
            float k = (t - pauseMs - slideMs - pauseMs) / slideMs;
            k = k * k * (3.0f - 2.0f * k);
            offset = scrollMax * (1.0f - k);
        }
        ctx.drawText(font, text, x - offset, y, color);
    }

    @Override
    public float getHeight() {
        return this.expanded && this.hasSettings() ? (float)(22.0 + this.settings.stream().filter(MenuSetting::isVisible).mapToDouble(m -> m.getHeight() + 8.0f).sum() + 8.0) : 22.0f;
    }

    public boolean hasSettings() {
        return !this.settings.isEmpty() && this.settings.stream().anyMatch(MenuSetting::isVisible);
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        block9: {
            MenuSetting setting;
            if (this.bounds != null && this.bounds.contains(mouseX, mouseY)) {
                if (button.getButtonIndex() > 2 && this.binding) {
                    this.binding = false;
                    this.module.setKeyCode(button.getButtonIndex());
                }
                if (button == MouseButton.RIGHT && this.hasSettings()) {
                    boolean bl = this.expanded = !this.expanded;
                }
                if (button == MouseButton.LEFT) {
                    if (this.boundsBind != null && this.boundsBind.contains(mouseX, mouseY)) {
                        this.binding = !this.binding;
                    } else {
                        this.module.toggle();
                    }
                } else if (button == MouseButton.MIDDLE) {
                    boolean bl = this.binding = !this.binding;
                }
            }
            if (!this.expanded) break block9;
            Iterator<MenuSetting> iterator2 = this.settings.iterator();
            while (!(!iterator2.hasNext() || (setting = iterator2.next()).isVisible() && setting.onMouseClicked(mouseX, mouseY, button))) {
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.binding) {
            if (keyCode == 256 || keyCode == 261 || keyCode == 259) {
                this.module.setKeyCode(-1);
            } else {
                this.module.setKeyCode(keyCode);
            }
            this.binding = false;
            return true;
        }
        boolean result = false;
        if (this.expanded) {
            for (MenuSetting setting : this.settings) {
                if (!setting.isVisible() || !setting.keyPressed(keyCode, scanCode, modifiers)) continue;
                result = true;
            }
        }
        return result;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (!this.expanded) {
            return false;
        }
        boolean result = false;
        for (MenuSetting setting : this.settings) {
            if (!setting.isVisible() || !setting.charTyped(chr, modifiers)) continue;
            result = true;
        }
        return result;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return true;
    }

    @Override
    public Category getCategory() {
        return this.module.getCategory();
    }

    @Override
    public String getName() {
        return this.module.getDisplayName();
    }

    @Override
    public void onMouseReleased(double mouseX, double mouseY, MouseButton button) {
        if (this.expanded) {
            for (MenuSetting setting : this.settings) {
                if (!setting.isVisible()) continue;
                setting.onMouseReleased(mouseX, mouseY, button);
            }
        }
    }

    @Override
    public void onMouseDragged(double mouseX, double mouseY, MouseButton button, double deltaX, double deltaY) {
    }

    private static boolean isSecondaryColorSetting(ColorSetting setting) {
        String name = setting.getName();
        if (name == null) {
            return false;
        }
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.contains("second") || lower.contains("второй");
    }

    public boolean isModuleHovered(double mouseX, double mouseY) {
        return this.bounds != null && this.bounds.contains(mouseX, mouseY);
    }

    public String getDescription() {
        if (this.module.getInfo() == null) {
            return "";
        }
        String description = this.module.getDisplayDescription();
        return description == null ? "" : description;
    }

    @Generated
    public Module getModule() {
        return this.module;
    }

    @Generated
    public boolean isBinding() {
        return this.binding;
    }
}

