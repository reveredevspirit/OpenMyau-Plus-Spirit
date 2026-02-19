@Override
public void drawScreen(int mouseX, int mouseY, float partialTicks) {

    ScaledResolution sr = new ScaledResolution(mc);

    openAnim += (1f - openAnim) * 0.15f;

    // Calculate height ONCE
    int panelHeight = getPanelHeight();

    // ----------------------------------------------------------------
    // BACKGROUND ONLY BEHIND THE GUI — not full screen
    // ----------------------------------------------------------------
    drawRect(
        posX - 4,
        posY - 4,
        posX + TOTAL_WIDTH + 4,
        posY + panelHeight + 4,
        (int)(0xAA000000)
    );

    // ----------------------------------------------------------------
    // OUTER BACKGROUND
    // ----------------------------------------------------------------
    RoundedUtils.drawRoundedRect(posX, posY, TOTAL_WIDTH, panelHeight, 10, 0xF0080808);

    // ----------------------------------------------------------------
    // SIDEBAR — same width/height as outer so edges align perfectly
    // NO divider line — the color difference is enough
    // ----------------------------------------------------------------
    RoundedUtils.drawRoundedRect(posX, posY, SIDEBAR_WIDTH, panelHeight, 10, 0xF0111111);

    // Title
    GL11.glColor4f(1f, 1f, 1f, 1f);
    mc.fontRendererObj.drawString("§b§lMyau", posX + 10, posY + 8, 0xFFFFFFFF);

    // ----------------------------------------------------------------
    // CATEGORIES
    // ----------------------------------------------------------------
    int yOffset = posY + 28;
    for (SidebarCategory cat : categories) {
        boolean selected = selectedCategory == cat;
        boolean hovered  = mouseX >= posX + 6 && mouseX <= posX + SIDEBAR_WIDTH - 6 &&
                           mouseY >= yOffset - 2 && mouseY <= yOffset + 18;

        if (selected) {
            RoundedUtils.drawRoundedRect(posX + 6, yOffset - 2, SIDEBAR_WIDTH - 12, 20, 4, 0xFF1A3A5C);
            drawRect(posX + 4, yOffset, posX + 7, yOffset + 14, 0xFF55AAFF);
        } else if (hovered) {
            RoundedUtils.drawRoundedRect(posX + 6, yOffset - 2, SIDEBAR_WIDTH - 12, 20, 4, 0xFF1A1A1A);
        }

        int textColor = selected ? 0xFF55AAFF : (hovered ? 0xFFCCCCCC : 0xFF888888);
        GL11.glColor4f(1f, 1f, 1f, 1f);
        mc.fontRendererObj.drawString(cat.getName(), posX + 14, yOffset + 3, textColor);

        yOffset += 24;
    }

    // ----------------------------------------------------------------
    // CONFIGS BUTTON
    // ----------------------------------------------------------------
    int configBtnY = getConfigBtnY();
    boolean configHovered = mouseX >= posX + 6 && mouseX <= posX + SIDEBAR_WIDTH - 6 &&
                            mouseY >= configBtnY && mouseY <= configBtnY + 16;

    RoundedUtils.drawRoundedRect(posX + 6, configBtnY, SIDEBAR_WIDTH - 12, 16, 4,
            showConfigs ? 0xFF1A3A5C : (configHovered ? 0xFF1A1A1A : 0xFF161616));

    if (showConfigs) {
        drawRect(posX + 4, configBtnY + 2, posX + 7, configBtnY + 14, 0xFF55AAFF);
    }

    GL11.glColor4f(1f, 1f, 1f, 1f);
    mc.fontRendererObj.drawString(
            showConfigs ? "§bConfigs" : "§7Configs",
            posX + 14, configBtnY + 4,
            showConfigs ? 0xFF55AAFF : (configHovered ? 0xFFCCCCCC : 0xFF888888));

    if (showConfigs) {
        configPanel.render(posX + 6, configBtnY + 20, mouseX, mouseY);
    }

    // ----------------------------------------------------------------
    // MAIN PANEL
    // ----------------------------------------------------------------
    int panelX = posX + SIDEBAR_WIDTH + 8;

    searchBar.render(panelX, posY + 10, mouseX, mouseY);

    drawRect(panelX, posY + 30, posX + TOTAL_WIDTH - 8, posY + 31, 0xFF222222);

    modulePanel.render(panelX, posY + 38, mouseX, mouseY, searchBar.getText());

    super.drawScreen(mouseX, mouseY, partialTicks);
}
