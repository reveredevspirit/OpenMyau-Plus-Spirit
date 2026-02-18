package myau.module.modules;

import myau.Myau;
import myau.enums.BlinkModules;
import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.event.types.Priority;
import myau.events.UpdateEvent;
import myau.mixin.IAccessorPlayerControllerMP;
import myau.module.Module;
import myau.property.properties.*;
import myau.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import java.util.Random;

public class Autoblock extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public final ModeProperty mode;
    public final BooleanProperty requirePress;
    public final BooleanProperty requireAttack;
    public final FloatProperty blockRange;
    public final FloatProperty minCPS;
    public final FloatProperty maxCPS;

    private boolean blockingState = false;
    private boolean fakeBlockState = false;
    private boolean isBlocking = false;
    private boolean blinkReset = false;
    private int blockTick = 0;
    private long blockDelayMS = 0L;

    public Autoblock() {
        super("Autoblock", false);

        this.mode = new ModeProperty(
                "mode",
                2,
                new String[]{"NONE", "VANILLA", "SPOOF", "HYPIXEL", "BLINK", "INTERACT", "SWAP", "LEGIT", "FAKE"}
