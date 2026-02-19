package myau.ui.clickgui;

import org.lwjgl.opengl.GL11;

public class RoundedUtils {

    public static void drawRoundedRect(float x, float y, float width, float height, float radius, int color) {
        float r = (color >> 16 & 0xFF) / 255f;
        float g = (color >> 8 & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        float a = (color >> 24 & 0xFF) / 255f;

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(r, g, b, a);

        GL11.glBegin(GL11.GL_TRIANGLE_FAN);

        // Center
        GL11.glVertex2f(x + width / 2, y + height / 2);

        // Edges
        for (int i = 0; i <= 360; i += 5) {
            double angle = Math.toRadians(i);
            float dx = (float) (Math.cos(angle) * radius);
            float dy = (float) (Math.sin(angle) * radius);

            float px = Math.min(Math.max(x + radius + dx, x), x + width);
            float py = Math.min(Math.max(y + radius + dy, y), y + height);

            GL11.glVertex2f(px, py);
        }

        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }
}
