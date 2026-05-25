
package vurst.visual.client.screens.menu.settings.impl;

import lombok.Generated;
import net.minecraft.ItemStack;
import vurst.visual.base.font.Font;
import vurst.visual.base.font.Fonts;
import vurst.visual.base.theme.Theme;
import vurst.visual.client.modules.api.setting.ItemIconProvider;
import vurst.visual.client.modules.api.setting.impl.KeySetting;
import vurst.visual.client.screens.menu.settings.api.MenuSetting;
import vurst.visual.utility.game.other.MouseButton;
import vurst.visual.utility.render.display.Keyboard;
import vurst.visual.utility.render.display.base.BorderRadius;
import vurst.visual.utility.render.display.base.Rect;
import vurst.visual.utility.render.display.base.UIContext;
import vurst.visual.utility.render.display.base.color.ColorRGBA;

public class MenuKeySetting
extends MenuSetting {
    private static final float ITEM_ICON_SCALE = 0.6f;
    private static final float ITEM_ICON_SIZE = 9.6f;
    private static final float ITEM_ICON_GAP = 3.0f;
    private final KeySetting setting;
    private Rect bounds;
    private boolean binding;

    public MenuKeySetting(KeySetting setting) {
        this.setting = setting;
    }

    @Override
    public void render(UIContext ctx, float mouseX, float mouseY, float x, float settingY, float moduleWidth, float alpha, float animEnable, ColorRGBA themeColor, ColorRGBA textColor, ColorRGBA descriptionColor, Theme theme) {
        ItemIconProvider provider;
        float settingX = x + 8.0f;
        Font settingFont = Fonts.MEDIUM.getFont(7.0f);
        float textY = settingY + (8.0f - settingFont.height()) / 2.0f - 0.5f;
        float textX = x + 8.0f + 10.0f;
        KeySetting keySetting = this.setting;
        if (keySetting instanceof ItemIconProvider && this.drawItemIcon(ctx, (provider = (ItemIconProvider)((Object)keySetting)).getMenuIconStack(), settingX, settingY - 1.0f)) {
            textX = x + 8.0f + 9.6f + 3.0f;
        } else {
            float iconY = textY - 1.0f;
            ctx.drawText(Fonts.ICONS.getFont(5.5f), "L", settingX + 1.2f, iconY + 1.2f, theme.getGray().mix(theme.getColor(), animEnable).mulAlpha(alpha));
        }
        ctx.drawText(settingFont, this.setting.getDisplayName(), textX, textY, textColor);
        Object keyText = this.binding ? "..." : "None";
        int keyCode = this.setting.getKeyCode();
        if (keyCode != -1 && keyCode != 0 && !this.binding) {
            try {
                String name = Keyboard.getKeyName(keyCode);
                if (name != null && !name.isBlank() && ((String)(keyText = name.toLowerCase())).length() > 6) {
                    keyText = ((String)keyText).substring(0, 6) + "..";
                }
            }
            catch (Exception name) {
                
            }
        }
        Font font = Fonts.MEDIUM.getFont(7.0f);
        float toggleWidth = 4.0f + font.width((String)keyText) + 4.0f;
        float toggleHeight = 8.0f;
        float toggleX = x + moduleWidth - toggleWidth - 8.0f;
        float toggleY = settingY;
        ColorRGBA colorToggle = theme.getForegroundLight().mix(theme.getColor(), animEnable).mulAlpha(alpha);
        ColorRGBA borderColor = theme.getForegroundLightStroke().mix(theme.getForegroundLightStroke().mulAlpha(0.0f), animEnable).mulAlpha(alpha);
        ctx.drawRoundedRect(toggleX, toggleY, toggleWidth, toggleHeight, BorderRadius.all(2.0f), colorToggle);
        ctx.drawRoundedBorder(toggleX, toggleY, toggleWidth, toggleHeight, -0.1f, BorderRadius.all(2.0f), borderColor);
        ctx.drawText(font, (String)keyText, toggleX + 4.0f, toggleY + 1.0f, textColor);
        this.bounds = new Rect(toggleX, toggleY, toggleWidth, toggleHeight);
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        if (this.binding && button.getButtonIndex() >= 2) {
            this.setting.setKeyCode(button.getButtonIndex());
            this.binding = false;
            return true;
        }
        if (this.bounds != null && this.bounds.contains(mouseX, mouseY)) {
            this.binding = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.binding) {
            return false;
        }
        if (keyCode == 256 || keyCode == 261 || keyCode == 259) {
            keyCode = -1;
        }
        this.setting.setKeyCode(keyCode);
        this.binding = false;
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

    @Generated
    public KeySetting getSetting() {
        return this.setting;
    }
}

