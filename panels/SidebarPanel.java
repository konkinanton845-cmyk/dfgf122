
package vurst.visual.client.screens.menu.panels;

import by.saskkeee.user.UserInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lombok.Generated;
import vurst.visual.VurstVisual;
import vurst.visual.base.animations.base.Animation;
import vurst.visual.base.animations.base.Easing;
import vurst.visual.base.font.Font;
import vurst.visual.base.font.Fonts;
import vurst.visual.base.theme.Theme;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.screens.menu.panels.SideBarCategory;
import vurst.visual.utility.math.MathUtil;
import vurst.visual.utility.render.display.base.BorderRadius;
import vurst.visual.utility.render.display.base.GuiUtil;
import vurst.visual.utility.render.display.base.Rect;
import vurst.visual.utility.render.display.base.UIContext;
import vurst.visual.utility.render.display.base.color.ColorRGBA;
import vurst.visual.utility.render.display.shader.DrawUtil;

public class SidebarPanel {
    private final Map<Category, Rect> categoryBounds = new HashMap<Category, Rect>();
    private Rect sidebarToggleButtonBounds;
    private Rect animRect = new Rect(0.0f, 0.0f, 0.0f, 0.0f);
    private Animation animationChange = new Animation(200L, 1.0f, Easing.LINEAR);
    private final Animation sidebarAnimation;
    private final boolean isSidebarExpanded;
    private final Consumer<Category> onCategorySelect;
    private final Runnable onSidebarToggle;
    private final List<SideBarCategory> categories = new ArrayList<SideBarCategory>();

    public SidebarPanel(Animation sidebarAnimation, boolean isSidebarExpanded, Consumer<Category> onCategorySelect, Runnable onSidebarToggle) {
        this.sidebarAnimation = sidebarAnimation;
        this.isSidebarExpanded = isSidebarExpanded;
        this.onCategorySelect = onCategorySelect;
        this.onSidebarToggle = onSidebarToggle;
        this.categories.addAll(Arrays.stream(Category.values()).map(SideBarCategory::new).toList());
    }

