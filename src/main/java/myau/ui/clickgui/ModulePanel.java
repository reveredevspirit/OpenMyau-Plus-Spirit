package myau.ui.clickgui;

import myau.module.BooleanSetting;
import myau.module.KeybindSetting;
import myau.module.Module;
import myau.module.Setting;
import myau.module.SliderSetting;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;

public class ModulePanel {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private SidebarCategory category;

    private final Map<Module, Float> toggleAnim = new HashMap<>();
    private Module expandedModule = null;

    private SliderSetting draggingSlider = null;
    private int sliderRenderX = 0;
    private int sliderRenderWidth = 0;

    private KeybindSetting listeningKeybind = null;

    // Scrolling
    private int scrollOffset = 0;
    private int visibleHeight = 200;

    public ModulePanel(SidebarCategory category) {
        this.category = category;
    }

    public void setCategory(SidebarCategory category) {
        this.category = category;
        expandedModule = null;
        scrollOffset = 0;
    }

    public void setVisibleHeight(int h) {
        this.visibleHeight = Math.max(50, h);
    }

    // ----------------------------------------------------------------
    // SCROLL
    // ----------------------------------------------------------------
    public void handleScroll(int delta) {
        int maxScroll = Math.max(0, getContentHeight() - visibleHeight);
        scrollOffset -= delta / 5;
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset));
    }

    // ----------------------------------------------------------------
    // RENDER
    // ----------------------------------------------------------------
    public void render(int x, int y, int mouseX, int mouseY, String search) {
        int offsetY = y - scrollOffset;
        int clipTop    = y;
        int clipBottom = y + visibleHeight;

        ScissorUtil.enable(x, clipTop, 200, visibleHeight);

        for (Module module : category.getModules()) {
            if (search != null && !search.isEmpty()) {
                if (!module.getName().toLowerCase().contains(search.toLowerCase())) continue;
            }

            int width  = 160;
            int height = 16;

            // Skip if completely out of visible area
            if (offsetY + height < clipTop || offsetY > clipBottom) {
                offsetY += height + 1;
                if (expandedModule == module) offsetY += getSettingsHeight(module);
                continue;
            }

            boolean hovered =
                    mouseX >= x && mouseX <= x + width &&
                    mouseY >= offsetY && mouseY <= offsetY + height;

            // Row background
            int rowColor;
            if (module.isEnabled())  rowColor = 0xFF0D2137;
            else if (hovered)        rowColor = 0xFF2A2A2A;
            else                     rowColor = 0xFF1A1A1A;
            RoundedUtils.drawRoundedRect(x, offsetY, width, height, 4, rowColor);

            if (module.isEnabled()) {
                drawSolidRect(x, offsetY, x + 3, offsetY + height, 0xFF55AAFF);
            }

            int nameColor = module.isEnabled() ? 0xFF55AAFF : (hovered ? 0xFFCCCCCC : 0xFFFFFFFF);
            GL11.glColor4f(1f, 1f, 1f, 1f);
            mc.fontRendererObj.drawString(module.getName(), x + 7, offsetY + 5, nameColor);

            // Toggle animation
            boolean enabled = module.isEnabled();
            toggleAnim.putIfAbsent(module, enabled ? 1f : 0f);
            float anim = toggleAnim.get(module);
            anim += ((enabled ? 1f : 0f) - anim) * 0.2f;
            toggleAnim.put(module, anim);

            int toggleX = x + width - 26;
            int toggleY = offsetY + 4;
            RoundedUtils.drawRoundedRect(toggleX, toggleY, 20, 8, 4, blend(0xFF555555, 0xFF55AAFF, anim));
            RoundedUtils.drawRoundedRect(toggleX + 2 + (int)(anim * 8), toggleY + 1, 6, 6, 3, 0xFFFFFFFF);

            if (!module.getSettings().isEmpty()) {
                String arrow = expandedModule == module ? "v" : ">";
                GL11.glColor4f(1f, 1f, 1f, 1f);
                mc.fontRendererObj.drawString(arrow, x + width - 38, offsetY + 5, 0xFF888888);
            }

            offsetY += height + 1;

            // ----------------------------------------------------------------
            // SETTINGS
            // -----------
