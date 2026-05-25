package vurst.visual.client.modules.impl.hud;

import com.darkmagician6.eventapi.EventTarget;
import com.google.gson.JsonObject;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import vurst.visual.base.events.impl.input.EventMouse;
import vurst.visual.base.events.impl.player.EventUpdate;
import vurst.visual.base.events.impl.render.EventHudRender;
import vurst.visual.base.font.Font;
import vurst.visual.base.font.Fonts;
import vurst.visual.base.theme.Theme;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.ModuleAnnotation;
import vurst.visual.client.modules.api.setting.impl.BooleanSetting;
import vurst.visual.utility.render.display.base.BorderRadius;
import vurst.visual.utility.render.display.base.CustomDrawContext;
import vurst.visual.utility.render.display.base.color.ColorRGBA;
import vurst.visual.utility.render.display.shader.DrawUtil;

@ModuleAnnotation(name = "Watermark", category = Category.HUD, description = "Exocle Visuals brand watermark.")
public final class Watermark extends HudModule {

    public static final Watermark INSTANCE = new Watermark();

    private static final String LEGACY_SHOW_TIME_KEY = "Show Time";
    private static final String BRAND_PREFIX = "EXOCLE";
    private static final String BRAND_SUFFIX = "VISUALS";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private static final float PILL_HEIGHT = 18.0f;
    private static final float PILL_RADIUS = 7.5f;
    private static final float PAD_X = 10.0f;
    private static final float GAP = 6.0f;
    private static final float SEP_GAP = 5.0f;
    private static final float BRAND_GAP = 3.0f;

    // Separator and subtle white tints (theme-independent)
    private static final ColorRGBA SEPARATOR_COLOR = new ColorRGBA(255, 255, 255, 42);
    private static final ColorRGBA SUBTLE_WHITE = new ColorRGBA(255, 255, 255, 28);

    private final BooleanSetting showTime = new BooleanSetting("Show Time", true);
    private final BooleanSetting showFps = new BooleanSetting("Показывать FPS", true);
    private final BooleanSetting glow = new BooleanSetting("Glow", true);

    private Watermark() {
        super(10.0f, 10.0f, 140.0f, PILL_HEIGHT, true);
    }

    @Override
    public void load(JsonObject object) {
        this.migrateLegacySettings(object);
        super.load(object);
    }

    @Override
    protected void draw(CustomDrawContext ctx) {
        Theme theme = this.getTheme();
        Font font = Fonts.ROUND_BOLD.getFont(7.2f);
        Font brandFont = Fonts.ROUND_BOLD.getFont(7.6f);
        Font tagFont = Fonts.MEDIUM.getFont(5.0f);

        boolean drawTime = this.showTime.isEnabled();
        boolean drawFps = this.showFps.isEnabled();

        String time = LocalTime.now().format(TIME_FORMATTER);
        String fps = mc.getCurrentFps() + " FPS";

        float timeW = drawTime ? font.width(time) : 0.0f;
        float prefixW = brandFont.width(BRAND_PREFIX);
        float suffixW = brandFont.width(BRAND_SUFFIX);
        float brandW = prefixW + BRAND_GAP + suffixW;
        float fpsW = drawFps ? font.width(fps) : 0.0f;

        // Animated indicator dot at the very left of the pill
        float dotZoneW = 12.0f;

        float contentW = dotZoneW + brandW;
        if (drawTime) contentW += timeW + SEP_GAP * 2.0f + 1.0f;   // +separator
        if (drawFps) contentW += fpsW + SEP_GAP * 2.0f + 1.0f;     // +separator

        float totalWidth = contentW + PAD_X * 2.0f;
        float totalHeight = PILL_HEIGHT;

        float x = this.getX();
        float y = this.getY();

        // -------- ambient breathing pulse (independent of game ticks) --------
        long now = System.currentTimeMillis();
        float pulse = 0.5f + 0.5f * (float) Math.sin(now * 0.0028f);
        float chase = (now % 2400L) / 2400.0f; // 0..1 looping

        // -------- glow halo behind the pill --------
        if (this.glow.isEnabled() && !this.transparentBackground.isEnabled()) {
            ColorRGBA accent = theme.getColor();
            for (int i = 5; i >= 1; --i) {
                float sp = i * 1.8f + pulse * 1.6f;
                float a = (0.10f - i * 0.014f) * (0.7f + 0.3f * pulse);
                if (a <= 0.0f) continue;
                ctx.drawRoundedRect(x - sp, y - sp,
                        totalWidth + sp * 2.0f, totalHeight + sp * 2.0f,
                        BorderRadius.all(PILL_RADIUS + sp), accent.mulAlpha(a));
            }
        }

        // -------- pill body --------
        this.drawPill(ctx, x, y, totalWidth, totalHeight, theme);

        // -------- top sheen highlight (1px line at the top of the pill) --------
        if (!this.transparentBackground.isEnabled()) {
            ctx.drawRoundedRect(x + 4.0f, y + 0.5f, totalWidth - 8.0f, 0.6f,
                    BorderRadius.all(0.3f), SUBTLE_WHITE);
        }

        // -------- animated indicator dot + ring --------
        float dotR = 1.8f + 0.7f * pulse;
        float dotX = x + PAD_X - 2.0f + dotR;
        float dotY = y + totalHeight / 2.0f;
        ColorRGBA accent = theme.getColor();
        // outer ring
        float ringR = dotR + 2.0f + pulse * 2.0f;
        ctx.drawRoundedRect(dotX - ringR, dotY - ringR, ringR * 2.0f, ringR * 2.0f,
                BorderRadius.all(ringR), accent.mulAlpha(0.22f * (1.0f - pulse)));
        // core dot
        ctx.drawRoundedRect(dotX - dotR, dotY - dotR, dotR * 2.0f, dotR * 2.0f,
                BorderRadius.all(dotR), accent);

        // -------- content cursor --------
        float cursorX = x + PAD_X + dotZoneW;
        float centerY = y + totalHeight / 2.0f;

        if (drawTime) {
            float ty = centerY - font.height() / 2.0f + 0.2f;
            ctx.drawText(font, time, cursorX, ty, this.resolveTextColor(ColorRGBA.WHITE));
            cursorX += timeW + SEP_GAP;
            this.drawSeparator(ctx, cursorX, y, totalHeight);
            cursorX += SEP_GAP + 1.0f;
        }

        // EXOCLE in accent color, VISUALS in white
        float by = centerY - brandFont.height() / 2.0f + 0.15f;
        ctx.drawText(brandFont, BRAND_PREFIX, cursorX, by, this.resolveTextColor(accent));
        ctx.drawText(brandFont, BRAND_SUFFIX, cursorX + prefixW + BRAND_GAP, by,
                this.resolveTextColor(ColorRGBA.WHITE));
        cursorX += brandW;

        if (drawFps) {
            cursorX += SEP_GAP;
            this.drawSeparator(ctx, cursorX, y, totalHeight);
            cursorX += SEP_GAP + 1.0f;
            float fy = centerY - font.height() / 2.0f + 0.2f;
            // FPS gets a faint accent tint
            ColorRGBA fpsColor = this.resolveTextColor(ColorRGBA.WHITE);
            ctx.drawText(font, fps, cursorX, fy, fpsColor);
        }

        // -------- thin accent chase line under the pill --------
        if (!this.transparentBackground.isEnabled()) {
            float lineW = totalWidth - 16.0f;
            float lineY = y + totalHeight - 1.2f;
            // base dim line
            ctx.drawRoundedRect(x + 8.0f, lineY, lineW, 0.6f,
                    BorderRadius.all(0.3f), accent.mulAlpha(0.18f));
            // moving bright segment
            float segW = Math.min(lineW * 0.32f, 36.0f);
            float startX = x + 8.0f + (lineW - segW) * chase;
            ctx.drawRoundedRect(startX, lineY, segW, 0.6f,
                    BorderRadius.all(0.3f), accent.mulAlpha(0.85f));
        }

        // -------- tagline below pill (optional micro-text) --------
        // Always shown — tiny "// premium overlay" tagline keeps the brand consistent.
        String tag = "// premium overlay";
        float tagW = tagFont.width(tag);
        float tagX = x + (totalWidth - tagW) / 2.0f;
        float tagY = y + totalHeight + 1.0f;
        ctx.drawText(tagFont, tag, tagX, tagY,
                this.resolveTextColor(ColorRGBA.WHITE).mulAlpha(0.4f));

        this.setBounds(totalWidth, totalHeight + tagFont.height() + 1.0f);
    }

