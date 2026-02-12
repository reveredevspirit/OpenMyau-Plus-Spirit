package net.minecraft.client.gui;

import java.io.IOException;
import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.realms.RealmsBridge;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.demo.DemoWorldServer;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.WorldInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GuiMainMenu extends GuiScreen implements GuiYesNoCallback
{
    private static final Logger logger = LogManager.getLogger();
    private static final Random RANDOM = new Random();
    private float updateCounter;
    private GuiButton buttonResetDemo;
    private int panoramaTimer;
    private DynamicTexture viewportTexture;
    private boolean field_175375_v = true;
    private final Object threadLock = new Object();
    private String openGLWarning1;
    private String openGLWarning2;
    private String openGLWarningLink;
    public static final String field_96138_a = "Please click " + EnumChatFormatting.UNDERLINE + "here" + EnumChatFormatting.RESET + " for more information.";
    private int field_92024_r;
    private int field_92023_s;
    private int field_92022_t;
    private int field_92021_u;
    private int field_92020_v;
    private int field_92019_w;
    private ResourceLocation backgroundTexture;
    private GuiButton altsButton;
    private boolean field_183502_L;
    private GuiScreen field_183503_M;
    private GuiButton modButton;
    private GuiScreen modUpdateNotification;
    private long initTime = System.currentTimeMillis();

    public GuiMainMenu() {
        this.openGLWarning2 = field_96138_a;
        this.field_183502_L = false;
        this.updateCounter = RANDOM.nextFloat();
        this.openGLWarning1 = "";
    }

    private boolean func_183501_a()
    {
        return Minecraft.getMinecraft().gameSettings.getOptionOrdinalValue(GameSettings.Options.REALMS_NOTIFICATIONS) && this.field_183503_M != null;
    }

    public void updateScreen()
    {
        ++this.panoramaTimer;

        if (this.func_183501_a())
        {
            this.field_183503_M.updateScreen();
        }
    }

    public boolean doesGuiPauseGame()
    {
        return false;
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
    }

    public void initGui()
    {
        this.viewportTexture = new DynamicTexture(256, 256);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        int i = 24;
        int j = this.height / 4 + 48;

        if (this.mc.isDemo())
        {
            this.addDemoButtons(j, 24);
        }
        else
        {
            this.addSingleplayerMultiplayerButtons(j, 24);
        }

        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, j + 72 + 12, 98, 20, I18n.format("menu.options")));
        this.buttonList.add(new GuiButton(4, this.width / 2 + 2, j + 72 + 12, 98, 20, I18n.format("menu.quit")));
        this.buttonList.add(new GuiButtonLanguage(5, this.width / 2 - 124, j + 72 + 12));

        synchronized (this.threadLock)
        {
            this.field_92023_s = this.fontRendererObj.getStringWidth(this.openGLWarning1);
            this.field_92024_r = this.fontRendererObj.getStringWidth(this.openGLWarning2);
            int k = Math.max(this.field_92023_s, this.field_92024_r);
            this.field_92022_t = (this.width - k) / 2;
            this.field_92021_u = this.buttonList.get(0).yPosition - 24;
            this.field_92020_v = this.field_92022_t + k;
            this.field_92019_w = this.field_92021_u + 24;
        }

        this.mc.setConnectedToRealms(false);

        if (Minecraft.getMinecraft().gameSettings.getOptionOrdinalValue(GameSettings.Options.REALMS_NOTIFICATIONS) && !this.field_183502_L)
        {
            RealmsBridge realmsbridge = new RealmsBridge();
            this.field_183503_M = realmsbridge.getNotificationScreen(this);
            this.field_183502_L = true;
        }

        if (this.func_183501_a())
        {
            this.field_183503_M.setGuiSize(this.width, this.height);
            this.field_183503_M.initGui();
        }
    }

    private void addSingleplayerMultiplayerButtons(int p_73969_1_, int p_73969_2_)
    {
        this.buttonList.add(new GuiButton(1, this.width / 2 - 100, p_73969_1_, I18n.format("menu.singleplayer")));
        this.buttonList.add(new GuiButton(2, this.width / 2 - 100, p_73969_1_ + p_73969_2_, I18n.format("menu.multiplayer")));

        // REMOVED: Reflector dependency not available
        this.buttonList.add(this.altsButton = new GuiButton(14, this.width / 2 - 100, p_73969_1_ + p_73969_2_ * 2, "Alt Manager"));
    }

    private void addDemoButtons(int p_73972_1_, int p_73972_2_)
    {
        this.buttonList.add(new GuiButton(11, this.width / 2 - 100, p_73972_1_, I18n.format("menu.playdemo")));
        this.buttonList.add(this.buttonResetDemo = new GuiButton(12, this.width / 2 - 100, p_73972_1_ + p_73972_2_, I18n.format("menu.resetdemo")));
        ISaveFormat isaveformat = this.mc.getSaveLoader();
        WorldInfo worldinfo = isaveformat.getWorldInfo("Demo_World");

        if (worldinfo == null)
        {
            this.buttonResetDemo.enabled = false;
        }
    }

    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.id == 0)
        {
            this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
        }

        if (button.id == 5)
        {
            this.mc.displayGuiScreen(new GuiLanguage(this, this.mc.gameSettings, this.mc.getLanguageManager()));
        }

        if (button.id == 1)
        {
            this.mc.displayGuiScreen(new GuiSelectWorld(this));
        }

        if (button.id == 2)
        {
            this.mc.displayGuiScreen(new GuiMultiplayer(this));
        }

        if (button.id == 14 && this.altsButton.visible)
        {
            // REMOVED: wtf.fentanyl dependency not available
            // this.mc.displayGuiScreen(new wtf.fentanyl.gui.alt.AltManagerGui());
        }

        if (button.id == 4)
        {
            this.mc.shutdown();
        }

        if (button.id == 6)
        {
            // Placeholder for mod list gui
        }

        if (button.id == 11)
        {
            this.mc.launchIntegratedServer("Demo_World", "Demo_World", DemoWorldServer.demoWorldSettings);
        }

        if (button.id == 12)
        {
            ISaveFormat isaveformat = this.mc.getSaveLoader();
            WorldInfo worldinfo = isaveformat.getWorldInfo("Demo_World");

            if (worldinfo != null)
            {
                GuiYesNo guiyesno = GuiSelectWorld.makeDeleteWorldYesNo(this, worldinfo.getWorldName(), 12);
                this.mc.displayGuiScreen(guiyesno);
            }
        }
    }

    private void switchToRealms()
    {
        RealmsBridge realmsbridge = new RealmsBridge();
        realmsbridge.switchToRealms(this);
    }

    public void confirmClicked(boolean result, int id)
    {
        if (result && id == 12)
        {
            ISaveFormat isaveformat = this.mc.getSaveLoader();
            isaveformat.flushCache();
            isaveformat.deleteWorldDirectory("Demo_World");
            this.mc.displayGuiScreen(this);
        }
        else if (id == 13)
        {
            if (result)
            {
                try
                {
                    Class<?> oclass = Class.forName("java.awt.Desktop");
                    Object object = oclass.getMethod("getDesktop").invoke(null);
                    oclass.getMethod("browse", URI.class).invoke(object, new URI(this.openGLWarningLink));
                }
                catch (Throwable throwable)
                {
                    logger.error("Couldn't open link", throwable);
                }
            }

            this.mc.displayGuiScreen(this);
        }
    }

    private void drawPanorama(int p_73970_1_, int p_73970_2_, float p_73970_3_)
    {
        // REMOVED: CustomPanorama dependency not available
        /*
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.matrixMode(5889);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        Project.gluPerspective(120.0F, 1.0F, 0.05F, 10.0F);
        GlStateManager.matrixMode(5888);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.disableCull();
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        int i = 8;
        int j = 64;

        // ... Panorama drawing logic would go here ...

        worldrenderer.setTranslation(0.0D, 0.0D, 0.0D);
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.matrixMode(5889);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(5888);
        GlStateManager.popMatrix();
        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.enableDepth();
        */
    }

    private void rotateAndBlurSkybox(float p_73968_1_)
    {
        // REMOVED: backgroundTexture field is never assigned, CustomPanorama not available
        /*
        this.mc.getTextureManager().bindTexture(this.backgroundTexture);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, 256, 256);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.colorMask(true, true, true, false);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        GlStateManager.disableAlpha();
        int i = 3;
        int j = 3;

        // ... Blur logic would go here ...

        tessellator.draw();
        GlStateManager.enableAlpha();
        GlStateManager.colorMask(true, true, true, true);
        */
    }

    private void renderSkybox(int p_73971_1_, int p_73971_2_, float p_73971_3_)
    {
        // REMOVED: CustomPanorama dependency not available
        /*
        this.mc.getFramebuffer().unbindFramebuffer();
        GlStateManager.viewport(0, 0, 256, 256);
        this.drawPanorama(p_73971_1_, p_73971_2_, p_73971_3_);
        this.rotateAndBlurSkybox(p_73971_3_);
        int i = 3;
        CustomPanoramaProperties custompanoramaproperties = CustomPanorama.getCustomPanoramaProperties();

        if (custompanoramaproperties != null)
        {
            i = custompanoramaproperties.getBlur3();
        }

        for (int j = 0; j < i; ++j)
        {
            this.rotateAndBlurSkybox(p_73971_3_);
            this.rotateAndBlurSkybox(p_73971_3_);
        }

        this.mc.getFramebuffer().bindFramebuffer(true);
        GlStateManager.viewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
        // ... rest of skybox rendering ...
        */
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        GlStateManager.disableAlpha();
        // MainMenu.draw(initTime);  // REMOVED: MainMenu dependency not available
        GlStateManager.enableAlpha();

        String titleText = "OpenMyau";
        float scale = 4.0F; // change size here

        GlStateManager.pushMatrix();
        GlStateManager.disableBlend(); // keeps text sharp

        GlStateManager.scale(scale, scale, scale);

// real center correction when scaled
        float x = (this.width / 2f) / scale
                - (this.fontRendererObj.getStringWidth(titleText) / 2f);
        float y = 20f / scale;


// core
        this.fontRendererObj.drawString(titleText, x, y, 0xFFFF3A3A, true);

        GlStateManager.enableBlend();
        GlStateManager.popMatrix();


        Tessellator tessellator = Tessellator.getInstance();
        // WorldRenderer worldrenderer = tessellator.getWorldRenderer();  // REMOVED: Not used
        int i = 274;
        int j = this.width / 2 - i / 2;
        // int k = 30;  // REMOVED: Never used
        int l = -2130706433;
        int i1 = 16777215;
        int j1 = 0;
        int k1 = Integer.MIN_VALUE;
        // CustomPanoramaProperties custompanoramaproperties = CustomPanorama.getCustomPanoramaProperties();  // REMOVED: CustomPanorama dependency not available

        /*
        if (custompanoramaproperties != null)
        {
            l = custompanoramaproperties.getOverlay1Top();
            i1 = custompanoramaproperties.getOverlay1Bottom();
            j1 = custompanoramaproperties.getOverlay2Top();
            k1 = custompanoramaproperties.getOverlay2Bottom();
        }
        */

        if (l != 0 || i1 != 0)
        {
            this.drawGradientRect(0, 0, this.width, this.height, l, i1);
        }

        if (j1 != 0 || k1 != 0)
        {
            this.drawGradientRect(0, 0, this.width, this.height, j1, k1);
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        if (this.mc.isDemo()) {
            // Demo mode - additional handling can go here
        }

        // REMOVED: Reflector dependency not available
        /*
        if (Reflector.FMLCommonHandler_getBrandings.exists())
        {
            Object object = Reflector.call(Reflector.FMLCommonHandler_instance, new Object[0]);
            List<String> list = Lists.<String>reverse((List)Reflector.call(object, Reflector.FMLCommonHandler_getBrandings, new Object[] {Boolean.valueOf(true)}));

            for (int l1 = 0; l1 < list.size(); ++l1)
            {
                String s1 = (String)list.get(l1);

                if (!Strings.isNullOrEmpty(s1))
                {
                    this.drawString(this.fontRendererObj, s1, 2, this.height - (10 + l1 * (this.fontRendererObj.FONT_HEIGHT + 1)), 16777215);
                }
            }

            if (Reflector.ForgeHooksClient_renderMainMenu.exists())
            {
                Reflector.call(Reflector.ForgeHooksClient_renderMainMenu, new Object[] {this, this.fontRendererObj, Integer.valueOf(this.width), Integer.valueOf(this.height)});
            }
        }
        */

        if (this.openGLWarning1 != null && !this.openGLWarning1.isEmpty())
        {
            drawRect(this.field_92022_t - 2, this.field_92021_u - 2, this.field_92020_v + 2, this.field_92019_w - 1, 1428160512);
            this.drawString(this.fontRendererObj, this.openGLWarning1, this.field_92022_t, this.field_92021_u, -1);
            this.drawString(this.fontRendererObj, this.openGLWarning2, (this.width - this.field_92024_r) / 2, this.buttonList.get(0).yPosition - 12, -1);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);

        if (this.func_183501_a())
        {
            this.field_183503_M.drawScreen(mouseX, mouseY, partialTicks);
        }

        if (this.modUpdateNotification != null)
        {
            this.modUpdateNotification.drawScreen(mouseX, mouseY, partialTicks);
        }
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        synchronized (this.threadLock)
        {
            if (!this.openGLWarning1.isEmpty() && mouseX >= this.field_92022_t && mouseX <= this.field_92020_v && mouseY >= this.field_92021_u && mouseY <= this.field_92019_w)
            {
                GuiConfirmOpenLink guiconfirmopenlink = new GuiConfirmOpenLink(this, this.openGLWarningLink, 13, true);
                guiconfirmopenlink.disableSecurityWarning();
                this.mc.displayGuiScreen(guiconfirmopenlink);
            }
        }

        if (this.func_183501_a())
        {
            this.field_183503_M.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    public void onGuiClosed()
    {
        if (this.field_183503_M != null)
        {
            this.field_183503_M.onGuiClosed();
        }
    }
}