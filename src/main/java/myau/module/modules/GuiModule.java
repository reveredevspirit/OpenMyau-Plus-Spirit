package myau.module.modules;

import myau.module.Module;
import myau.ui.ClickGui;
import myau.ui.clickgui.Rise6ClickGui;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

public class GuiModule extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private Rise6ClickGui clickGui;

    public GuiModule() {
        super("ClickGui", false);
        setKey(Keyboard.KEY_RSHIFT);
    }

    @Override
    public void onEnabled() {
        // Disable the module immediately (GUI modules should not stay "enabled")
        setEnabled(false);

        // Ensure ClickGui is initialized so module lists are populated
        if (ClickGui.combatModules == null) {
            new ClickGui();
        }

        // Create Rise6ClickGui instance
        clickGui = new Rise6ClickGui(
                ClickGui.combatModules,
                ClickGui.movementModules,
                ClickGui.playerModules,
                ClickGui.renderModules,
                ClickGui.miscModules
        );

        // Open the GUI
        mc.displayGuiScreen(clickGui);
    }
}
