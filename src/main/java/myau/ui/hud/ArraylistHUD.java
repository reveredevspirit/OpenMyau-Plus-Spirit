package myau.ui.hud;

import myau.Myau;
import myau.module.Module;
import myau.module.modules.HUD;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.lwjgl.opengl.GL11;

public class ArraylistHUD {

    private final Minecraft mc = Minecraft.getMinecraft();

    public void render() {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        ScaledResolution sr = new ScaledResolution(mc);
        HUD hudModule = (HUD) Myau.moduleManager.getModule(HUD.class);

        // Optional: only render if the HUD module exists and is enabled
        if (hudModule == null || !hudModule.isEnabled()) {
            return;
        }

        // Collect enabled modules once
        List<Module> enabled = new ArrayList<>(Myau.moduleManager.modules.values());
        enabled.removeIf(m -> !m.isEnabled());

        // Sort by width descending (longest names at the top)
        enabled.sort(Comparator.comparingInt(m -> -mc.fontRendererObj.getStringWidth(m.getName())));

        int y = 4;                    // starting y position (top of screen)
        int spacing = mc.fontRendererObj.FONT_HEIGHT + 2;

        for (int i = 0; i < enabled.size(); i++) {
            Module module = enabled.get(i);
            String name = module.getName();  // ← you can later do module.getDisplayName() if you add suffixes

            int textWidth = mc.fontRendererObj.getStringWidth(name);
            float x = sr.getScaledWidth() - textWidth - 4;  // 4px padding from right edge

            // Get color with offset for nicer rainbow gradient effect
            Color color = hudModule.getColor(System.currentTimeMillis() + (i * 150L));

            // Draw semi-transparent black background behind text
            drawBackground(
                (int) x - 3,
                y - 1,
                textWidth + 6,
                spacing - 1,
                new Color(0, 0, 0, 90).getRGB()
            );

            // Draw module name with shadow
            mc.fontRendererObj.drawStringWithShadow(
                name,
                x,
                y,
                color.getRGB()
            );

            y += spacing;
        }
    }

    private void drawBackground(int x, int y, int width, int height, int color) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        float a = (color >> 24 & 255) / 255.0f;
        float r = (color >> 16 & 255) / 255.0f;
        float g = (color >> 8  & 255) / 255.0f;
        float b = (color       & 255) / 255.0f;

        GL11.glColor4f(r, g, b, a * 0.85f);  // slight extra transparency control

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x + width, y);
        GL11.glVertex2f(x + width, y + height);
        GL11.glVertex2f(x, y + height);
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }
}
