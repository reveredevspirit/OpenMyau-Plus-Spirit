package myau.ui.clickgui;

import myau.module.KeybindSetting;
import myau.module.Module;
import myau.module.Setting;
import myau.module.SliderSetting;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

import java.util.HashMap;
import java.util.Map;

public class ModulePanel {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private SidebarCategory category;

    // Toggle animation per module
    private final Map<Module, Float> toggleAnim = new HashMap<>();

    // Which module is expanded (settings visible)
    private Module expandedModule = null;

    // Slider drag state
    private SliderSetting draggingSlider = null;
    private int sliderRenderX = 0;
    private int sliderRenderWidth = 0;

    // Keybind listening state
    private KeybindSetting listeningKeybind = null;

    public ModulePanel(SidebarCategory category) {
        this.category = category;
    }

    public void setCategory(SidebarCategory category) {
        this.category = category;
        expandedModule = null;
    }

    // ----------------------------------------------------------------
    // RENDER
    // ----------------------------------------------------------------
    public void render(int x, int y, int mouseX, int mouseY, String search) {
        int offsetY = y;

        for (Module module : category.getModules()) {
            if (search != null && !search.isEmpty()) {
                if (!module.getName().toLowerCase().contains(search.toLowerCase())) continue;
            }

            int width = 160;
            int height = 18;

            boolean hovered =
                    mouseX >= x && mouseX <= x + width &&
                    mouseY >= offsetY && mouseY <= offsetY + height;

            // Module row background
            int bg = hovered ? 0xFF2A2A2A : 0xFF1A1A1A;
            RoundedUtils.drawRoundedRect(x, offsetY, width, height, 5, bg);

            // Module name
            mc.fontRendererObj.drawString(module.getName(), x + 6, offsetY + 6, 0xFFFFFFFF);

            // Toggle switch animation
            boolean enabled = module.isEnabled();
            toggleAnim.putIfAbsent(module, enabled ? 1f : 0f);
            float anim = toggleAnim.get(module);
            anim += ((enabled ? 1f : 0f) - anim) * 0.2f;
            toggleAnim.put(module, anim);

            int toggleX = x + width - 28;
            int toggleY = offsetY + 4;
            int blendedColor = blend(0xFF555555, 0xFF55AAFF, anim);
            RoundedUtils.drawRoundedRect(toggleX, toggleY, 22, 10, 5, blendedColor);
            RoundedUtils.drawRoundedRect(toggleX + 2 + (int)(anim * 10), toggleY + 1, 8, 8, 4, 0xFFFFFFFF);

            // Expand arrow (shows if module has settings)
            if (!module.getSettings().isEmpty()) {
                boolean expanded = expandedModule == module;
                String arrow = expanded ? "v" : ">";
                mc.fontRendererObj.drawString(arrow, x + width - 40, offsetY + 6, 0xFF888888);
            }

            offsetY += height + 2;

            // Settings rows (only for expanded module)
            if (expandedModule == module) {
                for (Setting setting : module.getSettings()) {

                    // Settings background
                    RoundedUtils.drawRoundedRect(x + 8, offsetY, width - 8, 20, 4, 0xFF222222);

                    // Setting name
                    mc.fontRendererObj.drawString(setting.getName(), x + 14, offsetY + 3, 0xFFAAAAAA);

                    if (setting instanceof SliderSetting) {
                        SliderSetting slider = (SliderSetting) setting;

                        int barX = x + 14;
                        int barY = offsetY + 12;
                        int barW = width - 28;
                        int barH = 4;

                        // Track
                        RoundedUtils.drawRoundedRect(barX, barY, barW, barH, 2, 0xFF444444);
                        // Fill
                        int fillW = (int)(barW * slider.getPercent());
                        if (fillW > 0)
                            RoundedUtils.drawRoundedRect(barX, barY, fillW, barH, 2, 0xFF55AAFF);
                        // Knob
                        int knobX = barX + fillW - 3;
                        RoundedUtils.drawRoundedRect(knobX, barY - 2, 6, 8, 3, 0xFFFFFFFF);

                        // Value label
                        String valStr = formatDouble(slider.getValue());
                        mc.fontRendererObj.drawString(valStr, x + width - mc.fontRendererObj.getStringWidth(valStr) - 6, offsetY + 3, 0xFF55AAFF);

                        // Store bar position for drag detection
                        if (draggingSlider == slider) {
                            sliderRenderX = barX;
                            sliderRenderWidth = barW;
                        }

                        offsetY += 26;

                    } else if (setting instanceof KeybindSetting) {
                        KeybindSetting kb = (KeybindSetting) setting;

                        boolean isListening = listeningKeybind == kb;
                        String keyLabel = isListening ? "[ Press key... ]" : "[ " + kb.getDisplayName() + " ]";
                        int keyColor = isListening ? 0xFFFFAA00 : 0xFF55AAFF;

                        int labelW = mc.fontRendererObj.getStringWidth(keyLabel);
                        mc.fontRendererObj.drawString(keyLabel, x + width - labelW - 6, offsetY + 3, keyColor);

                        offsetY += 22;
                    }
                }

                // Small divider after settings
                offsetY += 2;
            }
        }
    }

