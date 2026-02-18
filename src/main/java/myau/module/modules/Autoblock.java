package myau.module.modules;

import myau.event.EventTarget;
import myau.events.TickEvent;
import myau.module.Module;
import myau.property.properties.BooleanProperty;
import myau.property.properties.PercentProperty;
import myau.property.properties.IntProperty;
import myau.util.TeamUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.CPacketEntityAction;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AutoBlock - Packet-based sword blocking for 1.8.9.
 * Uses START/STOP_SNEAKING packets → allows KillAura to keep attacking simultaneously.
 */
public class Autoblock extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    // ── Properties (toggles & sliders) ──────────────────────────────────────────
    public final PercentProperty range = new PercentProperty("Range", 45); // 0–100 → 0–10 blocks
    public final IntProperty maxHurtTime = new IntProperty("Max Hurt Time", 8, 0, 10);
    public final IntProperty maxHoldDuration = new IntProperty("Max Hold Ticks", 5, 1, 20);
    public final IntProperty maxLagDuration = new IntProperty("Lag Comp Ticks", 3, 0, 10);
    public final BooleanProperty onlySword = new BooleanProperty("Only Sword", true);
    public final BooleanProperty onlyWhenSwinging = new BooleanProperty("Only Swinging", true);
    public final BooleanProperty alwaysBlock = new BooleanProperty("Always Block", false); // NEW: ignore swinging check

    private int blockTicks = 0;
    private boolean isBlocking = false;

    public Autoblock() {
        super("Autoblock", false);
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (!isEnabled()) {
            stopBlocking();
            return;
        }

        if (mc.thePlayer == null || !mc.thePlayer.onGround) {
            stopBlocking();
            return;
        }

        if (onlySword.getValue() && !isHoldingSword()) {
            stopBlocking();
            return;
        }

        EntityLivingBase target = getClosestEnemy();
        if (target == null) {
            stopBlocking();
            return;
        }

        double distance = mc.thePlayer.getDistanceToEntity(target);
        float realRange = range.getValue().floatValue() / 10f;

        if (distance > realRange) {
            stopBlocking();
            return;
        }

        // Only block if enemy is swinging (unless "Always Block" is on)
        if (!alwaysBlock.getValue() && onlyWhenSwinging.getValue() && target.swingProgressInt <= 0) {
            stopBlocking();
            return;
        }

        // Don't block if we just got hit (anti-combo break)
        if (mc.thePlayer.hurtTime > maxHurtTime.getValue()) {
            stopBlocking();
            return;
        }

        // Start/continue blocking
        if (blockTicks < maxHoldDuration.getValue()) {
            startBlocking();
            blockTicks++;
        }

        // Lag compensation: keep blocking briefly after target leaves range
        if (blockTicks > 0 && distance > realRange + 0.5) {
            blockTicks--;
            if (blockTicks > 0) {
                startBlocking();
            }
        }
    }

    private boolean isHoldingSword() {
        return mc.thePlayer.getCurrentEquippedItem() != null
                && mc.thePlayer.getCurrentEquippedItem().getItem() instanceof ItemSword;
    }

    private EntityLivingBase getClosestEnemy() {
        List<Entity> entities = mc.theWorld.loadedEntityList;

        return entities.stream()
                .filter(e -> e instanceof EntityLivingBase
                        && e != mc.thePlayer
                        && !(e instanceof EntityPlayer && TeamUtil.isFriend((EntityPlayer) e))
                        && mc.thePlayer.canEntityBeSeen(e))
                .map(e -> (EntityLivingBase) e)
                .min(Comparator.comparingDouble(e -> mc.thePlayer.getDistanceToEntity(e)))
                .orElse(null);
    }

    private void startBlocking() {
        if (!isBlocking) {
            // Packet: START_SNEAKING → activates sword block on server
            mc.thePlayer.sendQueue.addToSendQueue(
                new CPacketEntityAction(mc.thePlayer, CPacketEntityAction.Action.START_SNEAKING)
            );
            isBlocking = true;
        }
    }

    private void stopBlocking() {
        if (isBlocking) {
            // Packet: STOP_SNEAKING → releases block
            mc.thePlayer.sendQueue.addToSendQueue(
                new CPacketEntityAction(mc.thePlayer, CPacketEntityAction.Action.STOP_SNEAKING)
            );
            isBlocking = false;
            blockTicks = 0;
        }
    }

    @Override
    public void onDisable() {
        stopBlocking();
        blockTicks = 0;
    }

    public String[] getSuffix() {
        return isBlocking ? new String[]{"BLOCKING"} : new String[0];
    }
}