    private void drawPill(CustomDrawContext ctx, float x, float y, float width, float height, Theme theme) {
        BorderRadius radius = BorderRadius.all(PILL_RADIUS);
        ColorRGBA bg = theme.getBackgroundColor().mulAlpha(2.4f); // theme bg is usually low alpha
        ColorRGBA border = theme.getForegroundStroke();
        if (border == null) border = new ColorRGBA(255, 255, 255, 40);

        if (!this.transparentBackground.isEnabled()) {
            if (theme.isBlur()) {
                DrawUtil.drawBlur(ctx.getMatrices(), x, y, width, height, 8.0f,
                        radius, new ColorRGBA(255, 255, 255, 110));
            }
            // Solid dark fill
            ctx.drawRoundedRect(x, y, width, height, radius,
                    bg.getAlpha() <= 0 ? new ColorRGBA(10, 11, 14, 220) : bg);
            // Soft inner gradient using accent color (very low alpha tint)
            ctx.drawRoundedRect(x, y, width, height, radius,
                    theme.getColor().mulAlpha(0.06f));
        }
        ctx.drawRoundedBorder(x, y, width, height, 0.85f, radius, border);
    }

    private void drawSeparator(CustomDrawContext ctx, float x, float y, float pillH) {
        float lineHeight = pillH - 6.0f;
        float lineY = y + (pillH - lineHeight) / 2.0f;
        ctx.drawRoundedRect(x, lineY, 0.75f, lineHeight, BorderRadius.all(1.0f), SEPARATOR_COLOR);
    }

    private void migrateLegacySettings(JsonObject object) {
        if (object == null || !object.has("Settings") || !object.get("Settings").isJsonObject()) return;
        JsonObject settings = object.getAsJsonObject("Settings");
        this.migrateSettingKey(settings, LEGACY_SHOW_TIME_KEY, this.showTime.getName());
    }

    private void migrateSettingKey(JsonObject settings, String legacyName, String newName) {
        if (!settings.has(newName) && settings.has(legacyName)) {
            settings.add(newName, settings.get(legacyName).deepCopy());
        }
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (Watermark.mc.player == null || Watermark.mc.world == null) return;
        this.updateHud();
    }

    @EventTarget
    public void onRender(EventHudRender event) {
        if (Watermark.mc.player == null || Watermark.mc.world == null) return;
        this.renderHud(event.getContext());
    }

    @EventTarget
    public void onMouse(EventMouse event) {
        if (Watermark.mc.player == null || Watermark.mc.world == null) return;
        this.handleMouse(event);
    }
}
