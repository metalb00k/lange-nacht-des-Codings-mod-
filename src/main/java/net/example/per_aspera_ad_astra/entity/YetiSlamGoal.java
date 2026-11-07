package net.example.per_aspera_ad_astra.entity;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * Yeti slam: jumps up, crashes down and sends an earthquake wave across the ground
 * (the wave itself runs in {@link YetiEntity#startEarthquake()}).
 * Used when the target is a few blocks away, so it is the yeti's answer to players keeping their distance.
 */
public class YetiSlamGoal extends Goal {
    /** Ticks between two slams (20 = 1 s). */
    private static final int COOLDOWN = 160;
    private static final int BOSS_COOLDOWN = 100;
    /** Target must be between these distances (blocks). */
    private static final double MIN_DIST = 3.0, MAX_DIST = 12.0;
    /** Give up the slam if it hasn't landed after this many ticks. */
    private static final int MAX_DURATION = 40;

    private final YetiEntity yeti;
    private int cooldown = 60; // no slam in the first 3 seconds of a fight
    private int ticks;
    private boolean slammed;

    public YetiSlamGoal(YetiEntity yeti) {
        this.yeti = yeti;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        LivingEntity target = yeti.getTarget();
        if (target == null || !target.isAlive() || !yeti.onGround() || yeti.isEarthquakeRunning()) {
            return false;
        }
        double distSq = yeti.distanceToSqr(target);
        return distSq >= MIN_DIST * MIN_DIST && distSq <= MAX_DIST * MAX_DIST && yeti.hasLineOfSight(target);
    }

    @Override
    public boolean canContinueToUse() {
        return !slammed && ticks < MAX_DURATION;
    }

    @Override
    public void start() {
        ticks = 0;
        slammed = false;
        yeti.getNavigation().stop();
        // Leap into the air with a roar
        yeti.setDeltaMovement(yeti.getDeltaMovement().multiply(0.2, 0, 0.2).add(0, 0.75, 0));
        yeti.hasImpulse = true;
        yeti.playSound(SoundEvents.POLAR_BEAR_WARNING, 3.0F, 0.6F);
    }

    @Override
    public void tick() {
        ticks++;
        LivingEntity target = yeti.getTarget();
        if (target != null) {
            yeti.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }
        // Landed again -> SLAM
        if (ticks > 5 && yeti.onGround()) {
            yeti.startEarthquake();
            slammed = true;
        }
    }

    @Override
    public void stop() {
        cooldown = yeti.isMiniboss() ? BOSS_COOLDOWN : COOLDOWN;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
