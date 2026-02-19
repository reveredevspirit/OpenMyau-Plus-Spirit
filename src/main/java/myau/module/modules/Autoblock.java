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

public class Autoblock extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public final ModeProperty mode;
    public final BooleanProperty requirePress;
    public final BooleanProperty requireAttack;
    public final FloatProperty blockRange;
    public final FloatProperty minCPS;
    public final FloatProperty maxCPS;
    public final BooleanProperty autoRelease;  // NEW: Proper release for Grim
    public final FloatProperty releaseDelay;   // NEW: Ticks to hold block (1-5)

    private boolean blockingState = false;
    private boolean fakeBlockState = false;
    private boolean isBlocking = false;
    private boolean blinkReset = false;
    private int blockTick = 0;
    private long blockDelayMS = 0L;
    private int releaseTick = 0;  // NEW: Countdown for auto-release
    private int lastAttackTick = 0;  // NEW: Track recent attacks for timing

    public Autoblock() {
        super("Autoblock", false);
        this.mode = new ModeProperty(
                "mode",
                10,  // Default to new GRIM mode
                new String[]{
                        "NONE", "VANILLA", "SPOOF", "HYPIXEL", "BLINK",
                        "INTERACT", "SWAP", "LEGIT", "FAKE", "LAGRANGE",
                        "GRIM"  // NEW: GrimAC-friendly mode
                }
        );
        this.requirePress = new BooleanProperty("require-press", false);
        this.requireAttack = new BooleanProperty("require-attack", true);  // RECOMMENDED: ON for Grim
        this.blockRange = new FloatProperty("block-range", 6.0F, 3.0F, 8.0F);
        this.minCPS = new FloatProperty("min-aps", 6.0F, 1.0F, 20.0F);     // LOWERED default for less spam
        this.maxCPS = new FloatProperty("max-aps", 9.0F, 1.0F, 20.0F);
        this.autoRelease = new BooleanProperty("auto-release", true);      // NEW
        this.releaseDelay = new FloatProperty("release-delay", 2.0F, 1.0F, 5.0F);  // NEW: Hold block 2 ticks
    }

    private long getBlockDelay() {
        return (long) (1000.0F / RandomUtil.nextLong(
                this.minCPS.getValue().longValue(),
                this.maxCPS.getValue().longValue()
        ));
    }

    private boolean canAutoblock() {
        if (!ItemUtil.isHoldingSword()) return false;
        if (this.requirePress.getValue() && !PlayerUtil.isUsingItem()) return false;
        KillAura ka = (KillAura) Myau.moduleManager.modules.get(KillAura.class);
        if (this.requireAttack.getValue() && !ka.isAttackAllowed()) return false;
        return true;
    }

    private boolean hasValidTarget() {
        return mc.theWorld.loadedEntityList.stream().anyMatch(
                entity -> entity instanceof net.minecraft.entity.EntityLivingBase
                        && RotationUtil.distanceToEntity((net.minecraft.entity.EntityLivingBase) entity)
                        <= this.blockRange.getValue()
        );
    }

    private void startBlock(ItemStack stack) {
        if (stack == null) return;
        PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(stack));
        mc.thePlayer.setItemInUse(stack, stack.getMaxItemUseDuration());
        this.blockingState = true;
        this.releaseTick = (int) this.releaseDelay.getValue().floatValue();  // Start countdown
    }

    private void stopBlock() {
        PacketUtil.sendPacket(new C07PacketPlayerDigging(
                C07PacketPlayerDigging.Action.RELEASE_USE_ITEM,
                BlockPos.ORIGIN,
                EnumFacing.DOWN
        ));
        mc.thePlayer.stopUsingItem();
        this.blockingState = false;
        this.releaseTick = 0;
    }

    private int findEmptySlot(int currentSlot) {
        for (int i = 0; i < 9; i++)
            if (i != currentSlot && mc.thePlayer.inventory.getStackInSlot(i) == null)
                return i;
        for (int i = 0; i < 9; i++) {
            if (i != currentSlot) {
                ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
                if (stack != null && !stack.hasDisplayName())
                    return i;
            }
        }
        return Math.floorMod(currentSlot - 1, 9);
    }

    @EventTarget(Priority.LOW)
    public void onUpdate(UpdateEvent event) {
        if (!this.isEnabled()) {
            resetState();
            return;
        }

        // NEW: Track recent attacks for better timing
        KillAura ka = (KillAura) Myau.moduleManager.modules.get(KillAura.class);
        if (ka.isAttackAllowed()) {
            this.lastAttackTick = 0;  // Reset on fresh attack
        } else {
            this.lastAttackTick++;
        }

        if (event.getType() == EventType.POST && this.blinkReset) {
            this.blinkReset = false;
            Myau.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
            Myau.blinkManager.setBlinkState(true, BlinkModules.AUTO_BLOCK);
        }

        if (event.getType() != EventType.PRE) return;

        if (this.blockDelayMS > 0L) this.blockDelayMS -= 50L;

        // NEW: Auto-release logic (prevents permanent block spam)
        if (this.autoRelease.getValue() && this.blockingState && this.releaseTick > 0) {
            this.releaseTick--;
            if (this.releaseTick <= 0) {
                stopBlock();
            }
        }

        boolean canBlock = this.canAutoblock() && this.hasValidTarget();
        if (!canBlock) {
            if (this.blockingState) stopBlock();  // Ensure release on stop
            Myau.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
            this.isBlocking = false;
            this.fakeBlockState = false;
            this.blockTick = 0;
            return;
        }

        boolean swap = false;
        switch (this.mode.getValue()) {
            case 0: // NONE
                this.isBlocking = false;
                this.fakeBlockState = false;
                break;
            case 1: // VANILLA - unchanged, but now with auto-release
                if (!this.blockingState) swap = true;
                this.isBlocking = true;
                this.fakeBlockState = false;
                break;
            case 2: // SPOOF - reduced frequency
                if (this.lastAttackTick <= 2) {  // Only after recent attack
                    int item = ((IAccessorPlayerControllerMP) mc.playerController).getCurrentPlayerItem();
                    int slot = this.findEmptySlot(item);
                    PacketUtil.sendPacket(new C09PacketHeldItemChange(slot));
                    PacketUtil.sendPacket(new C09PacketHeldItemChange(item));
                    swap = true;
                }
                this.isBlocking = true;
                this.fakeBlockState = false;
                break;
            case 3: // HYPIXEL - added post-attack check
                if (this.lastAttackTick > 3) {  // Delay after attack
                    switch (this.blockTick) {
                        case 0:
                            swap = true;
                            this.blockTick = 1;
                            break;
                        case 1:
                            if (this.blockDelayMS <= 50L) this.blockTick = 0;
                            break;
                    }
                }
                this.isBlocking = true;
                this.fakeBlockState = true;
                break;
            case 4: // BLINK - less aggressive
                if (this.lastAttackTick > 2) {
                    switch (this.blockTick) {
                        case 0:
                            swap = true;
                            this.blinkReset = true;
                            this.blockTick = 1;
                            break;
                        case 1:
                            if (this.blockDelayMS <= 50L) this.blockTick = 0;
                            break;
                    }
                }
                this.isBlocking = true;
                this.fakeBlockState = true;
                break;
            case 5: // INTERACT - only post-attack
                if (this.lastAttackTick <= 3) {
                    int current = ((IAccessorPlayerControllerMP) mc.playerController).getCurrentPlayerItem();
                    int empty = this.findEmptySlot(current);
                    PacketUtil.sendPacket(new C09PacketHeldItemChange(empty));
                    ((IAccessorPlayerControllerMP) mc.playerController).setCurrentPlayerItem(empty);
                    swap = true;
                }
                this.isBlocking = true;
                this.fakeBlockState = true;
                break;
            case 6: // SWAP - reduced
                if (this.lastAttackTick <= 2) {
                    int cur = ((IAccessorPlayerControllerMP) mc.playerController).getCurrentPlayerItem();
                    int emptySlot = this.findEmptySlot(cur);
                    PacketUtil.sendPacket(new C09PacketHeldItemChange(emptySlot));
                    PacketUtil.sendPacket(new C09PacketHeldItemChange(cur));
                    swap = true;
                }
                this.isBlocking = true;
                this.fakeBlockState = true;
                break;
            case 7: // LEGIT - unchanged + release
                if (this.lastAttackTick <= 1) swap = true;  // Tight to attack
                this.isBlocking = true;
                this.fakeBlockState = false;
                break;
            case 8: // FAKE
                this.isBlocking = false;
                this.fakeBlockState = true;
                break;
            case 9: // LAGRANGE - tuned for less spam
                int ping = PingUtil.getPing();
                int lagWindow = Math.min(100, Math.max(30, ping));  // Reduced max
                if (this.lastAttackTick <= 4) {
                    switch (this.blockTick) {
                        case 0:
                            swap = true;
                            this.blockDelayMS = lagWindow;
                            this.blockTick = 1;
                            break;
                        case 1:
                            if (this.blockDelayMS <= 0L) this.blockTick = 2;
                            break;
                        case 2:
                            if (ka.isAttackAllowed()) this.blockTick = 0;
                            break;
                    }
                }
                this.isBlocking = true;
                this.fakeBlockState = true;
                break;
            case 10: // NEW: GRIM - Conservative, legit-like with perfect release
                // Only block right after attack, hold briefly, release
                if (this.lastAttackTick <= 1 && !this.blockingState) {
                    swap = true;
                }
                this.isBlocking = true;
                this.fakeBlockState = false;  // Real block for legit feel
                break;
        }

        if (swap && this.blockDelayMS <= 0L) {
            this.blockDelayMS += this.getBlockDelay() + RandomUtil.nextInt(20, 50);  // EXTRA RAND DELAY
            this.startBlock(mc.thePlayer.getHeldItem());
        }
    }

    private void resetState() {
        stopBlock();  // ENSURE RELEASE
        Myau.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
        this.isBlocking = false;
        this.fakeBlockState = false;
        this.blockTick = 0;
        this.blockDelayMS = 0L;
        this.releaseTick = 0;
        this.lastAttackTick = 0;
    }

    public boolean isBlocking() {
        return this.fakeBlockState && ItemUtil.isHoldingSword();
    }

    public boolean isPlayerBlocking() {
        return (mc.thePlayer.isUsingItem() || this.blockingState) && ItemUtil.isHoldingSword();
    }

    @Override
    public void onDisabled() {
        resetState();
    }

    @Override
    public String[] getSuffix() {
        return isBlocking ? new String[]{"BLOCKING"} : new String[0];
    }
}
