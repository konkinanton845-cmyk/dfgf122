
package vurst.visual.client.screens.menu.settings.impl.popup;

import java.awt.Color;
import java.util.Objects;
import net.minecraft.MathHelper;
import net.minecraft.Vector2f;
import vurst.visual.VurstVisual;
import vurst.visual.base.font.Font;
import vurst.visual.base.font.Fonts;
import vurst.visual.base.theme.Theme;
import vurst.visual.client.modules.api.setting.impl.ColorSetting;
import vurst.visual.client.screens.menu.settings.api.MenuPopupSetting;
import vurst.visual.utility.game.other.MouseButton;
import vurst.visual.utility.interfaces.IMinecraft;
import vurst.visual.utility.math.MathUtil;
import vurst.visual.utility.render.display.TextBox;
import vurst.visual.utility.render.display.base.BorderRadius;
import vurst.visual.utility.render.display.base.ChangeRect;
import vurst.visual.utility.render.display.base.Gradient;
import vurst.visual.utility.render.display.base.UIContext;
import vurst.visual.utility.render.display.base.color.ColorRGBA;
import vurst.visual.utility.render.display.base.color.ColorUtil;
import vurst.visual.utility.render.display.shader.DrawUtil;

public class MenuColorPopupSetting
extends MenuPopupSetting {
    private boolean open;
    private float hue;
    private float saturation;
    private float brightness;
    private int alpha;
    private boolean afocused;
    private boolean hfocused;
    private boolean sbfocused;
    private ColorSetting setting;
    private final TextBox colorString;

    public MenuColorPopupSetting(ChangeRect rect, ColorSetting setting) {
        super(rect);
        this.setting = setting;
        this.colorString = new TextBox(new Vector2f(0.0f, 0.0f), Fonts.MEDIUM.getFont(7.0f), "цвет", 78.0f);
        this.colorString.setCharFilter(TextBox.CharFilter.ENGLISH_NUMBERS);
        this.colorString.setMaxLength(6);
        this.updatePos();
        this.animationScale.update(1.0f);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return this.colorString.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return this.colorString.charTyped(chr, modifiers);
    }

    @Override
    public void render(UIContext ctx, float mouseX, float mouseY, float alphas, Theme theme) {
        this.animationScale.update();
        alphas = 1.0f;
        ctx.pushMatrix();
        ctx.getMatrices().translate(this.bounds.getX(), this.bounds.getY() + this.bounds.getHeight() / 2.0f, 0.0f);
        ctx.getMatrices().scale(this.animationScale.getValue(), this.animationScale.getValue(), 1.0f);
        ctx.getMatrices().translate(-this.bounds.getX(), -(this.bounds.getY() + this.bounds.getHeight() / 2.0f), 0.0f);
        ctx.drawRoundedRect(this.bounds.getX(), this.bounds.getY(), this.bounds.getWidth(), this.bounds.getHeight(), BorderRadius.all(4.0f), theme.getForegroundColor().mulAlpha(alphas));
        ctx.drawRoundedRect(this.bounds.getX(), this.bounds.getY(), this.bounds.getWidth(), 18.0f, BorderRadius.top(4.0f, 4.0f), theme.getForegroundLight().mulAlpha(alphas));
        if (IMinecraft.mc.currentScreen == null) {
            this.afocused = false;
            this.hfocused = false;
            this.sbfocused = false;
        }
        float x = this.bounds.getX();
        float y = this.bounds.getY();
        float width = this.bounds.getWidth();
        float height = this.bounds.getHeight();
        float padding = 5.0f;
        float colorX = padding + x;
        float colorY = padding + y + 18.0f;
        float colorWidth = width - padding * 2.0f;
        float colorHeight = 48.0f;
        Font iconFont = Fonts.ICONS.getFont(6.0f);
        ctx.drawText(iconFont, "V", x + padding, y + (18.0f - iconFont.height()) / 2.0f, theme.getColor().mulAlpha(alphas));
        ctx.drawText(iconFont, "M", x + width - padding - iconFont.width("M"), y + (18.0f - iconFont.height()) / 2.0f, theme.getWhiteGray().mulAlpha(alphas));
        Font font = Fonts.MEDIUM.getFont(7.0f);
        ctx.drawText(font, this.setting.getDisplayName(), x + 8.0f + 8.0f, y + (18.0f - font.height()) / 2.0f, theme.getWhite().mulAlpha(alphas));
        this.bounds.setWidth(Math.max(96.0f, font.width(this.setting.getDisplayName()) + 30.0f));
        this.bounds.setHeight(18.0f + padding + colorHeight + padding + 6.0f + padding + 6.0f + padding + 18.0f + padding);
        float spos = colorX + colorWidth - (colorWidth - colorWidth * this.saturation);
        float bpos = colorY + (colorHeight - colorHeight * this.brightness);
        float hpos = colorWidth * this.hue;
        float apos = colorWidth * (float)this.alpha / 255.0f;
        ColorRGBA colorA = new ColorRGBA(Color.getHSBColor(this.hue, 0.0f, 1.0f)).mulAlpha(alphas);
        ColorRGBA colorB = new ColorRGBA(Color.getHSBColor(this.hue, 1.0f, 1.0f)).mulAlpha(alphas);
        ColorRGBA colorC = new ColorRGBA(new Color(0, 0, 0, 0));
        ColorRGBA colorD = new ColorRGBA(new Color(0, 0, 0));
        ctx.drawRoundedRect(colorX, colorY, colorWidth, colorHeight, BorderRadius.all(4.0f), Gradient.of(colorA, colorA, colorB, colorB));
        ctx.drawRoundedRect(colorX, colorY, colorWidth, colorHeight, BorderRadius.all(4.0f), Gradient.of(colorC, colorD, colorC, colorD));
        ctx.drawRoundedBorder(colorX, colorY, colorWidth, colorHeight, 0.1f, BorderRadius.all(4.0f), ColorRGBA.WHITE.mulAlpha(alphas));
        ctx.drawRoundedRect(spos - 2.0f, bpos - 2.0f, 6.0f, 6.0f, BorderRadius.all(2.0f), ColorRGBA.WHITE.mulAlpha(alphas));
        BorderRadius round = BorderRadius.all(1.0f);
        DrawUtil.drawRoundedTexture(ctx.getMatrices(), VurstVisual.id("icons/sliderhue.png"), colorX, colorY + colorHeight + padding, colorWidth, 4.0f, round, ColorRGBA.WHITE.mulAlpha(alphas));
        ctx.drawRoundedRect(colorX + hpos - 2.0f, colorY + colorHeight + padding - 1.0f, 6.0f, 6.0f, BorderRadius.all(2.0f), ColorRGBA.WHITE.mulAlpha(alphas));
        DrawUtil.drawRoundedTexture(ctx.getMatrices(), VurstVisual.id("icons/slidertransparent.png"), colorX, colorY + colorHeight + padding + 6.0f + padding, colorWidth, 4.0f, round, ColorRGBA.WHITE.mulAlpha(alphas));
        ColorRGBA fullAlpha = this.setting.getColor().withAlpha(255).mulAlpha(alphas);
        ctx.drawRoundedRect(colorX, colorY + colorHeight + padding + 6.0f + padding, colorWidth, 4.0f, round, Gradient.of(ColorRGBA.TRANSPARENT, ColorRGBA.TRANSPARENT, fullAlpha, fullAlpha));
        ctx.drawRoundedRect(colorX + apos - 2.0f, colorY + colorHeight + 6.0f + padding + padding - 1.0f, 6.0f, 6.0f, BorderRadius.all(2.0f), ColorRGBA.WHITE.mulAlpha(alphas));
        ctx.drawRoundedRect(colorX, colorY + colorHeight + padding + 6.0f + padding + 6.0f + padding, colorWidth, 14.0f, round, theme.getForegroundLight().mulAlpha(alphas));
        ctx.pushMatrix();
        ctx.drawText(font, "#", colorX + padding, colorY + colorHeight + padding + 6.0f + padding + 6.0f + padding + 4.0f, theme.getGray());
        this.colorString.render(ctx, colorX + padding + font.width("#") + 1.0f, colorY + colorHeight + padding + 6.0f + padding + 6.0f + padding + 4.5f, theme.getWhite().mulAlpha(alphas), theme.getGray().mulAlpha(alphas));
        this.colorString.setWidth(colorWidth - 20.0f);
        ctx.popMatrix();
        Color value = Color.getHSBColor(this.hue, this.saturation, this.brightness);
        if (this.sbfocused) {
            this.saturation = MathHelper.clamp((float)(mouseX - colorX), (float)0.0f, (float)colorWidth) / colorWidth;
            this.brightness = (colorHeight - MathHelper.clamp((float)(mouseY - colorY), (float)0.0f, (float)colorHeight)) / colorHeight;
            this.saturation = MathHelper.clamp((float)this.saturation, (float)0.0f, (float)1.0f);
            this.brightness = MathHelper.clamp((float)this.brightness, (float)0.0f, (float)1.0f);
            value = Color.getHSBColor(this.hue, this.saturation, this.brightness);
            this.setColor(new ColorRGBA(value.getRed(), value.getGreen(), value.getBlue(), this.alpha));
        }
        if (this.hfocused) {
            this.hue = MathHelper.clamp((float)(mouseX - colorX), (float)0.0f, (float)colorWidth) / colorWidth;
            this.hue = MathHelper.clamp((float)this.hue, (float)0.0f, (float)1.0f);
            value = Color.getHSBColor(this.hue, this.saturation, this.brightness);
            this.setColor(new ColorRGBA(value.getRed(), value.getGreen(), value.getBlue(), this.alpha));
        }
        if (this.afocused) {
            this.alpha = (int)(MathHelper.clamp((float)((mouseX - x) / colorWidth), (float)0.0f, (float)1.0f) * 255.0f);
            this.setColor(new ColorRGBA(value.getRed(), value.getGreen(), value.getBlue(), this.alpha));
        }
        if (this.colorString.isSelected()) {
            this.setColor(ColorUtil.hexToRgb(this.colorString.getText(), this.setting.getColor()));
            this.updatePos();
        } else {
            this.colorString.setText(ColorUtil.colorToHex(this.setting.getColor()));
            this.colorString.setCursor(6);
        }
        ctx.popMatrix();
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        this.colorString.onMouseClicked(mouseX, mouseY, button);
        float x = this.bounds.getX();
        float y = this.bounds.getY();
        float width = this.bounds.getWidth();
        float height = this.bounds.getHeight();
        float padding = 5.0f;
        float colorX = padding + x;
        float colorY = padding + y + 18.0f;
        float colorWidth = width - padding * 2.0f;
        float colorHeight = 48.0f;
        if (MathUtil.isHovered(mouseX, mouseY, this.colorString.getPosition().getX(), this.colorString.getPosition().getY(), colorWidth, 14.0)) {
            this.colorString.setSelected(true);
            return true;
        }
        if (MathUtil.isHovered(mouseX, mouseY, colorX, colorY, colorWidth, colorHeight)) {
            if (!this.hfocused && !this.afocused) {
                this.sbfocused = true;
            }
            return true;
        }
        if (MathUtil.isHovered(mouseX, mouseY, colorX, colorY + colorHeight + padding, colorWidth, 6.0)) {
            if (!this.sbfocused && !this.afocused) {
                this.hfocused = true;
            }
            return true;
        }
        if (MathUtil.isHovered(mouseX, mouseY, colorX, colorY + colorHeight + padding + 6.0f + padding, width, 6.0)) {
            if (!this.hfocused && !this.sbfocused) {
                this.afocused = true;
            }
            return true;
        }
        Font iconFont = Fonts.ICONS.getFont(6.0f);
        if (MathUtil.isHovered(mouseX, mouseY, x + width - padding - iconFont.width("M"), y + (18.0f - iconFont.height()) / 2.0f, iconFont.width("M"), 4.0)) {
            this.animationScale.update(0.0f);
            return true;
        }
        return this.bounds.contains(mouseX, mouseY);
    }

    @Override
    public float getWidth() {
        return 0.0f;
    }

    @Override
    public float getHeight() {
        return 0.0f;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    private void updatePos() {
        float[] hsb = Color.RGBtoHSB(this.setting.getColor().getRed(), this.setting.getColor().getGreen(), this.setting.getColor().getBlue(), null);
        this.hue = hsb[0];
        this.saturation = hsb[1];
        this.brightness = hsb[2];
        this.alpha = this.setting.getColor().getAlpha();
    }

    private void setColor(ColorRGBA color) {
        this.setting.setColor(color);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof MenuColorPopupSetting) {
            MenuColorPopupSetting that = (MenuColorPopupSetting)o;
            return this.setting == that.setting;
        }
        return false;
    }

    public int hashCode() {
        return Objects.hashCode(this.setting);
    }

    @Override
    public void onMouseReleased(double mouseX, double mouseY, MouseButton button) {
        this.hfocused = false;
        this.sbfocused = false;
        this.afocused = false;
    }
}