    // ----------------------------------------------------------------
    // MOUSE CLICKED
    // ----------------------------------------------------------------
    public void mouseClicked(int panelX, int panelY, int mouseX, int mouseY, int button) {
        int x = panelX;
        int offsetY = panelY;

        for (Module module : category.getModules()) {
            int width = 160;
            int height = 18;

            boolean hovered =
                    mouseX >= x && mouseX <= x + width &&
                    mouseY >= offsetY && mouseY <= offsetY + height;

            if (hovered) {
                if (button == 0) {
                    // Left click = toggle
                    module.toggle();
                } else if (button == 1) {
                    // Right click = expand/collapse settings
                    if (!module.getSettings().isEmpty()) {
                        expandedModule = (expandedModule == module) ? null : module;
                        draggingSlider = null;
                        listeningKeybind = null;
                    }
                }
                return;
            }

            offsetY += height + 2;

            // Check clicks inside expanded settings
            if (expandedModule == module) {
                for (Setting setting : module.getSettings()) {

                    if (setting instanceof SliderSetting) {
                        SliderSetting slider = (SliderSetting) setting;

                        int barX = x + 14;
                        int barY = offsetY + 12;
                        int barW = width - 28;
                        int barH = 8;

                        if (button == 0 &&
                            mouseX >= barX && mouseX <= barX + barW &&
                            mouseY >= barY - 2 && mouseY <= barY + barH) {

                            draggingSlider = slider;
                            sliderRenderX = barX;
                            sliderRenderWidth = barW;
                            updateSlider(mouseX);
                        }

                        offsetY += 26;

                    } else if (setting instanceof KeybindSetting) {
                        KeybindSetting kb = (KeybindSetting) setting;

                        int rowY = offsetY;
                        if (button == 0 &&
                            mouseX >= x + 8 && mouseX <= x + width &&
                            mouseY >= rowY && mouseY <= rowY + 22) {

                            if (listeningKeybind == kb) {
                                listeningKeybind = null; // cancel if clicked again
                            } else {
                                listeningKeybind = kb;
                                kb.startListening();
                            }
                        }

                        offsetY += 22;
                    }
                }
                offsetY += 2;
            }
        }
    }

    // ----------------------------------------------------------------
    // MOUSE DRAG
    // ----------------------------------------------------------------
    public void mouseClickMove(int mouseX) {
        if (draggingSlider != null) {
            updateSlider(mouseX);
        }
    }

    public void mouseReleased() {
        draggingSlider = null;
    }

    // ----------------------------------------------------------------
    // KEY TYPED
    // ----------------------------------------------------------------
    public void keyTyped(char typedChar, int keyCode) {
        if (listeningKeybind != null) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                listeningKeybind.setKeyCode(0); // clear bind
            } else {
                listeningKeybind.setKeyCode(keyCode);
            }
            listeningKeybind = null;
        }
    }

    // ----------------------------------------------------------------
    // HELPERS
    // ----------------------------------------------------------------
    private void updateSlider(int mouseX) {
        float pct = (float)(mouseX - sliderRenderX) / sliderRenderWidth;
        pct = Math.max(0f, Math.min(1f, pct));
        double newVal = draggingSlider.getMin() + pct * (draggingSlider.getMax() - draggingSlider.getMin());
        draggingSlider.setValue(newVal);
    }

    private String formatDouble(double v) {
        if (v == Math.floor(v)) return String.valueOf((int) v);
        return String.format("%.2f", v);
    }

    private int blend(int col1, int col2, float t) {
        int a1 = (col1 >> 24) & 0xFF, r1 = (col1 >> 16) & 0xFF, g1 = (col1 >> 8) & 0xFF, b1 = col1 & 0xFF;
        int a2 = (col2 >> 24) & 0xFF, r2 = (col2 >> 16) & 0xFF, g2 = (col2 >> 8) & 0xFF, b2 = col2 & 0xFF;
        return ((int)(a1+(a2-a1)*t) << 24) | ((int)(r1+(r2-r1)*t) << 16) | ((int)(g1+(g2-g1)*t) << 8) | (int)(b1+(b2-b1)*t);
    }
}
