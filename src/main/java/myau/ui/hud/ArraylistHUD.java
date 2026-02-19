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
        if (mc.thePlayer == null || mc.theWorld == null) return;
        // Don't draw inside any GUI / ClickGUI / chat / etc.
        if (mc.currentScreen != null) return;

        HUD hud = (HUD) Myau.moduleManager.getModule(HUD.class);
        if (hud == null || !hud.isEnabled()) return;

        ScaledResolution sr = new ScaledResolution(mc);

        // Collect currently enabled modules
        List<Module> active = new ArrayList<>(Myau.moduleManager.modules.values());
        active.removeIf(m -> !m.isEnabled());

        // Sort by name width descending → longest at top (classic look)
        active.sort(Comparator.comparingInt(m -> -mc.fontRendererObj.getStringWidth(m.getName())));

        int y = 3;                          // starting y (small top padding)
        int lineHeight = mc.fontRendererObj.FONT_HEIGHT + 2;  // spacing between lines

        for (int i = 0; i < active.size(); i++) {
            Module mod = active.get(i);
            String text = mod.getName();    // you can do mod.getDisplayName() later if you add suffixes

            int textWidth = mc.fontRendererObj.getStringWidth(text);

            // Get animation value (0 = fully out, 1 = fully in)
            float anim = mod.getAnimationProgress();

            // Slide from right (60 pixels distance feels nice)
            float slide = (1.0f - anim) * 60.0f;

            float x = sr.getScaledWidth() - textWidth - 5 - slide;  // 5px right padding

            // Color from HUD module (with offset per line for gradient/rainbow effect)
            Color c = hud.getColor(System.currentTimeMillis() + i * 180L);

            // Fade with animation
            int alpha = (int) (255 * anim);
            int textRGB = (c.getRGB() & 0x00FFFFFF) | (alpha << 24);

            // Background - dark semi-transparent, also fades
            int bgAlpha = (int) (100 * anim);
            drawRect(
                    (int) x - 4,
                    y - 1,
                    (int) x + textWidth + 4,
                    y + mc.fontRendererObj.FONT_HEIGHT + 1,
                    (bgAlpha << 24) | 0x0A0A0A   // very dark gray/black
            );

            // Shadowed text
            mc.fontRendererObj.drawStringWithShadow(text, x, y, textRGB);

            y += lineHeight;
        }
    }

    // Simple GL quad drawer (handles left/right/top/bottom in any order)
    private void drawRect(int left, int top, int right, int bottom, int color) {
        if (left < right) {
            int temp = left;
            left = right;
            right = temp;
        }
        if (top < bottom) {
            int temp = top;
            top = bottom;
            bottom = temp;
        }

        float a = ((color >> 24) & 255) / 255.0f;
        float r = ((color >> 16) & 255) / 255.0f;
        float g = ((color >> 8) & 255) / 255.0f;
        float b = (color & 255) / 255.0f;

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glColor4f(r, g, b, a);

        GL11.glBegin(GL11.GL_QUADS);
        {
            GL11.glVertex2f(left, top);
            GL11.glVertex2f(right, top);
            GL11.glVertex2f(right, bottom);
            GL11.glVertex2f(left, bottom);
        }
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);

        // Reset color to white (prevents tinting other things)
        GL11.glColor4f(1, 1, 1, 1);
    }
}
