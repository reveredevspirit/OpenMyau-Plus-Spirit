package myau.ui.clickgui;

import myau.config.GuiConfig;
import myau.module.Module;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Rise6ClickGui extends GuiScreen {

    private final List<SidebarCategory> categories = new ArrayList<>();
    private SidebarCategory selectedCategory;

    private float openAnim = 0f;

    private SearchBar searchBar;
    private ModulePanel modulePanel;

    // ----------------------------------------------------------------
    // DRAG STATE
    // ----------------------------------------------------------------
    private boolean dragging = false;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;

    private int posX;
    private int posY;

    private static final int SIDEBAR_WIDTH   = 120;
    private static final int PANEL_WIDTH     = 220;
    private static final int DRAG_BAR_HEIGHT = 16;
    private static final int GAP             = 0; // no gap, they're joined

    // Total combined width
    private static final int TOTAL_WIDTH = SIDEBAR_WIDTH + PANEL_WIDTH;

    public Rise6ClickGui(
            List<Module> combatModules,
            List<Module> movementModules,
            List<Module> playerModules,
            List<Module> renderModules,
            List<Module> miscModules
    ) {
        categories.add(new SidebarCategory("Combat",   combatModules));
        categories.add(new SidebarCategory("Movement", movementModules));
        categories.add(new SidebarCategory("Player",   playerModules));
        categories.add(new SidebarCategory("Render",   renderModules));
        categories.add(new SidebarCategory("Misc",     miscModules));

        selectedCategory = categories.get(0);

        searchBar   = new SearchBar();
        modulePanel = new ModulePanel(selectedCategory);

        GuiConfig.load();
        posX = GuiConfig.guiX;
        posY = GuiConfig.guiY;
    }

    @Override
    public void initGui() {
        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        ScaledResolution sr = new ScaledResolution(mc);

        openAnim += (1f - openAnim) * 0.15f;
        int guiAlpha = (int)(150 * openAnim);

        // Subtle dark overlay
        drawRect(0, 0, sr.getScaledWidth(), sr.getScaledHeight(), (guiAlpha << 24));

        int panelHeight = Math.max(modulePanel.getContentHeight() + 60, categories.size() * 28 + 20);

        // ----------------------------------------------------------------
        // OUTER BACKGROUND — one big rounded rect for the whole GUI
        // ----------------------------------------------------------------
        RoundedUtils.drawRoundedRect(posX, posY, TOTAL_WIDTH, panelHeight, 10, 0xF0080808);

        // ----------------------------------------------------------------
        // SIDEBAR — dark section on the left
        // ----------------------------------------------------------------
        // Slightly lighter shade to distinguish sidebar from module panel
        RoundedUtils.drawRoundedRect(posX, posY, SIDEBAR_WIDTH, panelHeight, 10, 0xF0111111);

        // Title at top of sidebar
        mc.fontRendererObj.drawString("§b§lMyau", posX + 10, posY + 8, 0xFFFFFFFF);

        // Divider line between sidebar and panel
        drawRect(posX + SIDEBAR_WIDTH, posY + 5, posX + SIDEBAR_WIDTH + 1, posY + panelHeight - 5, 0xFF222222);

        // Category entries
        int yOffset = posY + 28;
        for (SidebarCategory cat : categories) {
            boolean selected = selectedCategory == cat;

            // Blue highlight for selected category
            if (selected) {
                RoundedUtils.drawRoundedRect(posX + 6, yOffset - 2, SIDEBAR_WIDTH - 12, 20, 4, 0xFF1A3A5C);
                // Blue left accent bar
                RoundedUtils.drawRoundedRect(posX + 4, yOffset, 3, 14, 1, 0xFF55AAFF);
            }

            // Hover highlight
            boolean hovered = mouseX >= posX + 6 && mouseX <= posX + SIDEBAR_WIDTH - 6 &&
                               mouseY >= yOffset - 2 && mouseY <= yOffset + 18;
            if (hovered && !selected) {
                RoundedUtils.drawRoundedRect(posX + 6, yOffset - 2, SIDEBAR_WIDTH - 12, 20, 4, 0xFF1A1A1A);
            }

            int textColor = selected ? 0xFF55AAFF : (hovered ? 0xFFCCCCCC : 0xFF888888);
            mc.fontRendererObj.drawString(cat.getName(), posX + 14, yOffset + 3, textColor);

            yOffset += 24;
        }

        // ----------------------------------------------------------------
        // MAIN PANEL — search + modules
        // ----------------------------------------------------------------
        int panelX = posX + SIDEBAR_WIDTH + 8;

        // Search bar
        searchBar.render(panelX, posY + 10, mouseX, mouseY);

        // Divider under search
        drawRect(panelX, posY + 30, posX + TOTAL_WIDTH - 8, posY + 31, 0xFF222222);

        // Module list
        modulePanel.render(panelX, posY + 38, mouseX, mouseY, searchBar.getText());

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        GuiConfig.guiX = posX;
        GuiConfig.guiY = posY;
        GuiConfig.save();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {

        // Drag — clicking anywhere on the top bar of the whole GUI
        if (button == 0 &&
            mouseX >= posX && mouseX <= posX + TOTAL_WIDTH &&
            mouseY >= posY && mouseY <= posY + DRAG_BAR_HEIGHT) {

            dragging = true;
            dragOffsetX = mouseX - posX;
            dragOffsetY = mouseY - posY;
            return;
        }

        // Sidebar category clicks
        int yOffset = posY + 28;
        for (SidebarCategory cat : categories) {
            if (mouseX >= posX + 6 && mouseX <= posX + SIDEBAR_WIDTH - 6 &&
                mouseY >= yOffset - 2 && mouseY <= yOffset + 18) {
                selectedCategory = cat;
                modulePanel.setCategory(cat);
                return;
            }
            yOffset += 24;
        }

        int panelX = posX + SIDEBAR_WIDTH + 8;
        searchBar.mouseClicked(mouseX, mouseY, button);
        modulePanel.mouseClicked(panelX, posY + 38, mouseX, mouseY, button);
    }

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (dragging) {
            ScaledResolution sr = new ScaledResolution(mc);
            posX = Math.max(0, Math.min(sr.getScaledWidth() - TOTAL_WIDTH, mouseX - dragOffsetX));
            posY = Math.max(0, Math.min(sr.getScaledHeight() - 100,        mouseY - dragOffsetY));
        } else {
            modulePanel.mouseClickMove(mouseX);
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        dragging = false;
        modulePanel.mouseReleased();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            mc.displayGuiScreen(null);
            return;
        }
        if (searchBar.keyTyped(typedChar, keyCode)) return;
        modulePanel.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
