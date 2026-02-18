package myau.module.modules;

import myau.event.EventTarget;
import myau.events.MouseEvent;
import myau.events.Render3DEvent;
import myau.events.TickEvent;
import myau.module.Module;
import myau.property.properties.BooleanProperty;
import myau.property.properties.ModeProperty;
import myau.property.properties.PercentProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Mouse;

/**
 * FreeLook - Allows free camera rotation while keeping movement direction locked.
 * Hold or toggle mode, smooth return option.
 */
public class Freelook extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    // ── Properties ───────────────────────────────────────────────────────────────
    public final ModeProperty mode = new ModeProperty("Mode", 0, new String[]{
        "Hold",      // Hold key to freelook
        "Toggle"     // Press once to toggle on/off
    });

    public final PercentProperty sensitivity = new PercentProperty("Sensitivity", 100, "Adjust mouse sensitivity while freelook is active");

    public final BooleanProperty smoothReturn = new BooleanProperty("Smooth Return", true,
        "Smoothly reset camera to body rotation when freelook ends");

    public final PercentProperty returnSpeed = new PercentProperty("Return Speed", 50,
        "How fast the camera snaps back (only if Smooth Return is enabled)");

    // ── Internal state ───────────────────────────────────────────────────────────
    private boolean isActive = false;
    private float originalYaw = 0.0f;
    private float originalPitch = 0.0f;
    private float currentYawOffset = 0.0f;
    private float currentPitchOffset = 0.0f;
    private float targetYawOffset = 0.0f;
    private float targetPitchOffset = 0.0f;

    public FreeLook() {
        super("FreeLook", true); // true = default enabled, change to false if you want it off by default
        setKeybind(Keyboard.KEY_F); // Default key: F
    }

    @Override
    public void onEnable() {
        resetOffsets();
    }

    @Override
    public void onDisable() {
        resetOffsets();
        isActive = false;
    }

    private void resetOffsets() {
        currentYawOffset = 0.0f;
        currentPitchOffset = 0.0f;
        targetYawOffset = 0.0f;
        targetPitchOffset = 0.0f;
    }

        @EventTarget
    public void onTick(TickEvent event) {
    if (event.getType() != TickEvent.Type.PRE) return;

    // ── Activation logic ─────────────────────────────────────────────────────
    if (mode.getValue() == 0) { // "Hold"
        isActive = isKeybindDown();
    } else { // "Toggle" (index 1)
        if (isKeyPressedThisTick()) {
            isActive = !isActive;
        }
    }

    // ── Smooth return when freelook ends ─────────────────────────────────────
    if (!isActive && smoothReturn.getValue()) {
        targetYawOffset = 0.0f;
        targetPitchOffset = 0.0f;

        float speed = returnSpeed.getValue().floatValue() / 100f * 0.25f;
        currentYawOffset   += (targetYawOffset   - currentYawOffset)   * speed;
        currentPitchOffset += (targetPitchOffset - currentPitchOffset) * speed;

        if (Math.abs(currentYawOffset)   < 0.05f) currentYawOffset   = 0.0f;
        if (Math.abs(currentPitchOffset) < 0.05f) currentPitchOffset = 0.0f;
    }
}

    @EventTarget
    public void onMouse(MouseEvent event) {
        if (!isActive || mc.currentScreen != null) return;

        // Only capture mouse movement when freelook is active
        if (event.getDeltaX() != 0 || event.getDeltaY() != 0) {
            float sensMult = sensitivity.getValue().floatValue() / 100f;
            float yawDelta   = event.getDeltaX() * 0.15f * sensMult;
            float pitchDelta = event.getDeltaY() * 0.15f * sensMult * -1; // invert Y

            // Accumulate offsets
            targetYawOffset   += yawDelta;
            targetPitchOffset += pitchDelta;

            // Clamp pitch like normal camera
            targetPitchOffset = Math.max(-90.0f, Math.min(90.0f, targetPitchOffset));
        }
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        if (!isActive) return;

        // Apply camera offsets (yaw/pitch) without changing player rotation
        mc.thePlayer.rotationYawHead   = mc.thePlayer.rotationYaw   + currentYawOffset;
        mc.thePlayer.rotationPitchHead = mc.thePlayer.rotationPitch + currentPitchOffset;

        // Smoothly interpolate current → target (for smooth return too)
        float lerpFactor = 0.85f; // higher = snappier
        currentYawOffset   = lerp(currentYawOffset,   targetYawOffset,   lerpFactor);
        currentPitchOffset = lerp(currentPitchOffset, targetPitchOffset, lerpFactor);
    }

    // Simple lerp helper
    private float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    // Helper to detect key press this tick (for toggle mode)
    private boolean isKeyPressedThisTick() {
        return getKeybind().isPressed();
    }

    // Optional: override if you want different activation feel
    private boolean isKeybindDown() {
        return getKeybind().isKeyDown();
    }

    // HUD suffix (shows status)
    @Override
    public String getSuffix() {
        if (!isActive) return "";
        return mode.is("Hold") ? "HOLD" : "ON";
    }
}
