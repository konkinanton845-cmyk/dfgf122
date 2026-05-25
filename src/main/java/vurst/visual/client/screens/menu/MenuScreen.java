package vurst.visual.client.screens.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Generated;
import net.minecraft.DrawContext;
import net.minecraft.MathHelper;
import net.minecraft.MatrixStack;
import net.minecraft.Vector2f;
import vurst.visual.VurstVisual;
import vurst.visual.base.animations.base.Animation;
import vurst.visual.base.animations.base.Easing;
import vurst.visual.base.font.Font;
import vurst.visual.base.font.Fonts;
import vurst.visual.base.theme.Theme;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.screens.menu.elements.api.AbstractMenuElement;
import vurst.visual.client.screens.menu.elements.impl.MenuModuleElement;
import vurst.visual.client.screens.menu.elements.impl.MenuThemeElement;
import vurst.visual.client.screens.menu.panels.HeaderPanel;
import vurst.visual.client.screens.menu.panels.SidebarPanel;
import vurst.visual.client.screens.menu.settings.api.MenuPopupSetting;
import vurst.visual.utility.game.other.MouseButton;
import vurst.visual.utility.game.other.render.CustomScreen;
import vurst.visual.utility.math.MathUtil;
import vurst.visual.utility.render.display.ScrollHandler;
import vurst.visual.utility.render.display.TextBox;
import vurst.visual.utility.render.display.base.BorderRadius;
import vurst.visual.utility.render.display.base.UIContext;
import vurst.visual.utility.render.display.base.color.ColorRGBA;