    public void render(UIContext ctx, float boxX, float boxY, float height, float progress, Theme theme, Category selectedCategory, ColorRGBA primary, ColorRGBA textColor, ColorRGBA selectedColor) {
        float iconX;
        float categoryY;
        float sidebarProgress = this.sidebarAnimation.update();
        float collapsedSidebarWidth = 30.0f;
        float expandedSidebarWidth = 88.0f;
        float sidebarWidth = collapsedSidebarWidth + (expandedSidebarWidth - collapsedSidebarWidth) * sidebarProgress;
        float sidebarPadding = 8.0f;
        float sidebarX = boxX + sidebarPadding;
        float sidebarY = boxY + sidebarPadding;
        float sidebarHeight = height - sidebarPadding * 2.0f;
        ColorRGBA sideBar = theme.getForegroundColor().mulAlpha(progress);
        this.categoryBounds.clear();
        ctx.drawRoundedRect(sidebarX, sidebarY, sidebarWidth, sidebarHeight, BorderRadius.all(7.0f), sideBar);
        DrawUtil.drawRoundedBorder(ctx.getMatrices(), sidebarX, sidebarY, sidebarWidth, sidebarHeight, -0.1f, BorderRadius.all(7.0f), theme.getForegroundStroke().mulAlpha(progress));
        float logoSize = 14.0f;
        float logoX = sidebarX + (collapsedSidebarWidth - logoSize) / 2.0f;
        float logoY = sidebarY + 8.0f;
        Font logoGlyphFont = Fonts.BOLD.getFont(10.0f);
        String logoText = "V";
        float logoTextX = logoX + (logoSize - logoGlyphFont.width(logoText)) / 2.0f;
        float logoTextY = logoY + (logoSize - logoGlyphFont.height()) / 2.0f;
        ctx.drawText(logoGlyphFont, logoText, logoTextX, logoTextY, VurstVisual.getInstance().getThemeManager().getColorCycleIcon().toGradient().mulAlpha(progress));
        ctx.pushMatrix();
        ctx.enableScissor((int)sidebarX, (int)sidebarY, (int)(sidebarX + sidebarWidth), (int)(sidebarY + sidebarHeight));
        float textAlpha = Math.min(1.0f, sidebarProgress * 2.0f);
        textColor = textColor.mulAlpha(textAlpha);
        ColorRGBA textColorDisable = theme.getGrayLight().mulAlpha(progress * textAlpha);
        ColorRGBA iconColorDisable = theme.getGray().mulAlpha(progress);
        Font logoFont = Fonts.MEDIUM.getFont(7.0f);
        String clientName = "Vurst Visual";
        ctx.drawText(logoFont, clientName, logoX + logoSize + 8.0f, logoY + (logoSize - logoFont.height()) / 2.0f + 1.0f, textColor);
        float expandedIconSize = 10.0f;
        float collapsedIconSize = 7.0f;
        float iconSize = 10.0f + -3.0f * sidebarProgress;
        float padding = 10.5f;
        float startY = sidebarY + 35.0f;
        int index = 0;
        for (SideBarCategory sideBarCategory : this.categories) {
            if (selectedCategory == sideBarCategory.getCategory()) {
                categoryY = startY + (float)index * (iconSize + padding);
                iconX = sidebarX + (collapsedSidebarWidth - iconSize) / 2.0f;
                this.animRect = new Rect(MathUtil.interpolate(this.animRect.x(), sidebarX + 4.0f, this.animationChange.getValue()), MathUtil.interpolate(this.animRect.y(), categoryY, this.animationChange.getValue()), sidebarWidth - 8.0f, iconSize + 11.0f);
                sideBarCategory.render(ctx, this.animRect.x(), this.animRect.y(), sidebarWidth - 8.0f, iconSize + 11.0f, sidebarProgress, selectedCategory == sideBarCategory.getCategory(), textColor, textColorDisable, iconColorDisable, primary);
                ctx.drawRoundedRect(this.animRect.x(), this.animRect.y(), sidebarWidth - 8.0f, iconSize + 11.0f, BorderRadius.all(4.0f), theme.getForegroundLight().mulAlpha(progress));
                DrawUtil.drawRoundedBorder(ctx.getMatrices(), this.animRect.x(), this.animRect.y(), sidebarWidth - 8.0f, iconSize + 11.0f, -0.1f, BorderRadius.all(4.0f), theme.getForegroundLightStroke().mulAlpha(progress));
                break;
            }
            ++index;
        }
        this.animationChange.animateTo(1.0f);
        this.animationChange.update();
        index = 0;
        for (SideBarCategory sideBarCategory : this.categories) {
            categoryY = startY + (float)index * (iconSize + padding);
            iconX = sidebarX + (collapsedSidebarWidth - iconSize) / 2.0f;
            sideBarCategory.render(ctx, sidebarX + 4.0f, categoryY, sidebarWidth - 8.0f, iconSize + 11.0f, sidebarProgress, selectedCategory == sideBarCategory.getCategory(), textColor, textColorDisable, iconColorDisable, primary);
            this.categoryBounds.put(sideBarCategory.getCategory(), new Rect(sidebarX + 4.0f, categoryY, sidebarWidth - 8.0f, iconSize + 11.0f));
            ++index;
        }
        float avatarSize = 18.0f;
        float avatarX = sidebarX + (collapsedSidebarWidth - avatarSize) / 2.0f;
        float avatarY = sidebarY + sidebarHeight - avatarSize - 8.0f;
        float toggleX = avatarX + 5.0f;
        float toggleY = avatarY - 19.0f;
        float toggleW = 8.0f;
        float toggleH = 8.0f;
        Font iconFont = Fonts.ICONS.getFont(7.0f);
        ctx.drawText(iconFont, "6", toggleX, toggleY, theme.getGray().mulAlpha(progress));
        this.sidebarToggleButtonBounds = new Rect(toggleX, toggleY, toggleW, toggleH);
        boolean hover = GuiUtil.isHovered(avatarX, avatarY, avatarSize, avatarSize, ctx);
        DrawUtil.drawRoundedTexture(ctx.getMatrices(), VurstVisual.id("icons/avatar.png"), avatarX, avatarY, avatarSize, avatarSize, BorderRadius.all(4.0f), ColorRGBA.WHITE.mulAlpha(progress));
        DrawUtil.drawRoundedBorder(ctx.getMatrices(), avatarX, avatarY, avatarSize, avatarSize, -0.1f, BorderRadius.all(3.0f), new ColorRGBA(181, 162, 255, hover ? 200 : 190).mulAlpha(progress));
        String playerName = UserInfo.getUsername();
        Font nameFont = Fonts.MEDIUM.getFont(6.0f);
        ctx.drawText(nameFont, playerName, avatarX + avatarSize + 8.0f, avatarY + (avatarSize - nameFont.height()) / 2.0f, textColor);
        ctx.disableScissor();
        ctx.popMatrix();
    }

    public boolean handleMouseClicked(double mouseX, double mouseY) {
        if (this.sidebarToggleButtonBounds != null && this.sidebarToggleButtonBounds.contains(mouseX, mouseY)) {
            this.onSidebarToggle.run();
            return true;
        }
        for (Map.Entry<Category, Rect> entry : this.categoryBounds.entrySet()) {
            if (!entry.getValue().contains(mouseX, mouseY)) continue;
            this.animationChange.animateTo(0.0f);
            this.animationChange.setValue(0.0f);
            this.onCategorySelect.accept(entry.getKey());
            return true;
        }
        return false;
    }

    @Generated
    public Map<Category, Rect> getCategoryBounds() {
        return this.categoryBounds;
    }

    @Generated
    public Rect getSidebarToggleButtonBounds() {
        return this.sidebarToggleButtonBounds;
    }
}

