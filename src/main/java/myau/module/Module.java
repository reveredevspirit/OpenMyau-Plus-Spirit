package myau.module;

import myau.Myau;
import myau.event.Event;
import myau.settings.Setting; // if you have settings system, otherwise remove
import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {

    protected static final Minecraft mc = Minecraft.getMinecraft();

    private final String name;
    private final String description;
    private final ModuleCategory category;
    private int keybind = Keyboard.KEY_NONE;

    private boolean toggled = false;

    // Animation fields for ArrayList slide/fade
    private long lastToggleTime = 0L;
    private float animationProgress = 0.0f;  // 0.0 = hidden (slid out), 1.0 = fully visible

    // If your base uses a list of settings
    protected List<Setting> settings = new ArrayList<>();

    public Module(String name, String description, ModuleCategory category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public Module(String name, String description, ModuleCategory category, int keybind) {
        this(name, description, category);
        this.keybind = keybind;
    }

    // Toggle logic
    public void toggle() {
        setToggled(!toggled);
    }

    public void setToggled(boolean toggled) {
        if (this.toggled == toggled) return;

        this.toggled = toggled;
        lastToggleTime = System.currentTimeMillis();

        if (toggled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    public boolean isToggled() {
        return toggled;
    }

    // Animation update (call every frame/tick)
    public void updateAnimation() {
        float target = isToggled() ? 1.0f : 0.0f;
        animationProgress = MathHelper.lerp(0.14f, animationProgress, target);
    }

    public float getAnimationProgress() {
        return animationProgress;
    }

    // Optional: snap animation (e.g. on config load)
    public void resetAnimation() {
        animationProgress = isToggled() ? 1.0f : 0.0f;
    }

    // Lifecycle methods (override in child modules)
    public void onEnable() {
        // Called when module is enabled
    }

    public void onDisable() {
        // Called when module is disabled
    }

    public void onUpdate() {
        // Called every tick if enabled (add updateAnimation() here if no global call)
        updateAnimation();  // ← optional fallback if no global tick loop
    }

    public void onRender2D() {
        // Called every render frame (if needed for custom drawing)
    }

    // Event bus methods (if your base uses custom events)
    public void onEvent(Event event) {
        // Override if needed for specific events
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ModuleCategory getCategory() {
        return category;
    }

    public int getKeybind() {
        return keybind;
    }

    public void setKeybind(int keybind) {
        this.keybind = keybind;
    }

    public List<Setting> getSettings() {
        return settings;
    }

    // Optional: add setting helper
    protected void addSetting(Setting setting) {
        settings.add(setting);
    }
}
