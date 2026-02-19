package myau.ui.clickgui;

import myau.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import java.util.ArrayList;
import java.util.List;

public class ModulePanel {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private SidebarCategory category;
    private final List<Module> modules = new ArrayList<>();

    // Scroll
    private int scrollOffset = 0;
    private int maxScroll = 0;

    // Layout
    private final int moduleHeight = 20;
    private final int spacing = 4;

    public ModulePanel(SidebarCategory category) {
        setCategory(category);
    }

    public void setCategory(SidebarCategory category) {
        this.category = category;
        modules.clear();
        modules.addAll(category.getModules()); // <-- Correct for your client
    }

    public void render(int x, int y, int mouseX, int mouseY, String searchText) {

        // Panel background
        Gui.drawRect(x, y, x + 300, y + 200, 0xAA111111);

        int offsetY = y + 10 - scrollOffset;

        // Render modules
        for (Module m : modules) {

            // Search filter
            if (!searchText.isEmpty() &&
                !m.getName().toLowerCase().contains(searchText.toLowerCase())) {
                continue;
            }

            int boxX1 = x + 10;
            int boxX2 = x + 290;
            int boxY1 = offsetY;
            int boxY2 = offsetY + moduleHeight;

            // Module background
            Gui.drawRect(boxX1, boxY1, boxX2, boxY2, 0xFF1E1E1E);

            // Hover highlight
            if (mouseX >= boxX1 && mouseX <= boxX2 &&
                mouseY >= boxY1 && mouseY <= boxY2) {
                Gui.drawRect(boxX1, boxY1, boxX2, boxY2, 0x22FFFFFF);
            }

            // Module name
            mc.fontRendererObj.drawString(
                    m.getName(),
                    boxX1 + 5,
                    boxY1 + 6,
                    m.isEnabled() ? 0xFF55AAFF : 0xFFFFFFFF
            );

            offsetY += moduleHeight + spacing;
        }

        // Calculate scroll limit
        maxScroll = Math.max(0, (offsetY - y - 200));
    }

    public void mouseClicked(int mouseX, int mouseY, int button) {

        int x = 130;
        int y = 60;

        int offsetY = y + 10 - scrollOffset;

        for (Module m : modules) {

            int boxX1 = x + 10;
            int boxX2 = x + 290;
            int boxY1 = offsetY;
            int boxY2 = offsetY + moduleHeight;

            if (mouseX >= boxX1 && mouseX <= boxX2 &&
                mouseY >= boxY1 && mouseY <= boxY2) {

                if (button == 0) {
                    // Left click = toggle
                    m.toggle();
                }

                if (button == 1) {
                    // Right click = open settings (future)
                    // TODO: settings panel
                }

                return;
            }

            offsetY += moduleHeight + spacing;
        }
    }

    public void mouseReleased(int mouseX, int mouseY, int button) {
        // Reserved for sliders, color pickers, etc.
    }

    public void keyTyped(char typedChar, int keyCode) {
        // Reserved for text input inside module settings
    }

    public void handleScroll(int mouseX, int mouseY, int wheel) {
        if (wheel != 0) {
            scrollOffset -= wheel / 10;
            scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
        }
    }
}
