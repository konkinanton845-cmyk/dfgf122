
package vurst.visual.client.screens.menu.settings.impl.popup;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.Items;
import net.minecraft.Blocks;
import net.minecraft.Block;
import net.minecraft.Vector2f;
import vurst.visual.base.font.Font;
import vurst.visual.base.font.Fonts;
import vurst.visual.base.theme.Theme;
import vurst.visual.client.modules.api.setting.impl.ItemSelectSetting;
import vurst.visual.client.screens.menu.settings.api.MenuPopupSetting;
import vurst.visual.utility.game.other.MouseButton;
import vurst.visual.utility.math.MathUtil;
import vurst.visual.utility.render.display.ScrollHandler;
import vurst.visual.utility.render.display.TextBox;
import vurst.visual.utility.render.display.base.BorderRadius;
import vurst.visual.utility.render.display.base.ChangeRect;
import vurst.visual.utility.render.display.base.Rect;
import vurst.visual.utility.render.display.base.UIContext;
import vurst.visual.utility.render.display.base.color.ColorRGBA;

public class MenuItemPopupSetting
extends MenuPopupSetting {
    private final TextBox searchBox;
    private final ItemSelectSetting setting;
    private final ScrollHandler scrollHandler = new ScrollHandler();
    private boolean rebornSort = false;
    private Map<Block, Rect> itemBounds = new HashMap<Block, Rect>();

    public MenuItemPopupSetting(ItemSelectSetting setting, ChangeRect bounds) {
        super(bounds);
        this.searchBox = new TextBox(new Vector2f(0.0f, 0.0f), Fonts.MEDIUM.getFont(7.0f), "Поиск...", 78.0f);
        this.animationScale.update(1.0f);
        this.setting = setting;
    }

    @Override
    public void render(UIContext ctx, float mouseX, float mouseY, float alphas, Theme theme) {
        this.animationScale.update();
        alphas = 1.0f;
        float x = this.bounds.getX();
        float y = this.bounds.getY();
        float width = this.bounds.getWidth();
        float height = this.bounds.getHeight() - 20.0f - 4.0f;
        ctx.pushMatrix();
        ctx.getMatrices().translate(this.bounds.getX(), this.bounds.getY() + this.bounds.getHeight() / 2.0f, 0.0f);
        ctx.getMatrices().scale(this.animationScale.getValue(), this.animationScale.getValue(), 1.0f);
        ctx.getMatrices().translate(-this.bounds.getX(), -(this.bounds.getY() + this.bounds.getHeight() / 2.0f), 0.0f);
        ctx.drawRoundedRect(this.bounds.getX(), this.bounds.getY(), this.bounds.getWidth(), height, BorderRadius.all(4.0f), theme.getForegroundColor().mulAlpha(alphas));
        ctx.drawRoundedRect(this.bounds.getX(), this.bounds.getY(), this.bounds.getWidth(), 18.0f, BorderRadius.top(4.0f, 4.0f), theme.getForegroundLight().mulAlpha(alphas));
        Font itemFont = Fonts.MEDIUM.getFont(7.0f);
        Font iconFont = Fonts.ICONS.getFont(7.0f);
        ctx.drawText(itemFont, this.setting.getDisplayName(), x + 8.0f + 11.2f + 3.0f, y + 7.55f, theme.getWhite());
        ctx.pushMatrix();
        ctx.getMatrices().translate(x + 8.0f, y + 4.4f, 0.0f);
        ctx.getMatrices().scale(0.7f, 0.7f, 1.0f);
        ctx.drawItem(Items.TOTEM_OF_UNDYING.getDefaultStack(), 0, 0);
        ctx.popMatrix();
        float sortSize = 14.0f;
        ctx.drawRoundedRect(x + width - sortSize - 8.0f, y + 3.0f, sortSize, sortSize, BorderRadius.all(2.0f), theme.getForegroundGray().mulAlpha(alphas));
        ctx.drawText(iconFont, "W", x + width - 8.0f - sortSize + (sortSize - iconFont.width("W")) / 2.0f + 1.0f, y + 6.6f, theme.getColor());
        List<Block> sortedList = this.searchBox.isEmpty() && this.rebornSort ? MenuItemPopupSetting.getAllBlocks().toList() : MenuItemPopupSetting.getAllBlocks().sorted((o1, o2) -> {
            if (this.searchBox.isEmpty()) {
                boolean containsInSetting1 = this.setting.contains((Block)o1);
                boolean containsInSetting2 = this.setting.contains((Block)o2);
                return Boolean.compare(!containsInSetting1, !containsInSetting2);
            }
            String query = this.searchBox.getText().toLowerCase().trim();
            String name1 = o1.getTranslationKey().replaceFirst("^block\\.minecraft\\.", "").replaceAll("_", " ");
            String name2 = o2.getTranslationKey().replaceFirst("^block\\.minecraft\\.", "").replaceAll("_", " ");
            boolean matchesSearch1 = name1.toLowerCase().contains(query);
            boolean matchesSearch2 = name2.toLowerCase().contains(query);
            return Boolean.compare(!matchesSearch1, !matchesSearch2);
        }).toList();
        float contentHeight = (float)sortedList.size() * 20.0f;
        this.scrollHandler.setMax(Math.max(0.0f, contentHeight - height));
        this.scrollHandler.update();
        int padding = 4;
        float itemY = (float)(padding + 18) + y - (float)this.scrollHandler.getValue();
        float itemX = x;
        float itemWidth = width;
        ColorRGBA textColor = theme.getWhite().mulAlpha(alphas);
        ColorRGBA bgColor = theme.getColor().mulAlpha(alphas);
        this.itemBounds.clear();
        ColorRGBA graySlotColor = theme.getForegroundColor();
        ColorRGBA themeSlotColor = theme.getForegroundLight();
        ctx.enableScissor((int)x, (int)y + 18 + padding, (int)(x + width), (int)(y + height - (float)padding));
        int i = 0;
        for (Block item : sortedList) {
            if (item == Blocks.AIR) continue;
            ++i;
            if (itemY < y) {
                itemY += 20.0f;
                continue;
            }
            boolean selected = this.setting.contains(item);
            Rect rect = new Rect(itemX, itemY, itemWidth, 20.0f);
            ctx.drawRoundedRect(rect.x(), rect.y(), rect.width(), rect.height(), BorderRadius.ZERO, selected ? bgColor : (i % 2 == 0 ? graySlotColor : themeSlotColor));
            this.itemBounds.put(item, rect);
            Object name = item.getTranslationKey().replaceFirst("^block\\.minecraft\\.", "").replaceAll("_", " ");
            name = ((String)name).substring(0, 1).toUpperCase() + ((String)name).substring(1);
            ctx.pushMatrix();
            ctx.getMatrices().translate(itemX + 8.0f, itemY + 4.4f, 0.0f);
            ctx.getMatrices().scale(0.7f, 0.7f, 1.0f);
            ctx.drawItem(item.asItem().getDefaultStack(), 0, 0);
            ctx.popMatrix();
            ctx.drawText(Fonts.BOLD.getFont(8.0f), ".", itemX + 8.0f + 11.2f + 3.0f, itemY + 5.0f, theme.getWhiteGray());
            ctx.drawText(itemFont, (String)name, itemX + 8.0f + 11.2f + 8.0f, itemY + 7.55f, selected ? textColor : theme.getGrayLight());
            if (!((itemY += 20.0f) > y + height)) continue;
            break;
        }
        ctx.disableScissor();
        ctx.enableScissor((int)x, (int)(y + height + 4.0f), (int)(x + width), (int)(y + height + 24.0f));
        ctx.drawRoundedRect(x, y + height + 4.0f, width, 20.0f, BorderRadius.all(4.0f), theme.getForegroundColor().mulAlpha(alphas));
        this.searchBox.setWidth(width - 20.0f);
        this.searchBox.render(ctx, x + 8.0f, y + height + 4.0f + 8.0f, theme.getWhite().mulAlpha(alphas), theme.getGray().mulAlpha(alphas));
        this.searchBox.setMaxLength(35);
        ctx.disableScissor();
        ctx.popMatrix();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return this.searchBox.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return this.searchBox.charTyped(chr, modifiers);
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        this.searchBox.onMouseClicked(mouseX, mouseY, button);
        float x = this.bounds.getX();
        float y = this.bounds.getY();
        float width = this.bounds.getWidth();
        float height = this.bounds.getHeight();
        if (mouseY > (double)(y + 18.0f)) {
            for (Map.Entry<Block, Rect> entry : this.itemBounds.entrySet()) {
                if (!entry.getValue().contains(mouseX, mouseY)) continue;
                if (this.setting.contains(entry.getKey())) {
                    this.setting.remove(entry.getKey());
                } else {
                    this.setting.add(entry.getKey());
                }
                return true;
            }
        }
        if (MathUtil.isHovered(mouseX, mouseY, x + width - 8.0f - 16.0f, y + 3.0f, 16.0, 16.0)) {
            this.rebornSort = !this.rebornSort;
            this.scrollHandler.setTargetValue(0.0);
            return true;
        }
        return this.bounds.contains(mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        this.scrollHandler.scroll(verticalAmount);
        return true;
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

    public static Stream<Block> getAllBlocks() {
        return Stream.of(Blocks.class.getDeclaredFields()).filter(field -> Modifier.isStatic(field.getModifiers())).filter(field -> Modifier.isPublic(field.getModifiers())).filter(field -> Block.class.isAssignableFrom(field.getType())).map(field -> {
            try {
                return (Block)field.get(null);
            }
            catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof MenuItemPopupSetting) {
            MenuItemPopupSetting that = (MenuItemPopupSetting)o;
            return this.setting == that.setting;
        }
        return false;
    }

    public int hashCode() {
        return Objects.hashCode(this.setting);
    }
}

