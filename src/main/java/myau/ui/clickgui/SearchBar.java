package myau.ui.clickgui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

public class SearchBar {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private String text = "";
    private boolean focused = false;

    // Caret blink
    private long lastBlink = 0;
    private boolean showCaret = true;

    public void render(int x, int y, int mouseX, int mouseY) {

        int width = 260;
        int height = 18;

        // Background
        Gui.drawRect(x, y, x + width, y + height, 0xFF1A1A1A);

        // Hover highlight
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
            Gui.drawRect(x, y, x + width, y + height, 0x11FFFFFF);
        }

        // Border when focused
        if (focused) {
            Gui.drawRect(x - 1, y - 1, x + width + 1, y + height + 1, 0xFF55AAFF);
        }

        // Text
        mc.fontRendererObj.drawString(
                text.isEmpty() && !focused ? "Search..." : text,
                x + 4,
                y + 5,
                text.isEmpty() && !focused ? 0xFF777777 : 0xFFFFFFFF
        );

        // Caret blinking
        if (focused) {
            if (System.currentTimeMillis() - lastBlink > 500) {
                showCaret = !showCaret;
                lastBlink = System.currentTimeMillis();
            }

            if (showCaret) {
                int caretX = x + 4 + mc.fontRendererObj.getStringWidth(text);
                Gui.drawRect(caretX, y + 4, caretX + 1, y + height - 4, 0xFFFFFFFF);
            }
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int button) {

        int x = 130;
        int y = 30;
        int width = 260;
        int height = 18;

        boolean inside =
                mouseX >= x && mouseX <= x + width &&
                mouseY >= y && mouseY <= y + height;

        if (inside && button == 0) {
            focused = true;
        } else if (button == 0) {
            focused = false;
        }
    }

    public boolean keyTyped(char typedChar, int keyCode) {

        if (!focused) return false;

        // ESC handled by main GUI
        // Backspace
        if (keyCode == 14) {
            if (!text.isEmpty()) {
                text = text.substring(0, text.length() - 1);
            }
            return true;
        }

        // Allowed characters
        if (Character.isLetterOrDigit(typedChar) || typedChar == ' ') {
            text += typedChar;
            return true;
        }

        return false;
    }

    public String getText() {
        return text;
    }
}
