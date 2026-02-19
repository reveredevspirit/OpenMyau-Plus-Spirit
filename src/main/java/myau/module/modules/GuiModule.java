@Override
public void onEnabled() {
    setEnabled(false);

    // DO NOT INITIALIZE OLD CLICKGUI ANYMORE

    clickGui = new Rise6ClickGui(
            ClickGui.combatModules,
            ClickGui.movementModules,
            ClickGui.playerModules,
            ClickGui.renderModules,
            ClickGui.miscModules
    );

    mc.displayGuiScreen(clickGui);
}
