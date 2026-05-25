
package vurst.visual.client.screens.menu.panels;

import net.minecraft.MathHelper;
import vurst.visual.VurstVisual;
import vurst.visual.base.animations.base.Animation;
import vurst.visual.base.animations.base.Easing;
import vurst.visual.base.font.Font;
import vurst.visual.base.font.Fonts;
import vurst.visual.base.theme.Theme;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.Module;
import vurst.visual.utility.render.display.TextBox;
import vurst.visual.utility.render.display.base.BorderRadius;
import vurst.visual.utility.render.display.base.CustomSprite;
import vurst.visual.utility.render.display.base.Rect;
import vurst.visual.utility.render.display.base.UIContext;
import vurst.visual.utility.render.display.base.color.ColorRGBA;
import vurst.visual.utility.render.display.shader.DrawUtil;

public class HeaderPanel {
    public Rect themeButtonBounds;
    public Rect searchBarBounds;
    public Rect layoutToggleButtonBounds;
    private final TextBox searchField;
    private final Runnable onLayoutToggle;
    private final Runnable onThemeSwitch;
    private Category lastCategory = Category.MOVEMENT;
    Animation animation = new Animation(300L, 1.0f, Easing.QUAD_IN_OUT);

    public HeaderPanel(TextBox searchField, Runnable onLayoutToggle, Runnable onThemeSwitch) {
        this.searchField = searchField;
        this.onLayoutToggle = onLayoutToggle;
        this.onThemeSwitch = onThemeSwitch;
    }

    public void render(UIContext ctx, float contentStartX, float sidebarY, float boxX, int columns, float boxWidth, float progress, Theme theme, Category selectedCategory) {
        this.animation.update(1.0f);
        ColorRGBA sideBar = theme.getForegroundColor().mulAlpha(progress);
        ColorRGBA textColor = theme.getWhite().mulAlpha(progress);
        float x = contentStartX;
        x = this.renderBreadcrumbs(ctx, x, sidebarY, progress, theme, selectedCategory, textColor);
        x += 8.0f;
        x = this.renderStats(ctx, x, sidebarY, progress, theme, selectedCategory, textColor);
        x += 8.0f;
        x = this.renderThemeButton(ctx, x, sidebarY, progress, theme);
        this.renderLayoutButton(ctx, x += 8.0f, sidebarY, progress, theme, columns);
        this.renderSearchBar(ctx, boxX, sidebarY, boxWidth, progress, theme);
    }

    private float renderBreadcrumbs(UIContext ctx, float startX, float y, float progress, Theme theme, Category selectedCategory, ColorRGBA textColor) {
        String name = selectedCategory.getDisplayName();
        Font font = Fonts.MEDIUM.getFont(7.0f);
        Font icon7 = Fonts.ICONS.getFont(7.0f);
        Font icon6 = Fonts.ICONS.getFont(5.0f);
        float homeIcon = 7.0f;
        float arrowIcon = 6.0f;
        float catIcon = 7.0f;
        float pad = 8.0f;
        float gap = 4.0f;
        float tgap = 2.0f;
        float textW = MathHelper.lerp((float)this.animation.getValue(), (float)font.width(this.lastCategory.getDisplayName()), (float)font.width(name));
        float width = pad * 2.0f + homeIcon + gap + arrowIcon + catIcon + tgap + textW;
        float h = 22.0f;
        ColorRGBA bar = theme.getForegroundColor().mulAlpha(progress);
        ctx.drawRoundedRect(startX, y, width, h, BorderRadius.all(7.0f), bar);
        DrawUtil.drawRoundedBorder(ctx.getMatrices(), startX, y, width, h, -0.1f, BorderRadius.all(7.0f), theme.getForegroundStroke().mulAlpha(progress));
        float cx = startX + pad;
        float vy = y + h / 2.0f;
        ctx.drawText(icon7, "7", cx, vy - icon7.height() / 2.0f - 0.5f, theme.getColor().mulAlpha(progress));
        ctx.drawText(icon6, "A", (cx += icon7.width("7") + gap) + 1.0f, vy - icon6.height() / 2.0f - 0.3f, theme.getForegroundGray().mulAlpha(progress));
        ctx.enableScissor((int)startX + 20, (int)y, (int)(startX + width), (int)(y + h));
        float offset = (1.0f - this.animation.getValue()) * font.width(name) * 2.0f;
        float offset2 = this.animation.getValue() * font.width(this.lastCategory.getDisplayName());
        ctx.drawText(font, name, (cx += icon6.width("A") + gap) + offset, vy - font.height() / 2.0f, textColor.mulAlpha(this.animation.getValue()));
        ctx.drawText(font, this.lastCategory.getDisplayName(), cx - offset2, vy - font.height() / 2.0f, textColor.mulAlpha(1.0f - this.animation.getValue()));
        ctx.disableScissor();
        return startX + width;
    }

