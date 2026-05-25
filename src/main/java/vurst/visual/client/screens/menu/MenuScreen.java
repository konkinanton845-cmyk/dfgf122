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

    public static final String BRAND_TITLE = "Exocle";
    public static final String BRAND_SUBTITLE = "Visuals";

    private Category selectedCategory = Category.MOVEMENT;
    private Category realSelectedCategory = Category.MOVEMENT;

    private float boxX;
    private float boxY;
    private int columns = 1;
    private float boxWidth = 540.0f;
    private float boxHeight = 332.0f;

    private boolean dragging;
    private float dragOffsetX;
    private float dragOffsetY;

    // Sidebar expansion
    private final Animation sidebarAnimation = new Animation(320L, 0.0f, Easing.CUBIC_IN_OUT);
    private boolean isSidebarExpanded;

    // Open / close animation. We keep the original BAKEK_SIZE easing for backwards
    // compatibility but route it through a longer duration with a soft tail so the
    // popup has a more deliberate, premium feel.
    private final Animation animationClose = new Animation(360L, 0.0f, Easing.BAKEK_SIZE);

    // Ambient animations – constantly running, used to drive glow, gradient drift,
    // and the title shimmer. They tick from 0 -> 1 -> 0 forever.
    private final Animation ambientPulse = new Animation(2400L, 0.0f, Easing.SINE_IN_OUT);
    private final Animation ambientDrift = new Animation(6000L, 0.0f, Easing.SINE_IN_OUT);
    private float ambientPulseDir = 1.0f;
    private float ambientDriftDir = 1.0f;

    // Hover/press animation for the drag-region "title bar".
    private final Animation titleHoverAnim = new Animation(220L, 0.0f, Easing.CUBIC_IN_OUT);

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

    // Animates the content panel from a slight slide+fade when the category changes.
    private final Animation categorySlide = new Animation(280L, 0.0f, Easing.CUBIC_OUT);

    private boolean draggingScrollbar = false;
    private float scrollClickOffset = 0.0f;

    private Set<MenuPopupSetting> popupSettings = new HashSet<MenuPopupSetting>();
    List<AbstractMenuElement> modules = new ArrayList<AbstractMenuElement>();

    private String hoveredModuleDescription;

    public MenuScreen() {
        this.animationColums = new Animation(320L, this.columns == 3 ? 1.0f : 0.0f, Easing.CUBIC_IN_OUT);
        this.animationChangeCategory = new Animation(180L, 1.0f, Easing.CUBIC_IN_OUT);
        this.animationScrollHeight = new Animation(160L, 1.0f, Easing.QUAD_IN_OUT);
    }

    public void initialize() {
        this.modules.addAll(VurstVisual.getInstance().getModuleManager().getModules().stream()
                .map(MenuModuleElement::new).toList());
        this.modules.add(new MenuThemeElement(Theme.DARK));
        this.modules.add(new MenuThemeElement(Theme.LIGHT));
        this.modules.add(new MenuThemeElement(Theme.CUSTOM_THEME));
    }

    protected void init() {
        this.closing = false;
        this.animationColums.setValue(this.columns == 3 ? 1.0f : 0.0f);
        this.boxWidth = MathHelper.lerp((float) this.animationColums.getValue(), 480, 552);
        this.boxHeight = MathHelper.lerp((float) this.animationColums.getValue(), 300, 336);
        this.boxX = ((float) this.width - this.boxWidth) / 2.0f;
        this.boxY = ((float) this.height - this.boxHeight) / 2.0f;
        this.updateInputTransform(1.0f, this.boxX + this.boxWidth / 2.0f, this.boxY + this.boxHeight / 2.0f);

        // Bounce-in: start slightly smaller so the easing can overshoot a touch.
        this.animationClose.setValue(0.0f);
        this.animationClose.update(1.0f);

        if (!this.initialized) {
            this.searchField = new TextBox(
                    new Vector2f(this.boxX + this.boxWidth - 128.0f - 8.0f, this.boxY + 8.0f),
                    Fonts.MEDIUM.getFont(7.0f), "Search", 100.0f);

            this.sidebarPanel = new SidebarPanel(this.sidebarAnimation, this.isSidebarExpanded, category -> {
                this.headerPanel.resetAnim(this.realSelectedCategory, (Category) ((Object) category));
                this.realSelectedCategory = category;
                this.scrollHandler.setTargetValue(0.0);
                this.searchField.setSelectAll(true);
                this.searchField.setSelected(true);
                this.searchField.keyPressed(259, 0, 0);
                this.searchField.setSelected(false);
                // Kick off the category-slide animation
                this.categorySlide.setValue(0.0f);
                this.categorySlide.setTargetValue(1.0f);
            }, () -> {
                this.isSidebarExpanded = !this.isSidebarExpanded;
                this.sidebarAnimation.animateTo(this.isSidebarExpanded ? 1.0f : 0.0f);
            });

            this.headerPanel = new HeaderPanel(this.searchField,
                    () -> this.columns = this.columns % 3 + 1,
                    () -> VurstVisual.getInstance().getThemeManager().switchTheme());
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
        // Intentionally empty – we paint our own dimmed/blurred backdrop inside renderTop
        // so the open/close animation can scale the dim layer along with the window.
    }

    public boolean isFinish() {
        return this.animationClose.getValue() == 0.0f && this.closing;
    }

    public void renderTop(UIContext ctx, float mouseX, float mouseY) {
        if (!this.initialized) return;

        // -- timing / progress --------------------------------------------------
        this.animationColums.update(this.columns == 3 ? 1.0f : 0.0f);
        this.boxWidth = MathHelper.lerp((float) this.animationColums.getValue(), 480, 552);
        this.boxHeight = MathHelper.lerp((float) this.animationColums.getValue(), 300, 336);

        float progress = this.animationClose.update(this.closing ? 0.0f : 1.0f);
        progress = MathHelper.clamp(progress, 0.0f, 1.0f);
        float sidebarProgress = this.sidebarAnimation.update();

        // Drive ambient looping animations.
        float ambient = this.ambientPulse.update(this.ambientPulseDir);
        if (ambient >= 1.0f) this.ambientPulseDir = 0.0f;
        else if (ambient <= 0.0f) this.ambientPulseDir = 1.0f;
        float drift = this.ambientDrift.update(this.ambientDriftDir);
        if (drift >= 1.0f) this.ambientDriftDir = 0.0f;
        else if (drift <= 0.0f) this.ambientDriftDir = 1.0f;

        // Smooth slide-in for category transitions.
        float categorySlideValue = this.categorySlide.update(1.0f);

        // -- scale / fit --------------------------------------------------------
        // Slight overshoot when opening; the BAKEK_SIZE easing already gives a soft
        // bounce, we just layer in a tiny zoom for extra polish.
        float bounce = (float) Math.sin(progress * Math.PI) * 0.025f;
        float baseScale = 0.86f + 0.14f * progress + bounce;

        float fitScale = this.getFitScale(this.boxWidth, this.boxHeight);
        float interfaceScale = this.getInterfaceZoomScale();
        float scale = MathHelper.clamp(baseScale * fitScale * interfaceScale, 0.5f, 1.18f);

        Theme theme = VurstVisual.getInstance().getThemeManager().getCurrentTheme();
        MatrixStack ms = ctx.getMatrices();

        // -- ambient dimmer / vignette behind the window ------------------------
        float dim = 0.55f * progress;
        ctx.drawRoundedRect(0.0f, 0.0f, (float) this.width, (float) this.height,
                BorderRadius.all(0.0f), ColorRGBA.BLACK.mulAlpha(dim));

        ctx.pushMatrix();
        float scaleX = this.boxX + this.boxWidth / 2.0f;
        float scaleY = this.boxY + this.boxHeight / 2.0f;
        this.updateInputTransform(scale, scaleX, scaleY);
        float localMouseX = this.toMenuX(mouseX);
        float localMouseY = this.toMenuY(mouseY);

        ms.translate(scaleX, scaleY, 1.0f);
        ms.scale(scale, scale, 1.0f);
        ms.translate(-scaleX, -scaleY, 1.0f);

        // -- colors -------------------------------------------------------------
        ColorRGBA primary = theme.getColor().mulAlpha(progress);
        ColorRGBA baseBg = theme.getBackgroundColor().mulAlpha(progress * 4.0f);
        ColorRGBA selectedColor = theme.getWhite().mulAlpha(progress);
        ColorRGBA textColor = theme.getWhite().mulAlpha(progress);
        ColorRGBA accentSoft = primary.mulAlpha(0.18f + 0.12f * ambient);
        ColorRGBA accentGlow = primary.mulAlpha(0.05f + 0.04f * ambient);

        // -- outer glow (animated) ---------------------------------------------
        for (int i = 6; i >= 1; --i) {
            float spread = i * 1.6f;
            float a = (0.06f - i * 0.008f) * progress;
            if (a <= 0.0f) continue;
            ctx.drawRoundedRect(
                    this.boxX - spread, this.boxY - spread,
                    this.boxWidth + spread * 2.0f, this.boxHeight + spread * 2.0f,
                    BorderRadius.all(11.0f + i),
                    accentGlow.mulAlpha(a / accentGlow.getAlphaFloat()));
        }

        // -- main panel ---------------------------------------------------------
        ctx.drawRoundedRect(this.boxX, this.boxY, this.boxWidth, this.boxHeight,
                BorderRadius.all(11.0f), baseBg);

        // Subtle gradient sheen sweeping across the panel based on drift value.
        float sheenX = this.boxX + this.boxWidth * (drift * 1.2f - 0.1f);
        float sheenW = Math.max(40.0f, this.boxWidth * 0.18f);
        ctx.drawRoundedRect(sheenX, this.boxY, sheenW, this.boxHeight,
                BorderRadius.all(11.0f),
                theme.getWhite().mulAlpha(0.025f * progress));

        // Inner 1px accent stroke that pulses softly.
        ctx.drawRoundedRect(this.boxX + 0.5f, this.boxY + 0.5f,
                this.boxWidth - 1.0f, this.boxHeight - 1.0f,
                BorderRadius.all(10.5f),
                accentSoft);

        // -- brand header (Exocle Visuals) -------------------------------------
        this.renderBrand(ctx, theme, progress, ambient, drift);

        // -- sidebar ------------------------------------------------------------
        float widthScroll = 2.0f;
        this.sidebarPanel.render(ctx, this.boxX, this.boxY, this.boxHeight,
                progress, theme, this.realSelectedCategory, primary, textColor, selectedColor);

        float sidebarWidth = 30.0f + 58.0f * sidebarProgress;
        float contentStartX = this.boxX + 8.0f + sidebarWidth + 8.0f;
        float sidebarY = this.boxY + 8.0f;
        float contentY = this.boxY + 22.0f + 8.0f + 8.0f;

        // -- header panel -------------------------------------------------------
        this.headerPanel.render(ctx, contentStartX, sidebarY, this.boxX, this.columns,
                this.boxWidth, progress, theme, this.realSelectedCategory);

        // -- scrollbar (with pulsing fill) -------------------------------------
        float visibleHeight = this.boxHeight - 46.0f;
        float scrollProgress = this.scrollHandler.getMax() == 0.0
                ? 0.0f
                : (float) (this.scrollHandler.getValue() / this.scrollHandler.getMax());
        float scrollHeight = Math.max(
                visibleHeight * (visibleHeight / (float) ((double) visibleHeight + this.scrollHandler.getMax())),
                20.0f);
        scrollHeight = Math.min(visibleHeight, this.animationScrollHeight.update(scrollHeight));
        float denom = Math.max(1.0f, visibleHeight - scrollHeight);
        float scrollY = contentY + denom * scrollProgress;
        scrollY = Math.min(contentY + visibleHeight, scrollY);

        ctx.drawRoundedRect(this.boxX + this.boxWidth - 8.0f - widthScroll, contentY,
                widthScroll, visibleHeight, BorderRadius.all(0.5f),
                theme.getForegroundColor().mulAlpha(progress));

        ColorRGBA scrollFill = theme.getForegroundStroke().mulAlpha(progress * (0.85f + 0.15f * ambient));
        if (scrollY + scrollHeight > visibleHeight + contentY) {
            ctx.drawRoundedRect(this.boxX + this.boxWidth - 8.0f - widthScroll, contentY,
                    widthScroll, visibleHeight, BorderRadius.all(1.0f), scrollFill);
        } else {
            ctx.drawRoundedRect(this.boxX + this.boxWidth - 8.0f - widthScroll, scrollY,
                    widthScroll, scrollHeight, BorderRadius.all(1.0f), scrollFill);
        }

        float contentWidth = this.boxX + (float) (this.columns == 3 ? 549 : 477) - contentStartX - 8.0f;
        this.scaledScissorX = (int) contentStartX;
        this.scaledScissorY = (int) ((float) ((int) this.boxY) + 38.0f);
        this.scaledScissorEndX = (int) (this.boxX + this.boxWidth);
        this.scaledScissorEndY = (int) ((float) ((int) this.boxY) + this.boxHeight);

        ctx.enableScissor(this.scaledScissorX, this.scaledScissorY,
                this.scaledScissorEndX, this.scaledScissorEndY);

        // Category-change crossfade + tiny vertical slide.
        this.animationChangeCategory.setEasing(Easing.QUAD_IN_OUT);
        float changeAlpha = progress * this.animationChangeCategory.update(
                this.selectedCategory == this.realSelectedCategory ? 1.0f : 0.0f);
        float slideOffset = (1.0f - categorySlideValue) * 6.0f; // px

        ms.translate(0.0f, slideOffset, 0.0f);
        this.renderModules(ctx, localMouseX, localMouseY - slideOffset,
                changeAlpha, (int) contentStartX, contentWidth, (int) contentY);
        ms.translate(0.0f, -slideOffset, 0.0f);

        ctx.disableScissor();

        this.renderHoveredDescription(ctx, progress);

        // -- popup settings -----------------------------------------------------
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

        // -- scrollbar dragging -------------------------------------------------
        if (this.draggingScrollbar) {
            float scrollbarY = this.boxY + 22.0f + 8.0f + 8.0f;
            float newY = localMouseY - scrollbarY - this.scrollClickOffset;
            float scrollRatio = newY / denom;
            this.scrollHandler.setTargetValue(-((double) scrollRatio * this.scrollHandler.getMax()));
        }

        ctx.popMatrix();
    }

    private void renderBrand(UIContext ctx, Theme theme, float progress, float ambient, float drift) {
        // Title sits in the drag region at the top-left of the window.
        Font titleFont = Fonts.MEDIUM.getFont(10.0f);
        Font subFont = Fonts.MEDIUM.getFont(7.0f);

        float titleX = this.boxX + 12.0f;
        float titleY = this.boxY + 6.0f;

        // Hover/press feedback on the brand title (also serves as drag handle).
        // We don't have direct hover state for the title region so we drive it from
        // the dragging flag.
        float hoverTarget = this.dragging ? 1.0f : 0.0f;
        float hover = this.titleHoverAnim.update(hoverTarget);

        ColorRGBA primary = theme.getColor();
        ColorRGBA white = theme.getWhite().mulAlpha(progress);

        // Soft glow behind the title that breathes with the ambient pulse.
        ColorRGBA titleGlow = primary.mulAlpha(progress * (0.18f + 0.12f * ambient));
        float glowW = titleFont.width(BRAND_TITLE) + 18.0f;
        float glowH = titleFont.height() + 6.0f;
        ctx.drawRoundedRect(titleX - 6.0f, titleY - 2.0f, glowW, glowH,
                BorderRadius.all(6.0f), titleGlow.mulAlpha(0.20f * (1.0f - hover * 0.4f)));

        // Title text – "Exocle" rendered in the accent color, "Visuals" in white.
        ctx.drawText(titleFont, BRAND_TITLE, titleX, titleY,
                primary.mulAlpha(progress));
        float titleWidth = titleFont.width(BRAND_TITLE);
        ctx.drawText(subFont, BRAND_SUBTITLE,
                titleX + titleWidth + 4.0f,
                titleY + (titleFont.height() - subFont.height()) - 0.5f,
                white.mulAlpha(0.85f));

        // Tiny animated accent dot to the left of the title.
        float dotR = 1.5f + 0.5f * ambient;
        ctx.drawRoundedRect(titleX - 8.0f, titleY + titleFont.height() / 2.0f - dotR,
                dotR * 2.0f, dotR * 2.0f, BorderRadius.all(dotR),
                primary.mulAlpha(progress * (0.6f + 0.4f * ambient)));

        // Thin underline that shifts horizontally with drift.
        float underlineY = titleY + titleFont.height() + 0.5f;
        float underlineW = titleWidth + subFont.width(BRAND_SUBTITLE) + 6.0f;
        float underlineShift = (drift - 0.5f) * 6.0f;
        ctx.drawRoundedRect(titleX + underlineShift, underlineY,
                underlineW * 0.6f, 0.6f, BorderRadius.all(0.3f),
                primary.mulAlpha(progress * (0.25f + 0.15f * ambient)));
    }

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

        if (button.getButtonIndex() == 0
                && MathUtil.isHovered(localMouseX, localMouseY, this.boxX, this.boxY, this.boxWidth, 22.0)) {
            this.dragging = true;
            this.dragOffsetX = localMouseX - this.boxX;
            this.dragOffsetY = localMouseY - this.boxY;
            return;
        }
        if (!this.animationClose.isDone()) return;

        float scrollbarX = this.boxX + this.boxWidth - 8.0f - 2.0f;
        float scrollbarY = this.boxY + 22.0f + 8.0f + 8.0f;
        float visibleHeight = this.boxHeight - 38.0f;
        if (button.getButtonIndex() == 0
                && MathUtil.isHovered(localMouseX, localMouseY, scrollbarX, scrollbarY, 2.0, visibleHeight)) {
            this.draggingScrollbar = true;
            float scrollProgress = this.scrollHandler.getMax() == 0.0
                    ? 0.0f
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
        float visibleHeight = this.boxHeight - 38.0f;
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

        // Staggered fade-in for individual modules when the category changes.
        float catProg = this.categorySlide.getValue();

        int rendered = 0;
        for (AbstractMenuElement module : modules) {
            int col = 0;
            for (int j = 1; j < columns; ++j) {
                if (!(columnHeights[j] < columnHeights[col])) continue;
                col = j;
            }
            float x = contentStartX + (float) col * (moduleWidth + padding);
            float y = (float) ((double) startY + columnHeights[col] - this.scrollHandler.getValue());

            // Per-module fade based on its index, so they cascade in.
            float stagger = MathHelper.clamp((catProg - rendered * 0.012f) * 1.4f, 0.0f, 1.0f);
            float moduleAlpha = alpha * stagger;

            module.render(ctx, mouseX, mouseY, font, x, y, moduleWidth, moduleAlpha, col);

            if (module instanceof MenuModuleElement) {
                MenuModuleElement moduleElement = (MenuModuleElement) module;
                if (moduleElement.isModuleHovered(mouseX, mouseY)) {
                    String description = moduleElement.getDescription();
                    if (description != null && !description.isBlank()) {
                        this.hoveredModuleDescription = description;
                    }
                }
            }
            int n = col;
            columnHeights[n] = columnHeights[n] + (double) (module.getHeight() + padding);
            rendered++;
        }
        this.scrollHandler.update();
        double maxY = Arrays.stream(columnHeights).max().orElse(0.0);
        float visibleHeight = this.boxHeight - 38.0f;
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
