
package vurst.visual.client.screens.menu.panels;

import lombok.Generated;
import net.minecraft.MathHelper;
import vurst.visual.base.animations.base.Animation;
import vurst.visual.base.animations.base.Easing;
import vurst.visual.base.font.Font;
import vurst.visual.base.font.Fonts;
import vurst.visual.client.modules.api.Category;
import vurst.visual.utility.render.display.base.UIContext;
import vurst.visual.utility.render.display.base.color.ColorRGBA;

public class SideBarCategory {
    private final Category category;
    private final Animation animationSwitch;

    public SideBarCategory(Category category) {
        this.category = category;
        this.animationSwitch = new Animation(200L, category == Category.MOVEMENT ? 1.0f : 0.0f, Easing.LINEAR);
    }

    public void render(UIContext ctx, float x, float y, float width, float height, float sidebarProgress, boolean selected, ColorRGBA textColor, ColorRGBA textColorDisable, ColorRGBA iconColorDisable, ColorRGBA primary) {
        this.animationSwitch.animateTo(selected ? 1.0f : 0.0f);
        this.animationSwitch.update();
        ColorRGBA mixColor = iconColorDisable.mix(primary, this.animationSwitch.getValue());
        ColorRGBA mixColorText = textColorDisable.mix(textColor, this.animationSwitch.getValue());
        Font font = Fonts.ICONS.getFont(7.0f);
        float offestY = (height - font.height()) / 2.0f;
        float scale = MathHelper.lerp((float)sidebarProgress, (float)1.0f, (float)0.8f);
        float iconWidth = font.width(this.category.getIcon());
        ctx.pushMatrix();
        ctx.getMatrices().translate(x + 8.0f + iconWidth / 2.0f, y + offestY + font.height() / 2.0f, 0.0f);
        ctx.getMatrices().scale(scale, scale, 1.0f);
        ctx.getMatrices().translate(-(x + 8.0f + iconWidth / 2.0f), -(y + offestY + font.height() / 2.0f), 0.0f);
        ctx.drawText(Fonts.ICONS.getFont(7.0f), this.category.getIcon(), x + 8.0f, y + offestY, mixColor);
        ctx.popMatrix();
        Font categoryFont = Fonts.MEDIUM.getFont(7.0f);
        ctx.drawText(categoryFont, this.category.getDisplayName(), x + 8.0f + iconWidth * scale + 6.0f, y + (height - font.height()) / 2.0f, mixColorText);
    }

    @Generated
    public Category getCategory() {
        return this.category;
    }
}