    private float renderStats(UIContext ctx, float startX, float y, float progress, Theme theme, Category cat, ColorRGBA textColor) {
        int enabled = 0;
        int total = 0;
        for (Module m : VurstVisual.getInstance().getModuleManager().getModules()) {
            if (m.getName().equalsIgnoreCase("Hud") || m.getCategory() != cat) continue;
            ++total;
            if (!m.isEnabled()) continue;
            ++enabled;
        }
        Font font = Fonts.MEDIUM.getFont(7.0f);
        Font iconFont = Fonts.ICONS.getFont(7.0f);
        float w = 8.0f + iconFont.width(cat.getIcon()) + 4.0f + font.width(String.valueOf(enabled)) + 1.0f + 8.0f + 1.0f + iconFont.width(cat.getIcon()) + 4.0f + font.width(String.valueOf(total)) + 8.0f;
        float h = 22.0f;
        ColorRGBA bar = theme.getForegroundColor().mulAlpha(progress);
        ctx.drawRoundedRect(startX, y, w, h, BorderRadius.all(7.0f), bar);
        DrawUtil.drawRoundedBorder(ctx.getMatrices(), startX, y, w, h, -0.1f, BorderRadius.all(7.0f), theme.getForegroundStroke().mulAlpha(progress));
        float iconSz = 7.0f;
        float cx = startX + 8.0f;
        float ty = y + (h - font.height()) / 2.0f;
        float iy = y + (h - iconFont.height()) / 2.0f - 0.5f;
        ctx.drawText(iconFont, cat.getIcon(), cx + (float)(cat.getIcon().equals("2") ? 1 : 0), iy, theme.getColor().mulAlpha(progress));
        ctx.drawText(font, String.valueOf(enabled), cx += iconFont.width(cat.getIcon()) + 4.0f, ty, textColor);
        cx += font.width(String.valueOf(enabled));
        ctx.drawSprite(new CustomSprite("icons/separator.png"), cx += 1.0f, iy - 1.0f, 8.0f, 8.0f, ColorRGBA.WHITE.mulAlpha(progress));
        ctx.drawText(iconFont, cat.getIcon(), cx += 9.0f, iy, theme.getColor().mulAlpha(progress));
        ctx.drawText(font, String.valueOf(total), cx += iconFont.width(cat.getIcon()) + 4.0f, ty, textColor);
        return startX + w;
    }

    private float renderThemeButton(UIContext ctx, float startX, float y, float progress, Theme theme) {
        float size = 22.0f;
        this.themeButtonBounds = new Rect(startX, y, size, size);
        this.drawIconButton(ctx, startX, y, size, theme.getIcon(), progress, theme);
        return startX + size;
    }

    private float renderLayoutButton(UIContext ctx, float startX, float y, float progress, Theme theme, int cols) {
        float size = 22.0f;
        this.layoutToggleButtonBounds = new Rect(startX, y, size, size);
        String icon = cols == 2 ? ":" : (cols == 3 ? ";" : "9");
        this.drawIconButton(ctx, startX, y, size, icon, progress, theme);
        return startX + size;
    }

    private void drawIconButton(UIContext ctx, float x, float y, float s, String icon, float progress, Theme theme) {
        ColorRGBA bar = theme.getForegroundColor().mulAlpha(progress);
        ctx.drawRoundedRect(x, y, s, s, BorderRadius.all(6.0f), bar);
        DrawUtil.drawRoundedBorder(ctx.getMatrices(), x, y, s, s, -0.1f, BorderRadius.all(6.0f), theme.getForegroundStroke().mulAlpha(progress));
        Font iconF = Fonts.ICONS.getFont(7.0f);
        float ix = x + (s - iconF.width(icon)) / 2.0f;
        float iy = y + (s - iconF.height()) / 2.0f;
        ctx.drawText(iconF, icon, ix, iy, theme.getColor().mulAlpha(progress));
    }

    private void renderSearchBar(UIContext ctx, float boxX, float y, float boxWidth, float progress, Theme theme) {
        float w = 128.0f;
        float h = 22.0f;
        float pad = 8.0f;
        float x = boxX + boxWidth - pad - w;
        this.searchBarBounds = new Rect(x, y, w, h);
        ColorRGBA bar = theme.getForegroundColor().mulAlpha(progress);
        ctx.drawRoundedRect(x, y, w, h, BorderRadius.all(6.0f), bar);
        DrawUtil.drawRoundedBorder(ctx.getMatrices(), x, y, w, h, -0.1f, BorderRadius.all(6.0f), theme.getForegroundStroke().mulAlpha(progress));
        Font font = Fonts.MEDIUM.getFont(7.0f);
        String txt = this.searchField.getText();
        boolean empty = txt.isEmpty() && !this.searchField.isSelected();
        float ty = y + (h - font.height()) / 2.0f;
        if (empty) {
            ctx.drawText(font, "Поиск", x + 8.0f, ty, theme.getWhite().mulAlpha(progress * 0.5f));
        } else {
            ctx.enableScissor((int)x + 8, (int)ty - 10, (int)Math.ceil(x + 8.0f + 128.0f), (int)Math.ceil(ty) + 10);
            this.searchField.render(ctx, x + 8.0f, ty, theme.getWhite().mulAlpha(progress), theme.getWhite().mulAlpha(progress * 0.5f));
            ctx.disableScissor();
        }
    }

    public void resetAnim(Category last, Category next) {
        this.animation.reset(0.0f);
        this.lastCategory = last;
    }

    public boolean handleMouseClicked(double mouseX, double mouseY) {
        if (this.layoutToggleButtonBounds != null && this.layoutToggleButtonBounds.contains(mouseX, mouseY)) {
            this.onLayoutToggle.run();
            return true;
        }
        if (this.themeButtonBounds != null && this.themeButtonBounds.contains(mouseX, mouseY)) {
            this.onThemeSwitch.run();
            return true;
        }
        return this.searchBarBounds != null && this.searchBarBounds.contains(mouseX, mouseY);
    }
}