public class MenuScreen
extends CustomScreen {
    private static final String BRAND_PREFIX = "Exocle";
    private static final String BRAND_SUFFIX = "Visuals";
    private static final int PARTICLE_COUNT = 22;

    private Category selectedCategory = Category.MOVEMENT;
    private Category realSelectedCategory = Category.MOVEMENT;
    private float boxX;
    private float boxY;
    private int columns = 1;
    private float boxWidth = 522.0f;
    private float boxHeight = 316.0f;
    private boolean dragging;
    private float dragOffsetX;
    private float dragOffsetY;
    private final Animation sidebarAnimation = new Animation(300L, 0.0f, Easing.CUBIC_IN_OUT);
    private boolean isSidebarExpanded;
    private final Animation animationClose = new Animation(380L, 0.0f, Easing.CUBIC_IN_OUT);
    private boolean initialized;
    private TextBox searchField;
    private final ScrollHandler scrollHandler = new ScrollHandler();
    private boolean closing = false;
    private SidebarPanel sidebarPanel;
    private HeaderPanel headerPanel;
    private int scaledScissorX = 0;
    private int scaledScissorY = 0;
    private int scaledScissorEndX = 2000;
    private int scaledScissorEndY = 2000;
    private float inputScale = 1.0f;
    private float inputPivotX = 0.0f;
    private float inputPivotY = 0.0f;
    private final Animation animationColums;
    private final Animation animationScrollHeight;
    private final Animation animationChangeCategory;
    private final Animation animationIntro;
    private final Animation animationBrandPulse;
    private final Animation animationBrandShift;
    private final Animation animationScrollGlow;
    private boolean draggingScrollbar = false;
    private float scrollClickOffset = 0.0f;
    private Set<MenuPopupSetting> popupSettings = new HashSet<MenuPopupSetting>();
    List<AbstractMenuElement> modules = new ArrayList<AbstractMenuElement>();
    private String hoveredModuleDescription;
    private long openTimeMs = 0L;

    public MenuScreen() {
        this.animationColums = new Animation(300L, this.columns == 3 ? 1.0f : 0.0f, Easing.CUBIC_IN_OUT);
        this.animationChangeCategory = new Animation(180L, 1.0f, Easing.CUBIC_IN_OUT);
        this.animationScrollHeight = new Animation(150L, 1.0f, Easing.QUAD_IN_OUT);
        this.animationIntro = new Animation(520L, 0.0f, Easing.CUBIC_IN_OUT);
        this.animationBrandPulse = new Animation(1400L, 0.0f, Easing.QUAD_IN_OUT);
        this.animationBrandShift = new Animation(2600L, 0.0f, Easing.CUBIC_IN_OUT);
        this.animationScrollGlow = new Animation(220L, 0.0f, Easing.QUAD_IN_OUT);
    }

    public void initialize() {
        this.modules.addAll(VurstVisual.getInstance().getModuleManager().getModules().stream().map(MenuModuleElement::new).toList());
        this.modules.add(new MenuThemeElement(Theme.DARK));
        this.modules.add(new MenuThemeElement(Theme.LIGHT));
        this.modules.add(new MenuThemeElement(Theme.CUSTOM_THEME));
    }

    protected void init() {
        this.closing = false;
        this.openTimeMs = System.currentTimeMillis();
        this.animationColums.setValue(this.columns == 3 ? 1.0f : 0.0f);
        this.boxWidth = MathHelper.lerp((float)this.animationColums.getValue(), (int)465, (int)533);
        this.boxHeight = MathHelper.lerp((float)this.animationColums.getValue(), (int)282, (int)320);
        this.boxX = ((float)this.width - this.boxWidth) / 2.0f;
        this.boxY = ((float)this.height - this.boxHeight) / 2.0f;
        this.updateInputTransform(1.0f, this.boxX + this.boxWidth / 2.0f, this.boxY + this.boxHeight / 2.0f);
        this.animationClose.setValue(0.0f);
        this.animationClose.update(1.0f);
        this.animationIntro.setValue(0.0f);
        this.animationBrandPulse.setValue(0.0f);
        this.animationBrandShift.setValue(0.0f);
        if (!this.initialized) {
            this.searchField = new TextBox(new Vector2f(this.boxX + this.boxWidth - 128.0f - 8.0f, this.boxY + 8.0f), Fonts.MEDIUM.getFont(7.0f), "Поиск", 100.0f);
            this.sidebarPanel = new SidebarPanel(this.sidebarAnimation, this.isSidebarExpanded, category -> {
                this.headerPanel.resetAnim(this.realSelectedCategory, (Category)((Object)category));
                this.realSelectedCategory = category;
                this.scrollHandler.setTargetValue(0.0);
                this.searchField.setSelectAll(true);
                this.searchField.setSelected(true);
                this.searchField.keyPressed(259, 0, 0);
                this.searchField.setSelected(false);
            }, () -> {
                this.isSidebarExpanded = !this.isSidebarExpanded;
                this.sidebarAnimation.animateTo(this.isSidebarExpanded ? 1.0f : 0.0f);
            });
            this.headerPanel = new HeaderPanel(this.searchField, () -> {
                this.columns = this.columns % 3 + 1;
            }, () -> VurstVisual.getInstance().getThemeManager().switchTheme());
        }
        this.initialized = true;
    }

    @Override
    public void tick() {
        if (this.closing && this.animationClose.getValue() == 0.0f) {
            this.close();
        }
        super.tick();
    }

    public void removed() {
        this.closing = true;
        super.removed();
    }

    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    public boolean isFinish() {
        return this.animationClose.getValue() == 0.0f && this.closing;
    }

    public void renderTop(UIContext ctx, float mouseX, float mouseY) {
        if (!this.initialized) {
            return;
        }
        this.animationColums.update(this.columns == 3 ? 1.0f : 0.0f);
        this.boxWidth = MathHelper.lerp((float)this.animationColums.getValue(), (int)465, (int)533);
        this.boxHeight = MathHelper.lerp((float)this.animationColums.getValue(), (int)282, (int)320);
        float progress = this.animationClose.update(this.closing ? 0.0f : 1.0f);
        progress = Math.min(Math.max(progress, 0.0f), 1.0f);
        float intro = this.animationIntro.update(1.0f);
        intro = Math.min(Math.max(intro, 0.0f), 1.0f);
        // Looping brand animations (drive both ways for a continuous ping-pong)
        if (this.animationBrandPulse.getValue() >= 0.999f) this.animationBrandPulse.setValue(0.0f);
        this.animationBrandPulse.update(1.0f);
        if (this.animationBrandShift.getValue() >= 0.999f) this.animationBrandShift.setValue(0.0f);
        this.animationBrandShift.update(1.0f);
        this.animationScrollGlow.update(this.draggingScrollbar ? 1.0f : 0.0f);

        float sidebarProgress = this.sidebarAnimation.update();
        float baseScale = 0.82f + 0.18f * progress;
        // Intro: very small overshoot via easing already; add a tiny extra lift
        float introOffsetY = (1.0f - intro) * 14.0f;
        float fitScale = this.getFitScale(this.boxWidth, this.boxHeight);
        float interfaceScale = this.getInterfaceZoomScale();
        float scale = MathHelper.clamp((float)(baseScale * fitScale * interfaceScale), (float)0.5f, (float)1.15f);
        Theme theme = VurstVisual.getInstance().getThemeManager().getCurrentTheme();
        MatrixStack ms = ctx.getMatrices();
        ctx.pushMatrix();
        float scaleX = this.boxX + this.boxWidth / 2.0f;
        float scaleY = this.boxY + this.boxHeight / 2.0f;
        this.updateInputTransform(scale, scaleX, scaleY);
        float localMouseX = this.toMenuX(mouseX);
        float localMouseY = this.toMenuY(mouseY);
        ms.translate(scaleX, scaleY + introOffsetY, 1.0f);
        ms.scale(scale, scale, 1.0f);
        ms.translate(-scaleX, -scaleY, 1.0f);

        ColorRGBA primary = theme.getColor().mulAlpha(progress);
        ColorRGBA baseBg = theme.getBackgroundColor().mulAlpha(progress * 4.0f);
        ColorRGBA selectedColor = theme.getWhite().mulAlpha(progress);
        ColorRGBA textColor = theme.getWhite().mulAlpha(progress);

        // ----- Ambient floating particles drifting behind the panel -----
        this.renderAmbientParticles(ctx, theme, progress, intro);

        // ----- Outer accent glow rings behind the window (subtle bloom) -----
        this.renderWindowGlow(ctx, theme, progress * intro);

        // ----- Main panel body -----
        ctx.drawRoundedRect(this.boxX, this.boxY, this.boxWidth, this.boxHeight, BorderRadius.all(11.0f), baseBg);
        // Soft inner stroke to give the panel an edge
        ctx.drawRoundedRect(this.boxX, this.boxY, this.boxWidth, 1.0f, BorderRadius.all(11.0f), theme.getWhite().mulAlpha(0.06f * progress));
        ctx.drawRoundedRect(this.boxX, this.boxY + this.boxHeight - 1.0f, this.boxWidth, 1.0f, BorderRadius.all(11.0f), theme.getWhite().mulAlpha(0.04f * progress));

        // ----- Brand title "Exocle Visuals" -----
        this.renderBrand(ctx, theme, progress, intro);

        float widthScroll = 2.0f;
        this.sidebarPanel.render(ctx, this.boxX, this.boxY, this.boxHeight, progress, theme, this.realSelectedCategory, primary, textColor, selectedColor);
        float sidebarWidth = 30.0f + 58.0f * sidebarProgress;
        float contentStartX = this.boxX + 8.0f + sidebarWidth + 8.0f;
        float sidebarY = this.boxY + 8.0f;
        float contentY = this.boxY + 22.0f + 8.0f + 8.0f;
        this.headerPanel.render(ctx, contentStartX, sidebarY, this.boxX, this.columns, this.boxWidth, progress, theme, this.realSelectedCategory);

        // Animated accent rail under the header
        this.renderHeaderRail(ctx, theme, contentStartX, contentY - 6.0f, this.boxX + this.boxWidth - contentStartX - 8.0f, progress);

        float visibleHeight = this.boxHeight - 46.0f;
        float scrollProgress = this.scrollHandler.getMax() == 0.0 ? 0.0f : (float)(this.scrollHandler.getValue() / this.scrollHandler.getMax());
        float scrollHeight = Math.max(visibleHeight * (visibleHeight / (float)((double)visibleHeight + this.scrollHandler.getMax())), 20.0f);
        scrollHeight = Math.min(visibleHeight, this.animationScrollHeight.update(scrollHeight));
        float denom = Math.max(1.0f, visibleHeight - scrollHeight);
        float scrollY = contentY + denom * scrollProgress;
        scrollY = Math.min(contentY + visibleHeight, scrollY);
        // Scrollbar track
        ctx.drawRoundedRect(this.boxX + this.boxWidth - 8.0f - widthScroll, contentY, widthScroll, visibleHeight, BorderRadius.all(1.0f), theme.getForegroundColor().mulAlpha(progress * 0.85f));
        // Glow halo behind active thumb
        float scrollGlowAlpha = this.animationScrollGlow.getValue() * 0.45f * progress;
        if (scrollGlowAlpha > 0.001f) {
            ctx.drawRoundedRect(this.boxX + this.boxWidth - 8.0f - widthScroll - 1.5f, scrollY - 1.5f, widthScroll + 3.0f, scrollHeight + 3.0f, BorderRadius.all(2.0f), theme.getColor().mulAlpha(scrollGlowAlpha));
        }
        if (scrollY + scrollHeight > visibleHeight + contentY) {
            ctx.drawRoundedRect(this.boxX + this.boxWidth - 8.0f - widthScroll, contentY, widthScroll, visibleHeight, BorderRadius.all(1.0f), theme.getForegroundStroke().mulAlpha(progress));
        } else {
            ctx.drawRoundedRect(this.boxX + this.boxWidth - 8.0f - widthScroll, scrollY, widthScroll, scrollHeight, BorderRadius.all(1.0f), theme.getForegroundStroke().mulAlpha(progress));
        }

        float contentWidth = this.boxX + (float)(this.columns == 3 ? 530 : 461) - contentStartX - 8.0f;
        this.scaledScissorX = (int)contentStartX;
        this.scaledScissorY = (int)((float)((int)this.boxY) + 38.0f);
        this.scaledScissorEndX = (int)(this.boxX + this.boxWidth);
        this.scaledScissorEndY = (int)((float)((int)this.boxY) + this.boxHeight);
        ctx.enableScissor(this.scaledScissorX, this.scaledScissorY, this.scaledScissorEndX, this.scaledScissorEndY);
        this.animationChangeCategory.setEasing(Easing.QUAD_IN_OUT);
        float categoryAlpha = progress * intro * this.animationChangeCategory.update(this.selectedCategory == this.realSelectedCategory ? 1.0f : 0.0f);
        this.renderModules(ctx, localMouseX, localMouseY, categoryAlpha, (int)contentStartX, contentWidth, (int)contentY);
        ctx.disableScissor();
        this.renderHoveredDescription(ctx, progress);

        ArrayList<MenuPopupSetting> removes = new ArrayList<MenuPopupSetting>();
        for (MenuPopupSetting setting : this.popupSettings) {
            setting.render(ctx, localMouseX, localMouseY, progress, theme);
            if (setting.getAnimationScale().getValue() != 0.0f) continue;
            removes.add(setting);
        }
        this.popupSettings.removeAll(removes);
        if (this.animationChangeCategory.getValue() == 0.0f) {
            this.selectedCategory = this.realSelectedCategory;
        }
        if (this.draggingScrollbar) {
            float scrollbarY = this.boxY + 22.0f + 8.0f + 8.0f;
            float newY = localMouseY - scrollbarY - this.scrollClickOffset;
            float scrollRatio = newY / denom;
            this.scrollHandler.setTargetValue(-((double)scrollRatio * this.scrollHandler.getMax()));
        }
        ctx.popMatrix();
    }

    // ---------- Decorative renderers ----------

    private void renderWindowGlow(UIContext ctx, Theme theme, float alpha) {
        if (alpha <= 0.001f) return;
        // Stacked halos behind the panel for a soft bloom
        ColorRGBA accent = theme.getColor();
        float t = this.animationBrandPulse.getValue();
        // ping-pong 0..1..0
        float pulse = t < 0.5f ? t * 2.0f : (1.0f - t) * 2.0f;
        for (int i = 6; i >= 1; --i) {
            float spread = i * 4.0f + pulse * 3.0f;
            float a = (0.04f + 0.012f * pulse) * (1.0f - (i - 1) * 0.14f) * alpha;
            ctx.drawRoundedRect(this.boxX - spread, this.boxY - spread,
                    this.boxWidth + spread * 2.0f, this.boxHeight + spread * 2.0f,
                    BorderRadius.all(11.0f + spread * 0.5f),
                    accent.mulAlpha(a));
        }
    }

    private void renderAmbientParticles(UIContext ctx, Theme theme, float alpha, float intro) {
        if (alpha <= 0.001f) return;
        long now = System.currentTimeMillis() - this.openTimeMs;
        ColorRGBA tint = theme.getColor();
        ColorRGBA soft = theme.getWhite();
        float w = this.boxWidth + 80.0f;
        float h = this.boxHeight + 80.0f;
        float ox = this.boxX - 40.0f;
        float oy = this.boxY - 40.0f;
        for (int i = 0; i < PARTICLE_COUNT; ++i) {
            float seed = (i + 1) * 0.6180339f;
            float baseX = ((seed * 113.0f) % 1.0f) * w;
            float baseY = ((seed * 271.0f) % 1.0f) * h;
            float speed = 0.018f + ((seed * 17.0f) % 1.0f) * 0.025f;
            float driftX = (float)Math.sin((now * 0.001f) * speed * 2.4f + i) * 14.0f;
            float driftY = (float)Math.cos((now * 0.001f) * speed * 1.9f + i * 0.7f) * 10.0f;
            float px = ox + ((baseX + driftX) % w + w) % w;
            float py = oy + ((baseY + driftY) % h + h) % h;
            float sz = 1.2f + ((seed * 41.0f) % 1.0f) * 1.8f;
            float twinkle = 0.35f + 0.65f * (float)(0.5 + 0.5 * Math.sin((now * 0.002f) + i * 1.3f));
            ColorRGBA c = (i % 3 == 0 ? tint : soft).mulAlpha(0.05f * twinkle * alpha * intro);
            ctx.drawRoundedRect(px, py, sz, sz, BorderRadius.all(sz * 0.5f), c);
        }
    }

    private void renderBrand(UIContext ctx, Theme theme, float alpha, float intro) {
        if (alpha <= 0.001f) return;
        Font heavy = Fonts.MEDIUM.getFont(9.5f);
        Font light = Fonts.MEDIUM.getFont(9.5f);
        Font tiny = Fonts.MEDIUM.getFont(5.5f);

        float prefixW = heavy.width(BRAND_PREFIX);
        float spacer = 4.0f;
        float suffixW = light.width(BRAND_SUFFIX);
        float totalW = prefixW + spacer + suffixW;

        // Float the brand strip just above the panel
        float stripY = this.boxY - 18.0f;
        float stripCenterX = this.boxX + this.boxWidth / 2.0f;
        float introLift = (1.0f - intro) * 8.0f;
        float baseY = stripY + introLift;

        // Brand backdrop pill (subtle)
        float padX = 10.0f;
        float pillW = totalW + padX * 2.0f;
        float pillH = 14.0f;
        float pillX = stripCenterX - pillW / 2.0f;
        float pillY = baseY - 2.0f;
        ctx.drawRoundedRect(pillX, pillY, pillW, pillH, BorderRadius.all(pillH / 2.0f),
                theme.getBackgroundColor().mulAlpha(0.6f * alpha));
        ctx.drawRoundedRect(pillX, pillY, pillW, 1.0f, BorderRadius.all(pillH / 2.0f),
                theme.getWhite().mulAlpha(0.07f * alpha));

        // Animated accent dot that travels across the brand pill
        float shift = this.animationBrandShift.getValue();
        float dotTravel = shift < 0.5f ? shift * 2.0f : (1.0f - shift) * 2.0f;
        float dotX = pillX + 4.0f + (pillW - 8.0f - 3.0f) * dotTravel;
        float dotY = pillY + pillH / 2.0f - 1.5f;
        ctx.drawRoundedRect(dotX, dotY, 3.0f, 3.0f, BorderRadius.all(1.5f),
                theme.getColor().mulAlpha(0.85f * alpha));
        // Trail
        for (int i = 1; i <= 4; ++i) {
            float backX = dotX - i * 2.6f * (dotTravel > 0.5f ? -1.0f : 1.0f);
            ctx.drawRoundedRect(backX, dotY + 0.4f, 2.4f, 2.2f, BorderRadius.all(1.1f),
                    theme.getColor().mulAlpha(0.85f * alpha * (1.0f - i * 0.22f)));
        }

        // Text glow halo behind "Exocle"
        float textY = baseY + (pillH - heavy.height()) / 2.0f - 0.5f;
        float startX = stripCenterX - totalW / 2.0f;
        float t = this.animationBrandPulse.getValue();
        float pulse = t < 0.5f ? t * 2.0f : (1.0f - t) * 2.0f;
        for (int i = 3; i >= 1; --i) {
            float a = 0.10f * pulse * alpha / i;
            ctx.drawText(heavy, BRAND_PREFIX, startX - i * 0.5f, textY, theme.getColor().mulAlpha(a));
            ctx.drawText(heavy, BRAND_PREFIX, startX + i * 0.5f, textY, theme.getColor().mulAlpha(a));
        }
        // Main text
        ctx.drawText(heavy, BRAND_PREFIX, startX, textY, theme.getColor().mulAlpha(alpha));
        ctx.drawText(light, BRAND_SUFFIX, startX + prefixW + spacer, textY, theme.getWhite().mulAlpha(0.9f * alpha));

        // Tiny subtitle below the pill
        String subtitle = "// premium overlay  •  v2";
        float subW = tiny.width(subtitle);
        ctx.drawText(tiny, subtitle, stripCenterX - subW / 2.0f, pillY + pillH + 2.0f,
                theme.getWhite().mulAlpha(0.35f * alpha));
    }

    private void renderHeaderRail(UIContext ctx, Theme theme, float x, float y, float width, float alpha) {
        if (alpha <= 0.001f || width <= 1.0f) return;
        // Static base line
        ctx.drawRoundedRect(x, y, width, 1.0f, BorderRadius.all(0.5f),
                theme.getForegroundColor().mulAlpha(0.5f * alpha));
        // Shimmer
        float t = this.animationBrandShift.getValue();
        float pos = t * width * 0.9f;
        float segW = Math.min(width * 0.35f, 80.0f);
        float startX = x + Math.max(-segW, pos - segW);
        float visW = Math.min(segW + width - pos, segW);
        if (visW > 1.0f) {
            ctx.drawRoundedRect(Math.max(x, startX), y, Math.min(visW, width), 1.0f, BorderRadius.all(0.5f),
                    theme.getColor().mulAlpha(0.55f * alpha));
        }
    }

    // ---------- helpers ----------

    private float getFitScale(float boxWidth, float boxHeight) {
        float pad = 24.0f;
        float maxW = Math.max(1.0f, (float)this.width - pad * 2.0f);
        float maxH = Math.max(1.0f, (float)this.height - pad * 2.0f);
        float scaleW = maxW / boxWidth;
        float scaleH = maxH / boxHeight;
        float scale = Math.min(1.0f, Math.min(scaleW, scaleH));
        return MathHelper.clamp((float)scale, (float)0.6f, (float)1.0f);
    }

    private float getInterfaceZoomScale() {
        if (mc == null || mc.getWindow() == null) {
            return 1.0f;
        }
        double windowScale = mc.getWindow().getScaleFactor();
        if (windowScale <= 0.0) {
            return 1.0f;
        }
        return MathHelper.clamp((float)((float)(2.0 / windowScale)), (float)0.72f, (float)1.12f);
    }

    private void updateInputTransform(float scale, float pivotX, float pivotY) {
        this.inputScale = Math.max(0.01f, scale);
        this.inputPivotX = pivotX;
        this.inputPivotY = pivotY;
    }

    private float toMenuX(double mouseX) {
        return this.inputPivotX + ((float)mouseX - this.inputPivotX) / this.inputScale;
    }

    private float toMenuY(double mouseY) {
        return this.inputPivotY + ((float)mouseY - this.inputPivotY) / this.inputScale;
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        float localMouseX = this.toMenuX(mouseX);
        float localMouseY = this.toMenuY(mouseY);
        if (!this.popupSettings.isEmpty()) {
            for (MenuPopupSetting setting : this.popupSettings) {
                if (setting.getBounds().contains(localMouseX, localMouseY)) {
                    setting.onMouseClicked(localMouseX, localMouseY, button);
                    return;
                }
                setting.getAnimationScale().update(0.0f);
            }
        }
        if (this.isClosing()) {
            return;
        }
        if (this.headerPanel.handleMouseClicked(localMouseX, localMouseY)) {
            if (this.headerPanel.searchBarBounds.contains(localMouseX, localMouseY)) {
                this.searchField.setSelected(true);
            }
            return;
        }
        if (this.sidebarPanel.handleMouseClicked(localMouseX, localMouseY)) {
            return;
        }
        if (this.searchField.isSelected()) {
            this.searchField.setSelected(false);
        }
        if (button.getButtonIndex() == 0 && MathUtil.isHovered(localMouseX, localMouseY, this.boxX, this.boxY, this.boxWidth, 20.0)) {
            this.dragging = true;
            this.dragOffsetX = localMouseX - this.boxX;
            this.dragOffsetY = localMouseY - this.boxY;
            return;
        }
        if (!this.animationClose.isDone()) {
            return;
        }
        float scrollbarX = this.boxX + this.boxWidth - 8.0f - 2.0f;
        float scrollbarY = this.boxY + 22.0f + 8.0f + 8.0f;
        float visibleHeight = this.boxHeight - 38.0f;
        if (button.getButtonIndex() == 0 && MathUtil.isHovered(localMouseX, localMouseY, scrollbarX, scrollbarY, 2.0, visibleHeight)) {
            this.draggingScrollbar = true;
            float scrollProgress = this.scrollHandler.getMax() == 0.0 ? 0.0f : (float)(this.scrollHandler.getValue() / this.scrollHandler.getMax());
            float scrollHeight = Math.max(visibleHeight * (visibleHeight / (float)((double)visibleHeight + this.scrollHandler.getMax())), 20.0f);
            float denom = Math.max(1.0f, visibleHeight - scrollHeight);
            float scrollY = scrollbarY + denom * scrollProgress;
            this.scrollClickOffset = localMouseY - scrollY;
            return;
        }
        if (!MathUtil.isHoveredByCords(localMouseX, localMouseY, this.scaledScissorX, this.scaledScissorY, this.scaledScissorEndX, this.scaledScissorEndY)) {
            return;
        }
        this.modules.stream().filter(m -> this.searchField.isEmpty() ? m.getCategory() == this.selectedCategory : m.getName().toLowerCase().contains(this.searchField.getText().toLowerCase())).forEach(menuModule -> menuModule.onMouseClicked(localMouseX, localMouseY, button));
        super.onMouseClicked(mouseX, mouseY, button);
    }

    public boolean charTyped(char chr, int modifiers) {
        if (this.searchField.isSelected()) {
            return this.searchField.charTyped(chr, modifiers);
        }
        for (MenuPopupSetting setting : this.popupSettings) {
            setting.charTyped(chr, modifiers);
        }
        boolean result = false;
        for (AbstractMenuElement module : this.modules) {
            if (!module.charTyped(chr, modifiers)) continue;
            result = true;
        }
        if (result) {
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public void onMouseReleased(double mouseX, double mouseY, MouseButton button) {
        float localMouseX = this.toMenuX(mouseX);
        float localMouseY = this.toMenuY(mouseY);
        for (MenuPopupSetting setting : this.popupSettings) {
            setting.onMouseReleased(localMouseX, localMouseY, button);
        }
        if (button.getButtonIndex() == 0) {
            this.dragging = false;
            this.draggingScrollbar = false;
            this.scrollClickOffset = 0.0f;
        }
        for (AbstractMenuElement module : this.modules) {
            module.onMouseReleased(localMouseX, localMouseY, button);
        }
        super.onMouseReleased(mouseX, mouseY, button);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean returnCheck = false;
        for (MenuPopupSetting setting : this.popupSettings) {
            if (!setting.keyPressed(keyCode, scanCode, modifiers)) continue;
            this.searchField.setSelected(false);
            returnCheck = true;
        }
        if (returnCheck) {
            return true;
        }
        if (this.searchField.isSelected()) {
            if (keyCode == 256) {
                this.searchField.setSelected(false);
                return true;
            }
            return this.searchField.keyPressed(keyCode, scanCode, modifiers);
        }
        boolean result = false;
        for (AbstractMenuElement module : this.modules) {
            if (!module.keyPressed(keyCode, scanCode, modifiers)) continue;
            result = true;
        }
        if (result) {
            return true;
        }
        if (keyCode == 256) {
            if (!this.closing) {
                this.onMouseReleased(0.0, 0.0, MouseButton.LEFT);
                this.onMouseReleased(0.0, 0.0, MouseButton.RIGHT);
                this.onMouseReleased(0.0, 0.0, MouseButton.MIDDLE);
                for (MenuPopupSetting setting : this.popupSettings) {
                    setting.getAnimationScale().setTargetValue(0.0f);
                }
                this.closing = true;
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public boolean shouldCloseOnEsc() {
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        float localMouseX = this.toMenuX(mouseX);
        float localMouseY = this.toMenuY(mouseY);
        if (!this.popupSettings.isEmpty()) {
            for (MenuPopupSetting setting : this.popupSettings) {
                setting.mouseScrolled(localMouseX, localMouseY, horizontalAmount, verticalAmount);
            }
            return true;
        }
        float visibleHeight = this.boxHeight - 38.0f;
        float baseStep = (float)Math.max(20.0, Math.min(60.0, this.scrollHandler.getMax() / (double)visibleHeight * 10.0));
        this.scrollHandler.scroll(verticalAmount * (double)baseStep / 8.0);
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void onMouseDragged(double mouseX, double mouseY, MouseButton button, double deltaX, double deltaY) {
        float localMouseX = this.toMenuX(mouseX);
        float localMouseY = this.toMenuY(mouseY);
        double localDeltaX = deltaX / (double)Math.max(0.01f, this.inputScale);
        double localDeltaY = deltaY / (double)Math.max(0.01f, this.inputScale);
        if (button.getButtonIndex() == 0 && this.dragging) {
            this.boxX = localMouseX - this.dragOffsetX;
            this.boxY = localMouseY - this.dragOffsetY;
            return;
        }
        for (AbstractMenuElement module : this.modules) {
            module.onMouseDragged(localMouseX, localMouseY, button, localDeltaX, localDeltaY);
        }
        super.onMouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    public void close() {
        super.close();
    }

    private void renderModules(UIContext ctx, float mouseX, float mouseY, float alpha, float contentStartX, float contentWidth, float startY) {
        List<AbstractMenuElement> modules = this.modules.stream().filter(m -> this.searchField.isEmpty() ? m.getCategory() == this.selectedCategory : m.getName().toLowerCase().contains(this.searchField.getText().toLowerCase())).sorted(Comparator.comparing(menuModule -> menuModule.getName(), String.CASE_INSENSITIVE_ORDER)).toList();
        int columns = this.columns;
        float padding = 6.0f;
        float scrollbarWidth = 6.0f;
        float maxContentWidth = contentWidth - scrollbarWidth;
        float moduleWidth = (maxContentWidth - padding * (float)(columns - 1)) / (float)columns;
        Font font = Fonts.MEDIUM.getFont(7.0f);
        double[] columnHeights = new double[columns];
        this.hoveredModuleDescription = null;

        Theme theme = VurstVisual.getInstance().getThemeManager().getCurrentTheme();
        long now = System.currentTimeMillis() - this.openTimeMs;

        int idx = 0;
        for (AbstractMenuElement module : modules) {
            String description;
            MenuModuleElement moduleElement;
            int col = 0;
            for (int j = 1; j < columns; ++j) {
                if (!(columnHeights[j] < columnHeights[col])) continue;
                col = j;
            }
            float x = contentStartX + (float)col * (moduleWidth + padding);
            float y = (float)((double)startY + columnHeights[col] - this.scrollHandler.getValue());

            // Staggered intro for module cards
            float stagger = MathHelper.clamp((float)((now - idx * 22L) / 320.0f), 0.0f, 1.0f);
            float cardAlpha = alpha * stagger;
            float cardOffsetY = (1.0f - stagger) * 8.0f;
            float drawY = y + cardOffsetY;

            // Soft hover halo behind hovered module cards
            boolean hovered = (module instanceof MenuModuleElement)
                    && ((MenuModuleElement)module).isModuleHovered(mouseX, mouseY);
            if (hovered && cardAlpha > 0.001f) {
                float h = module.getHeight();
                ctx.drawRoundedRect(x - 2.0f, drawY - 2.0f, moduleWidth + 4.0f, h + 4.0f,
                        BorderRadius.all(7.0f), theme.getColor().mulAlpha(0.16f * cardAlpha));
                ctx.drawRoundedRect(x - 1.0f, drawY - 1.0f, moduleWidth + 2.0f, h + 2.0f,
                        BorderRadius.all(6.5f), theme.getColor().mulAlpha(0.10f * cardAlpha));
            }

            module.render(ctx, mouseX, mouseY, font, x, drawY, moduleWidth, cardAlpha, col);

            if (module instanceof MenuModuleElement && (moduleElement = (MenuModuleElement)module).isModuleHovered(mouseX, mouseY) && (description = moduleElement.getDescription()) != null && !description.isBlank()) {
                this.hoveredModuleDescription = description;
            }
            int n = col;
            columnHeights[n] = columnHeights[n] + (double)(module.getHeight() + padding);
            ++idx;
        }
        this.scrollHandler.update();
        double maxY = Arrays.stream(columnHeights).max().orElse(0.0);
        float visibleHeight = this.boxHeight - 38.0f;
        this.scrollHandler.setMax(Math.max(0.0, maxY - (double)visibleHeight) + (double)(maxY > (double)visibleHeight ? 4 : 0));
    }

    private void renderHoveredDescription(UIContext ctx, float alpha) {
        if (this.hoveredModuleDescription == null || this.hoveredModuleDescription.isBlank()) {
            return;
        }
        if (this.headerPanel == null || this.headerPanel.searchBarBounds == null || this.headerPanel.layoutToggleButtonBounds == null) {
            return;
        }
        Font infoFont = Fonts.MEDIUM.getFont(6.5f);
        float left = this.headerPanel.layoutToggleButtonBounds.x() + this.headerPanel.layoutToggleButtonBounds.width() + 8.0f;
        float right = this.headerPanel.searchBarBounds.x() - 8.0f;
        float maxTextWidth = Math.max(0.0f, right - left);
        if (maxTextWidth <= 2.0f) {
            return;
        }
        List<String> lines = this.wrapTextToLines(infoFont, this.hoveredModuleDescription, maxTextWidth, 3);
        if (lines.isEmpty()) {
            return;
        }
        float lineGap = 1.0f;
        float lineHeight = infoFont.height();
        float totalHeight = (float)lines.size() * lineHeight + (float)Math.max(0, lines.size() - 1) * lineGap;
        float centerY = this.headerPanel.searchBarBounds.y() + this.headerPanel.searchBarBounds.height() / 2.0f;
        float textY = centerY - totalHeight / 2.0f;
        ColorRGBA color = ColorRGBA.WHITE.mulAlpha(alpha);
        for (String line : lines) {
            float lineWidth = infoFont.width(line);
            float textX = left + Math.max(0.0f, (maxTextWidth - lineWidth) / 2.0f);
            ctx.drawText(infoFont, line, textX, textY, color);
            textY += lineHeight + lineGap;
        }
    }

    private List<String> wrapTextToLines(Font font, String text, float maxWidth, int maxLines) {
        ArrayList<String> lines = new ArrayList<String>();
        if (text == null || text.isBlank() || maxWidth <= 0.0f || maxLines <= 0) {
            return lines;
        }
        String[] words = text.trim().split("\\s+");
        int index = 0;
        while (index < words.length && lines.size() < maxLines) {
            StringBuilder line = new StringBuilder();
            while (index < words.length) {
                String candidate;
                String word = words[index];
                String string = candidate = line.isEmpty() ? word : String.valueOf(line) + " " + word;
                if (!(font.width(candidate) <= maxWidth)) break;
                line.setLength(0);
                line.append(candidate);
                ++index;
            }
            if (line.isEmpty()) {
                String fitted = this.fitSingleWord(font, words[index], maxWidth);
                if (fitted.isEmpty()) break;
                line.append(fitted);
                ++index;
            }
            lines.add(line.toString());
        }
        if (index < words.length && !lines.isEmpty()) {
            int last = lines.size() - 1;
            lines.set(last, this.appendEllipsis(font, (String)lines.get(last), maxWidth));
        }
        return lines;
    }

    private String fitSingleWord(Font font, String word, float maxWidth) {
        int end;
        if (word == null || word.isEmpty()) {
            return "";
        }
        if (font.width(word) <= maxWidth) {
            return word;
        }
        for (end = word.length(); end > 0 && font.width(word.substring(0, end)) > maxWidth; --end) {
        }
        if (end <= 0) {
            return "";
        }
        return word.substring(0, end);
    }

    private String appendEllipsis(Font font, String line, float maxWidth) {
        String ellipsis;
        String base = line == null ? "" : line;
        if (font.width(base + (ellipsis = "...")) <= maxWidth) {
            return base + ellipsis;
        }
        while (!base.isEmpty() && font.width(base + ellipsis) > maxWidth) {
            base = base.substring(0, base.length() - 1);
        }
        return base.isEmpty() ? ellipsis : base + ellipsis;
    }

    public void addPopupMenuSetting(MenuPopupSetting setting) {
        this.popupSettings.add(setting);
    }

    public void removePopupMenuSetting(MenuPopupSetting setting) {
        this.popupSettings.remove(setting);
    }

    @Override
    public void render(UIContext context, float mouseX, float mouseY) {
        this.renderTop(context, mouseX, mouseY);
    }

    @Generated
    public int getColumns() {
        return this.columns;
    }

    @Generated
    public void setColumns(int columns) {
        this.columns = columns;
    }

    @Generated
    public boolean isClosing() {
        return this.closing;
    }

    @Generated
    public void setClosing(boolean closing) {
        this.closing = closing;
    }
}
