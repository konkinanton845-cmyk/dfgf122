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

public class MenuScreen extends CustomScreen {

    // Brand
    private static final String BRAND_PREFIX = "EXOCLE";
    private static final String BRAND_SUFFIX = "VISUALS";
    private static final String BRAND_TAGLINE = "// PREMIUM OVERLAY";
    private static final int PARTICLE_COUNT = 36;
    private static final float TOP_BAR_H = 32.0f; // tall, prominent header strip

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

    private final Animation animationClose = new Animation(420L, 0.0f, Easing.CUBIC_IN_OUT);

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
    private final Animation animationIntro;        // 0->1 on open
    private final Animation animationScrollGlow;   // when dragging scrollbar
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
        this.animationIntro = new Animation(560L, 0.0f, Easing.CUBIC_IN_OUT);
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
        this.boxWidth = MathHelper.lerp((float) this.animationColums.getValue(), (int) 465, (int) 533);
        this.boxHeight = MathHelper.lerp((float) this.animationColums.getValue(), (int) 282, (int) 320);
        this.boxX = ((float) this.width - this.boxWidth) / 2.0f;
        this.boxY = ((float) this.height - this.boxHeight) / 2.0f;
        this.updateInputTransform(1.0f, this.boxX + this.boxWidth / 2.0f, this.boxY + this.boxHeight / 2.0f);
        this.animationClose.setValue(0.0f);
        this.animationClose.update(1.0f);
        this.animationIntro.setValue(0.0f);
        if (!this.initialized) {
            // Push the search field down to fit under the new tall top bar
            this.searchField = new TextBox(
                    new Vector2f(this.boxX + this.boxWidth - 128.0f - 8.0f, this.boxY + TOP_BAR_H + 6.0f),
                    Fonts.MEDIUM.getFont(7.0f), "Поиск", 100.0f);
            this.sidebarPanel = new SidebarPanel(this.sidebarAnimation, this.isSidebarExpanded, category -> {
                this.headerPanel.resetAnim(this.realSelectedCategory, (Category) ((Object) category));
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
        if (!this.initialized) return;

        this.animationColums.update(this.columns == 3 ? 1.0f : 0.0f);
        this.boxWidth = MathHelper.lerp((float) this.animationColums.getValue(), (int) 465, (int) 533);
        this.boxHeight = MathHelper.lerp((float) this.animationColums.getValue(), (int) 282, (int) 320);
        float progress = MathHelper.clamp(this.animationClose.update(this.closing ? 0.0f : 1.0f), 0.0f, 1.0f);
        float intro = MathHelper.clamp(this.animationIntro.update(1.0f), 0.0f, 1.0f);
        this.animationScrollGlow.update(this.draggingScrollbar ? 1.0f : 0.0f);

        long t = System.currentTimeMillis() - this.openTimeMs;
        float timeSec = t / 1000.0f;

        float sidebarProgress = this.sidebarAnimation.update();

        // Heavy opening animation: rotate-in (just scale-y wobble) + bounce
        float bounce = (float) Math.sin(progress * Math.PI) * 0.04f;
        float baseScale = 0.75f + 0.25f * progress + bounce;
        float introOffsetY = (1.0f - intro) * 24.0f;

        float fitScale = this.getFitScale(this.boxWidth, this.boxHeight);
        float interfaceScale = this.getInterfaceZoomScale();
        float scale = MathHelper.clamp(baseScale * fitScale * interfaceScale, 0.5f, 1.18f);

        Theme theme = VurstVisual.getInstance().getThemeManager().getCurrentTheme();
        MatrixStack ms = ctx.getMatrices();

        // === STRONG dim of the whole screen behind the menu (was empty before) ===
        ctx.drawRoundedRect(0.0f, 0.0f, (float) this.width, (float) this.height,
                BorderRadius.all(0.0f), ColorRGBA.BLACK.mulAlpha(0.55f * progress));

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

        // ----- Strong outer glow ring (much more visible than before) -----
        this.renderWindowGlow(ctx, theme, progress * intro, timeSec);

        // ----- Visible drifting particles -----
        this.renderAmbientParticles(ctx, theme, progress * intro, timeSec);

        // ----- Main panel body -----
        ctx.drawRoundedRect(this.boxX, this.boxY, this.boxWidth, this.boxHeight,
                BorderRadius.all(12.0f), baseBg);

        // ----- Animated multi-segment accent border (rotating around the panel) -----
        this.renderAnimatedBorder(ctx, theme, progress, timeSec);

        // ----- Tall top header strip ("EXOCLE VISUALS") -----
        this.renderTopBar(ctx, theme, progress, intro, timeSec);

        // ----- Diagonal scan-line that sweeps across the panel -----
        this.renderScanline(ctx, theme, progress * intro, timeSec);

        // ----- Bottom status strip -----
        this.renderBottomStrip(ctx, theme, progress * intro, timeSec);

        // ----- Existing sidebar and header (shifted down to fit under top bar) -----
        float sidebarY = this.boxY + TOP_BAR_H + 6.0f;
        float widthScroll = 2.0f;
        // Sidebar panel renders relative to boxY internally; we let it stay but the
        // top bar lives in the area above where the original drag handle was.
        this.sidebarPanel.render(ctx, this.boxX, this.boxY + TOP_BAR_H,
                this.boxHeight - TOP_BAR_H, progress, theme,
                this.realSelectedCategory, primary, textColor, selectedColor);
        float sidebarWidth = 30.0f + 58.0f * sidebarProgress;
        float contentStartX = this.boxX + 8.0f + sidebarWidth + 8.0f;
        float contentY = sidebarY + 22.0f + 8.0f;
        this.headerPanel.render(ctx, contentStartX, sidebarY, this.boxX,
                this.columns, this.boxWidth, progress, theme, this.realSelectedCategory);

        // ----- Scrollbar -----
        float visibleHeight = this.boxHeight - TOP_BAR_H - 38.0f;
        float scrollProgress = this.scrollHandler.getMax() == 0.0 ? 0.0f
                : (float) (this.scrollHandler.getValue() / this.scrollHandler.getMax());
        float scrollHeight = Math.max(
                visibleHeight * (visibleHeight / (float) ((double) visibleHeight + this.scrollHandler.getMax())),
                20.0f);
        scrollHeight = Math.min(visibleHeight, this.animationScrollHeight.update(scrollHeight));
        float denom = Math.max(1.0f, visibleHeight - scrollHeight);
        float scrollY = contentY + denom * scrollProgress;
        scrollY = Math.min(contentY + visibleHeight, scrollY);
        ctx.drawRoundedRect(this.boxX + this.boxWidth - 8.0f - widthScroll, contentY,
                widthScroll, visibleHeight, BorderRadius.all(1.0f),
                theme.getForegroundColor().mulAlpha(progress * 0.85f));
        float scrollGlowAlpha = this.animationScrollGlow.getValue() * 0.55f * progress;
        if (scrollGlowAlpha > 0.001f) {
            ctx.drawRoundedRect(this.boxX + this.boxWidth - 8.0f - widthScroll - 2.0f,
                    scrollY - 2.0f, widthScroll + 4.0f, scrollHeight + 4.0f,
                    BorderRadius.all(3.0f), theme.getColor().mulAlpha(scrollGlowAlpha));
        }
        ctx.drawRoundedRect(this.boxX + this.boxWidth - 8.0f - widthScroll,
                scrollY, widthScroll, scrollHeight, BorderRadius.all(1.0f),
                theme.getColor().mulAlpha(progress));

        float contentWidth = this.boxX + (float) (this.columns == 3 ? 530 : 461) - contentStartX - 8.0f;
        this.scaledScissorX = (int) contentStartX;
        this.scaledScissorY = (int) ((float) ((int) this.boxY) + TOP_BAR_H + 38.0f);
        this.scaledScissorEndX = (int) (this.boxX + this.boxWidth);
        this.scaledScissorEndY = (int) ((float) ((int) this.boxY) + this.boxHeight - 12.0f);
        ctx.enableScissor(this.scaledScissorX, this.scaledScissorY,
                this.scaledScissorEndX, this.scaledScissorEndY);
        this.animationChangeCategory.setEasing(Easing.QUAD_IN_OUT);
        float catAlpha = progress * intro * this.animationChangeCategory.update(
                this.selectedCategory == this.realSelectedCategory ? 1.0f : 0.0f);
        this.renderModules(ctx, localMouseX, localMouseY, catAlpha,
                (int) contentStartX, contentWidth, (int) contentY);
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
            float scrollbarY = contentY;
            float newY = localMouseY - scrollbarY - this.scrollClickOffset;
            float scrollRatio = newY / denom;
            this.scrollHandler.setTargetValue(-((double) scrollRatio * this.scrollHandler.getMax()));
        }
        ctx.popMatrix();
    }

    // =========================================================================
    // DECORATIVE RENDERERS
    // =========================================================================

    /** Bright stacked halo behind the panel. Much more visible than before. */
    private void renderWindowGlow(UIContext ctx, Theme theme, float alpha, float timeSec) {
        if (alpha <= 0.001f) return;
        ColorRGBA accent = theme.getColor();
        float pulse = 0.5f + 0.5f * (float) Math.sin(timeSec * 2.0f);
        for (int i = 8; i >= 1; --i) {
            float spread = i * 5.0f + pulse * 6.0f;
            float a = (0.18f - i * 0.018f) * (0.65f + 0.35f * pulse) * alpha;
            if (a <= 0.0f) continue;
            ctx.drawRoundedRect(this.boxX - spread, this.boxY - spread,
                    this.boxWidth + spread * 2.0f, this.boxHeight + spread * 2.0f,
                    BorderRadius.all(12.0f + spread * 0.45f),
                    accent.mulAlpha(a));
        }
    }

    /** Visible particles drifting around the panel. */
    private void renderAmbientParticles(UIContext ctx, Theme theme, float alpha, float timeSec) {
        if (alpha <= 0.001f) return;
        ColorRGBA tint = theme.getColor();
        ColorRGBA soft = theme.getWhite();
        float w = this.boxWidth + 120.0f;
        float h = this.boxHeight + 120.0f;
        float ox = this.boxX - 60.0f;
        float oy = this.boxY - 60.0f;
        for (int i = 0; i < PARTICLE_COUNT; ++i) {
            float seed = (i + 1) * 0.6180339f;
            float baseX = ((seed * 113.0f) % 1.0f) * w;
            float baseY = ((seed * 271.0f) % 1.0f) * h;
            float driftX = (float) Math.sin(timeSec * 0.6f + i * 0.7f) * 30.0f;
            float driftY = (float) Math.cos(timeSec * 0.5f + i * 1.1f) * 22.0f;
            float px = ox + (((baseX + driftX) % w) + w) % w;
            float py = oy + (((baseY + driftY) % h) + h) % h;
            float sz = 1.5f + ((seed * 41.0f) % 1.0f) * 2.6f;
            float twinkle = 0.45f + 0.55f * (float) (0.5 + 0.5 * Math.sin(timeSec * 2.5f + i * 1.3f));
            float pAlpha = 0.28f * twinkle * alpha;
            ColorRGBA c = (i % 3 == 0 ? tint : soft).mulAlpha(pAlpha);
            // small halo around the particle
            ctx.drawRoundedRect(px - sz, py - sz, sz * 3.0f, sz * 3.0f,
                    BorderRadius.all(sz * 1.5f), c.mulAlpha(0.25f));
            ctx.drawRoundedRect(px, py, sz, sz, BorderRadius.all(sz * 0.5f), c);
        }
    }

    /** Animated traveling segment(s) running around the panel border. */
    private void renderAnimatedBorder(UIContext ctx, Theme theme, float alpha, float timeSec) {
        if (alpha <= 0.001f) return;
        ColorRGBA accent = theme.getColor();
        float radius = 12.0f;

        // Static thin outline (always visible)
        float thin = 0.8f;
        ctx.drawRoundedRect(this.boxX - thin, this.boxY - thin,
                this.boxWidth + thin * 2.0f, thin, BorderRadius.all(radius), accent.mulAlpha(0.45f * alpha));
        ctx.drawRoundedRect(this.boxX - thin, this.boxY + this.boxHeight,
                this.boxWidth + thin * 2.0f, thin, BorderRadius.all(radius), accent.mulAlpha(0.45f * alpha));
        ctx.drawRoundedRect(this.boxX - thin, this.boxY,
                thin, this.boxHeight, BorderRadius.all(radius), accent.mulAlpha(0.45f * alpha));
        ctx.drawRoundedRect(this.boxX + this.boxWidth, this.boxY,
                thin, this.boxHeight, BorderRadius.all(radius), accent.mulAlpha(0.45f * alpha));

        // Perimeter travel: 0..1 around the panel, two opposite chasers
        float perimeter = (this.boxWidth + this.boxHeight) * 2.0f;
        float segLen = perimeter * 0.18f;
        for (int chaser = 0; chaser < 2; ++chaser) {
            float phase = (timeSec * 0.38f + chaser * 0.5f) % 1.0f;
            this.drawPerimeterSegment(ctx, phase, segLen, accent.mulAlpha(0.85f * alpha), 2.0f);
        }

        // Corner accents (bright L-shaped brackets)
        ColorRGBA bracket = accent.mulAlpha(alpha);
        float cl = 14.0f;
        float cw = 2.0f;
        // top-left
        ctx.drawRoundedRect(this.boxX - 1.0f, this.boxY - 1.0f, cl, cw, BorderRadius.all(1.0f), bracket);
        ctx.drawRoundedRect(this.boxX - 1.0f, this.boxY - 1.0f, cw, cl, BorderRadius.all(1.0f), bracket);
        // top-right
        ctx.drawRoundedRect(this.boxX + this.boxWidth - cl + 1.0f, this.boxY - 1.0f, cl, cw, BorderRadius.all(1.0f), bracket);
        ctx.drawRoundedRect(this.boxX + this.boxWidth - cw + 1.0f, this.boxY - 1.0f, cw, cl, BorderRadius.all(1.0f), bracket);
        // bottom-left
        ctx.drawRoundedRect(this.boxX - 1.0f, this.boxY + this.boxHeight - cw + 1.0f, cl, cw, BorderRadius.all(1.0f), bracket);
        ctx.drawRoundedRect(this.boxX - 1.0f, this.boxY + this.boxHeight - cl + 1.0f, cw, cl, BorderRadius.all(1.0f), bracket);
        // bottom-right
        ctx.drawRoundedRect(this.boxX + this.boxWidth - cl + 1.0f, this.boxY + this.boxHeight - cw + 1.0f, cl, cw, BorderRadius.all(1.0f), bracket);
        ctx.drawRoundedRect(this.boxX + this.boxWidth - cw + 1.0f, this.boxY + this.boxHeight - cl + 1.0f, cw, cl, BorderRadius.all(1.0f), bracket);
    }

    private void drawPerimeterSegment(UIContext ctx, float phase, float segLen, ColorRGBA color, float thickness) {
        float perimeter = (this.boxWidth + this.boxHeight) * 2.0f;
        for (int i = 0; i < 16; ++i) {
            float local = (phase * perimeter + i * (segLen / 16.0f)) % perimeter;
            float alphaFade = 1.0f - i / 16.0f;
            float[] p = this.perimeterPoint(local);
            float t = thickness * (0.55f + 0.45f * alphaFade);
            ctx.drawRoundedRect(p[0] - t / 2.0f, p[1] - t / 2.0f, t, t,
                    BorderRadius.all(t / 2.0f), color.mulAlpha(alphaFade));
        }
    }

    private float[] perimeterPoint(float dist) {
        float w = this.boxWidth;
        float h = this.boxHeight;
        if (dist < w) {
            return new float[] { this.boxX + dist, this.boxY - 1.0f };
        }
        dist -= w;
        if (dist < h) {
            return new float[] { this.boxX + w + 1.0f, this.boxY + dist };
        }
        dist -= h;
        if (dist < w) {
            return new float[] { this.boxX + w - dist, this.boxY + h + 1.0f };
        }
        dist -= w;
        return new float[] { this.boxX - 1.0f, this.boxY + h - dist };
    }

    /** Tall, prominent header bar at the top of the panel with "EXOCLE VISUALS". */
    private void renderTopBar(UIContext ctx, Theme theme, float alpha, float intro, float timeSec) {
        if (alpha <= 0.001f) return;
        float barX = this.boxX;
        float barY = this.boxY;
        float barW = this.boxWidth;
        float barH = TOP_BAR_H;

        // Darker tinted backdrop for the top bar
        ctx.drawRoundedRect(barX, barY, barW, barH, BorderRadius.all(12.0f),
                theme.getBackgroundColor().mulAlpha(alpha * 5.5f));
        // Subtle separator line below the bar
        ctx.drawRoundedRect(barX + 8.0f, barY + barH - 0.6f, barW - 16.0f, 0.6f,
                BorderRadius.all(0.3f), theme.getColor().mulAlpha(0.55f * alpha));

        // Glow halo behind the title
        float pulse = 0.5f + 0.5f * (float) Math.sin(timeSec * 1.6f);
        Font heavy = Fonts.MEDIUM.getFont(11.5f);
        Font light = Fonts.MEDIUM.getFont(11.5f);
        Font tiny = Fonts.MEDIUM.getFont(5.5f);

        float prefixW = heavy.width(BRAND_PREFIX);
        float spacer = 5.0f;
        float suffixW = light.width(BRAND_SUFFIX);
        float totalW = prefixW + spacer + suffixW;

        float introX = (1.0f - intro) * -20.0f;
        float textX = barX + 14.0f + introX;
        float textY = barY + (barH - heavy.height()) / 2.0f - 1.0f;

        // Multi-layer glow behind "EXOCLE"
        for (int i = 4; i >= 1; --i) {
            float a = 0.10f * pulse * alpha / i;
            ctx.drawText(heavy, BRAND_PREFIX, textX - i * 0.8f, textY, theme.getColor().mulAlpha(a));
            ctx.drawText(heavy, BRAND_PREFIX, textX + i * 0.8f, textY, theme.getColor().mulAlpha(a));
            ctx.drawText(heavy, BRAND_PREFIX, textX, textY - i * 0.8f, theme.getColor().mulAlpha(a));
            ctx.drawText(heavy, BRAND_PREFIX, textX, textY + i * 0.8f, theme.getColor().mulAlpha(a));
        }
        // Main title
        ctx.drawText(heavy, BRAND_PREFIX, textX, textY, theme.getColor().mulAlpha(alpha));
        ctx.drawText(light, BRAND_SUFFIX, textX + prefixW + spacer, textY,
                theme.getWhite().mulAlpha(0.95f * alpha));

        // Tagline below the title
        float tagY = textY + heavy.height() - 1.0f;
        ctx.drawText(tiny, BRAND_TAGLINE, textX, tagY,
                theme.getWhite().mulAlpha(0.45f * alpha));

        // Animated indicator dot to the left of the title
        float dotR = 2.5f + 1.0f * pulse;
        float dotX = barX + 6.5f;
        float dotY = barY + barH / 2.0f - 0.5f;
        ctx.drawRoundedRect(dotX - dotR, dotY - dotR, dotR * 2.0f, dotR * 2.0f,
                BorderRadius.all(dotR), theme.getColor().mulAlpha(alpha));
        // Pulse ring
        float ringR = dotR + 2.0f + pulse * 2.0f;
        ctx.drawRoundedRect(dotX - ringR, dotY - ringR, ringR * 2.0f, ringR * 2.0f,
                BorderRadius.all(ringR), theme.getColor().mulAlpha(0.25f * (1.0f - pulse) * alpha));

        // Right-side mini status pills (animated dots)
        float pillX = barX + barW - 14.0f;
        float pillY = barY + barH / 2.0f;
        for (int i = 0; i < 3; ++i) {
            float pp = (float) (0.5 + 0.5 * Math.sin(timeSec * 3.0f + i * 1.3f));
            float pr = 1.4f + 0.6f * pp;
            float px = pillX - i * 6.0f;
            ctx.drawRoundedRect(px - pr, pillY - pr, pr * 2.0f, pr * 2.0f,
                    BorderRadius.all(pr), theme.getColor().mulAlpha((0.4f + 0.6f * pp) * alpha));
        }
    }

    /** Diagonal scanline sweeping across the panel. */
    private void renderScanline(UIContext ctx, Theme theme, float alpha, float timeSec) {
        if (alpha <= 0.001f) return;
        float cycle = 5.5f;
        float t = (timeSec % cycle) / cycle;
        // skip rendering for part of the cycle so it's a flash, not a constant beam
        if (t > 0.55f) return;
        float local = t / 0.55f;
        float panelTop = this.boxY + TOP_BAR_H + 2.0f;
        float panelBottom = this.boxY + this.boxHeight - 12.0f;
        float y = panelTop + (panelBottom - panelTop) * local;
        float lineAlpha = (float) Math.sin(local * Math.PI) * 0.45f * alpha;
        if (lineAlpha <= 0.001f) return;
        ctx.drawRoundedRect(this.boxX + 2.0f, y, this.boxWidth - 4.0f, 1.2f,
                BorderRadius.all(0.6f), theme.getColor().mulAlpha(lineAlpha));
        // Soft band above the line
        ctx.drawRoundedRect(this.boxX + 2.0f, y - 4.0f, this.boxWidth - 4.0f, 4.0f,
                BorderRadius.all(2.0f), theme.getColor().mulAlpha(lineAlpha * 0.18f));
    }

    /** Small footer with module count + version, animated. */
    private void renderBottomStrip(UIContext ctx, Theme theme, float alpha, float timeSec) {
        if (alpha <= 0.001f) return;
        Font tiny = Fonts.MEDIUM.getFont(5.5f);
        int total = this.modules.size();
        int enabled = 0;
        for (AbstractMenuElement m : this.modules) {
            if (!(m instanceof MenuModuleElement)) continue;
            try {
                if (((MenuModuleElement) m).getClass().getDeclaredField("module") != null) {
                    // Reflection is overkill here; the panel already shows per-category stats.
                }
            } catch (Throwable ignored) {
            }
        }
        String left = "EXOCLE //  modules: " + total;
        String right = "build " + (1000 + (int) (timeSec * 0) ) + "  •  ONLINE";
        float y = this.boxY + this.boxHeight - 9.0f;
        ctx.drawText(tiny, left, this.boxX + 10.0f, y,
                theme.getWhite().mulAlpha(0.45f * alpha));
        float rw = tiny.width(right);
        ctx.drawText(tiny, right, this.boxX + this.boxWidth - 10.0f - rw, y,
                theme.getColor().mulAlpha(0.7f * alpha));
    }

    // =========================================================================
    // helpers
    // =========================================================================

    private float getFitScale(float boxWidth, float boxHeight) {
        float pad = 24.0f;
        float maxW = Math.max(1.0f, (float) this.width - pad * 2.0f);
        float maxH = Math.max(1.0f, (float) this.height - pad * 2.0f);
        float scaleW = maxW / boxWidth;
        float scaleH = maxH / boxHeight;
        float scale = Math.min(1.0f, Math.min(scaleW, scaleH));
        return MathHelper.clamp(scale, 0.6f, 1.0f);
    }

    private float getInterfaceZoomScale() {
        if (mc == null || mc.getWindow() == null) return 1.0f;
        double windowScale = mc.getWindow().getScaleFactor();
        if (windowScale <= 0.0) return 1.0f;
        return MathHelper.clamp((float) (2.0 / windowScale), 0.72f, 1.12f);
    }

    private void updateInputTransform(float scale, float pivotX, float pivotY) {
        this.inputScale = Math.max(0.01f, scale);
        this.inputPivotX = pivotX;
        this.inputPivotY = pivotY;
    }

    private float toMenuX(double mouseX) {
        return this.inputPivotX + ((float) mouseX - this.inputPivotX) / this.inputScale;
    }

    private float toMenuY(double mouseY) {
        return this.inputPivotY + ((float) mouseY - this.inputPivotY) / this.inputScale;
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
        if (this.isClosing()) return;
        if (this.headerPanel.handleMouseClicked(localMouseX, localMouseY)) {
            if (this.headerPanel.searchBarBounds.contains(localMouseX, localMouseY)) {
                this.searchField.setSelected(true);
            }
            return;
        }
        if (this.sidebarPanel.handleMouseClicked(localMouseX, localMouseY)) return;
        if (this.searchField.isSelected()) this.searchField.setSelected(false);

        // Drag region: now matches the tall top bar
        if (button.getButtonIndex() == 0
                && MathUtil.isHovered(localMouseX, localMouseY,
                this.boxX, this.boxY, this.boxWidth, TOP_BAR_H)) {
            this.dragging = true;
            this.dragOffsetX = localMouseX - this.boxX;
            this.dragOffsetY = localMouseY - this.boxY;
            return;
        }
        if (!this.animationClose.isDone()) return;

        float scrollbarX = this.boxX + this.boxWidth - 8.0f - 2.0f;
        float scrollbarY = this.boxY + TOP_BAR_H + 22.0f + 8.0f;
        float visibleHeight = this.boxHeight - TOP_BAR_H - 38.0f;
        if (button.getButtonIndex() == 0
                && MathUtil.isHovered(localMouseX, localMouseY, scrollbarX, scrollbarY, 2.0, visibleHeight)) {
            this.draggingScrollbar = true;
            float scrollProgress = this.scrollHandler.getMax() == 0.0 ? 0.0f
                    : (float) (this.scrollHandler.getValue() / this.scrollHandler.getMax());
            float scrollHeight = Math.max(
                    visibleHeight * (visibleHeight / (float) ((double) visibleHeight + this.scrollHandler.getMax())),
                    20.0f);
            float denom = Math.max(1.0f, visibleHeight - scrollHeight);
            float scrollY = scrollbarY + denom * scrollProgress;
            this.scrollClickOffset = localMouseY - scrollY;
            return;
        }
        if (!MathUtil.isHoveredByCords(localMouseX, localMouseY,
                this.scaledScissorX, this.scaledScissorY, this.scaledScissorEndX, this.scaledScissorEndY)) {
            return;
        }
        this.modules.stream()
                .filter(m -> this.searchField.isEmpty()
                        ? m.getCategory() == this.selectedCategory
                        : m.getName().toLowerCase().contains(this.searchField.getText().toLowerCase()))
                .forEach(menuModule -> menuModule.onMouseClicked(localMouseX, localMouseY, button));
        super.onMouseClicked(mouseX, mouseY, button);
    }

    public boolean charTyped(char chr, int modifiers) {
        if (this.searchField.isSelected()) return this.searchField.charTyped(chr, modifiers);
        for (MenuPopupSetting setting : this.popupSettings) setting.charTyped(chr, modifiers);
        boolean result = false;
        for (AbstractMenuElement module : this.modules) {
            if (!module.charTyped(chr, modifiers)) continue;
            result = true;
        }
        if (result) return true;
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
        if (returnCheck) return true;
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
        if (result) return true;
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
        float visibleHeight = this.boxHeight - TOP_BAR_H - 38.0f;
        float baseStep = (float) Math.max(20.0,
                Math.min(60.0, this.scrollHandler.getMax() / (double) visibleHeight * 10.0));
        this.scrollHandler.scroll(verticalAmount * (double) baseStep / 8.0);
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void onMouseDragged(double mouseX, double mouseY, MouseButton button, double deltaX, double deltaY) {
        float localMouseX = this.toMenuX(mouseX);
        float localMouseY = this.toMenuY(mouseY);
        double localDeltaX = deltaX / (double) Math.max(0.01f, this.inputScale);
        double localDeltaY = deltaY / (double) Math.max(0.01f, this.inputScale);
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

    private void renderModules(UIContext ctx, float mouseX, float mouseY, float alpha,
                               float contentStartX, float contentWidth, float startY) {
        List<AbstractMenuElement> modules = this.modules.stream()
                .filter(m -> this.searchField.isEmpty()
                        ? m.getCategory() == this.selectedCategory
                        : m.getName().toLowerCase().contains(this.searchField.getText().toLowerCase()))
                .sorted(Comparator.comparing(menuModule -> menuModule.getName(), String.CASE_INSENSITIVE_ORDER))
                .toList();
        int columns = this.columns;
        float padding = 6.0f;
        float scrollbarWidth = 6.0f;
        float maxContentWidth = contentWidth - scrollbarWidth;
        float moduleWidth = (maxContentWidth - padding * (float) (columns - 1)) / (float) columns;
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
            float x = contentStartX + (float) col * (moduleWidth + padding);
            float y = (float) ((double) startY + columnHeights[col] - this.scrollHandler.getValue());

            float stagger = MathHelper.clamp((float) ((now - idx * 32L) / 380.0f), 0.0f, 1.0f);
            float cardAlpha = alpha * stagger;
            float cardOffsetX = (1.0f - stagger) * 12.0f * (col % 2 == 0 ? -1.0f : 1.0f);
            float cardOffsetY = (1.0f - stagger) * 10.0f;
            float drawX = x + cardOffsetX;
            float drawY = y + cardOffsetY;

            // Strong hover halo
            boolean hovered = (module instanceof MenuModuleElement)
                    && ((MenuModuleElement) module).isModuleHovered(mouseX, mouseY);
            if (hovered && cardAlpha > 0.001f) {
                float h = module.getHeight();
                for (int i = 4; i >= 1; --i) {
                    float sp = i * 1.5f;
                    ctx.drawRoundedRect(drawX - sp, drawY - sp,
                            moduleWidth + sp * 2.0f, h + sp * 2.0f,
                            BorderRadius.all(7.0f + sp),
                            theme.getColor().mulAlpha((0.06f + 0.02f * i) * cardAlpha));
                }
            }

            module.render(ctx, mouseX, mouseY, font, drawX, drawY, moduleWidth, cardAlpha, col);

            if (module instanceof MenuModuleElement
                    && (moduleElement = (MenuModuleElement) module).isModuleHovered(mouseX, mouseY)
                    && (description = moduleElement.getDescription()) != null
                    && !description.isBlank()) {
                this.hoveredModuleDescription = description;
            }
            int n = col;
            columnHeights[n] = columnHeights[n] + (double) (module.getHeight() + padding);
            ++idx;
        }
        this.scrollHandler.update();
        double maxY = Arrays.stream(columnHeights).max().orElse(0.0);
        float visibleHeight = this.boxHeight - TOP_BAR_H - 38.0f;
        this.scrollHandler.setMax(Math.max(0.0, maxY - (double) visibleHeight)
                + (double) (maxY > (double) visibleHeight ? 4 : 0));
    }

    private void renderHoveredDescription(UIContext ctx, float alpha) {
        if (this.hoveredModuleDescription == null || this.hoveredModuleDescription.isBlank()) return;
        if (this.headerPanel == null
                || this.headerPanel.searchBarBounds == null
                || this.headerPanel.layoutToggleButtonBounds == null) return;
        Font infoFont = Fonts.MEDIUM.getFont(6.5f);
        float left = this.headerPanel.layoutToggleButtonBounds.x()
                + this.headerPanel.layoutToggleButtonBounds.width() + 8.0f;
        float right = this.headerPanel.searchBarBounds.x() - 8.0f;
        float maxTextWidth = Math.max(0.0f, right - left);
        if (maxTextWidth <= 2.0f) return;
        List<String> lines = this.wrapTextToLines(infoFont, this.hoveredModuleDescription, maxTextWidth, 3);
        if (lines.isEmpty()) return;
        float lineGap = 1.0f;
        float lineHeight = infoFont.height();
        float totalHeight = (float) lines.size() * lineHeight
                + (float) Math.max(0, lines.size() - 1) * lineGap;
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
        if (text == null || text.isBlank() || maxWidth <= 0.0f || maxLines <= 0) return lines;
        String[] words = text.trim().split("\\s+");
        int index = 0;
        while (index < words.length && lines.size() < maxLines) {
            StringBuilder line = new StringBuilder();
            while (index < words.length) {
                String word = words[index];
                String candidate = line.isEmpty() ? word : line + " " + word;
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
            lines.set(last, this.appendEllipsis(font, lines.get(last), maxWidth));
        }
        return lines;
    }

    private String fitSingleWord(Font font, String word, float maxWidth) {
        if (word == null || word.isEmpty()) return "";
        if (font.width(word) <= maxWidth) return word;
        int end;
        for (end = word.length(); end > 0 && font.width(word.substring(0, end)) > maxWidth; --end) {
        }
        if (end <= 0) return "";
        return word.substring(0, end);
    }

    private String appendEllipsis(Font font, String line, float maxWidth) {
        String ellipsis = "...";
        String base = line == null ? "" : line;
        if (font.width(base + ellipsis) <= maxWidth) return base + ellipsis;
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
