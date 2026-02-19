package myau.ui.clickgui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

public class SidebarCategory {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private final String name;

    // Sidebar layout
    private final int width = 100;
    private final int height = 22;

    public SidebarCategory(String name) {
        this.name = name;
    }

    public void render(int x, int y, int mouseX, int mouseY, boolean selected) {

        // Background
        Gui.drawRect(x, y, x + width, y + height, 0xFF1A1A1A);

        // Hover highlight
        if (mouseX >= x && mouseX <= x + width &&
            mouseY >= y && mouseY <= y + height) {
            Gui.drawRect(x, y, x + width, y + height, 0x22FFFFFF);
        }

        // Selected highlight (Rise 6 blue)
        if (selected) {
            Gui.drawRect(x, y, x + 3, y + height, 0xFF55AAFF);
        }

        // Category text
        mc.fontRendererObj.drawString(
                name,
                x + 12,
                y + 7,
                selected ? 0xFF55AAFF : 0xFFFFFFFF
        );
    }

    public boolean mouseClicked(int mouseX, int mouseY, int button) {

        int x = 10; // Sidebar X (matches Rise6ClickGui)
        int y = 0;  // Y is passed dynamically in render()

        // We don't know the Y here, so click detection is handled in Rise6ClickGui
        // This method only returns true if Rise6ClickGui already confirmed the click area.

        return button == 0;
    }

    public String getName() {
        return name;
    }
}
