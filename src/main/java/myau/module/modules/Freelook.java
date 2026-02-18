package myau.module.modules;

import myau.event.EventTarget;
import myau.events.Render3DEvent;
import myau.events.TickEvent;
import myau.module.Module;
import myau.property.properties.BooleanProperty;
import myau.property.properties.ModeProperty;
import myau.property.properties.PercentProperty;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class Freelook extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public final ModeProperty mode = new ModeProperty("Mode", 0, new String[]{"Hold", "Toggle"});
    public final PercentProperty sensitivity = new PercentProperty("Sensitivity", 100);
    public final BooleanProperty invertPitch = new BooleanProperty("Invert Pitch", false);
    public final BooleanProperty smoothReturn = new BooleanProperty("Smooth Return", true);
    public final PercentProperty returnSpeed = new PercentProperty("Return Speed", 50);

    private boolean isActive = false;
    private boolean wasPressed = false;

    private float cameraYaw;
    private float cameraPitch;
    private float realYaw;
    private float realPitch;

    private int previousPerspective = 0;

    public Freelook() {
        super("Freelook", true);
        // Changed default key to 6
        this.setKey(Keyboard.KEY_6);
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (!isEnabled()) return;

        // Activation logic
        boolean keyDown = Keyboard.isKeyDown(this.getKey());

        if (mode.getValue() == 0) { // Hold
            isActive = keyDown && mc.currentScreen == null;
        } else { // Toggle
            if (keyDown) {
                if (!wasPressed) {
                    isActive = !isActive;
                    wasPressed = true;
                }
            } else {
                wasPressed = false;
            }
        }

        if (isActive && mc.currentScreen == null) {

            // First activation frame → store real angles & switch to 3rd person
            if (!wasPressed || !isActiveWasTrueLastTick) {  // simple way to detect activation start
                realYaw = mc.thePlayer.rotationYaw;
                realPitch = mc.thePlayer.rotationPitch;
                cameraYaw = realYaw;
                cameraPitch = realPitch;
                previousPerspective = mc.gameSettings.thirdPersonView;
                mc.gameSettings.thirdPersonView = 1; // third person back
            }

            // ─────────────────────────────────────────────
            // Mouse movement (this is the main fix/improvement)
            // ─────────────────────────────────────────────
            if (Mouse.isInsideWindow() && Mouse.isButtonDown(0) == false && Mouse.isButtonDown(1) == false) {
                Mouse.getDX(); // clear accumulated movement from previous frames
                Mouse.getDY();
            }

            int dx = Mouse.getDX();
            int dy = Mouse.getDY();

            float sens = sensitivity.getValue().floatValue() / 100f * 0.15f;

            // Yaw (horizontal) — normal direction
            cameraYaw += dx * sens;

            // Pitch (vertical) — with optional invert
            float pitchDelta = dy * sens;
            if (invertPitch.getValue()) {
                pitchDelta = -pitchDelta;
            }
            cameraPitch -= pitchDelta;   // note: -= because MC pitch is inverted natively

            cameraPitch = Math.max(-90.0F, Math.min(90.0F, cameraPitch));

            // Keep real player rotation frozen
            mc.thePlayer.rotationYaw = realYaw;
            mc.thePlayer.rotationPitch = realPitch;
            mc.thePlayer.prevRotationYaw = realYaw;
            mc.thePlayer.prevRotationPitch = realPitch;
        }

        // When turning off freelook
        if (!isActive) {
            // Smoothly interpolate camera back (visual only)
            if (smoothReturn.getValue()) {
                float speed = returnSpeed.getValue().floatValue() / 100f * 0.35f;
                cameraYaw += (realYaw - cameraYaw) * speed;
                cameraPitch += (realPitch - cameraPitch) * speed;
            } else {
                cameraYaw = realYaw;
                cameraPitch = realPitch;
            }

            // Restore original perspective when fully returned or instant
            if (!smoothReturn.getValue() || Math.abs(cameraYaw - realYaw) < 0.2f && Math.abs(cameraPitch - realPitch) < 0.2f) {
                mc.gameSettings.thirdPersonView = previousPerspective;
            }
        }

        // Remember state for next tick (used for first-frame detection)
        isActiveWasTrueLastTick = isActive;
    }

    private boolean isActiveWasTrueLastTick = false;

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        if (!isEnabled()) return;

        if (isActive || smoothReturn.getValue()) {
            // Apply freelook camera angles to head & body render
            mc.thePlayer.rotationYawHead = cameraYaw;
            mc.thePlayer.renderYawOffset = cameraYaw;
            // Optional: also affects third-person camera direction
            // mc.thePlayer.rotationYaw = cameraYaw;  // uncomment only if you want camera behind new direction
        }
    }

    @Override
    public void onDisabled() {
        isActive = false;
        wasPressed = false;
        isActiveWasTrueLastTick = false;
        if (mc.gameSettings != null) {
            mc.gameSettings.thirdPersonView = previousPerspective;
        }
    }

    @Override
    public String[] getSuffix() {
        if (!isActive) return new String[0];
        return new String[]{ mode.getValue() == 0 ? "HOLD" : "ON" };
    }
}
