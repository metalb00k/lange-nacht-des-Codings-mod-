package net.example.per_aspera_ad_astra.entity;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Ice ghast ("Eisghast").
 *
 * It extends the vanilla Ghast, so it floats around and attacks players like a ghast.
 *
 * Differences to the normal ghast:
 *  - shoots ice charges (IceChargeEntity) instead of fireballs: they freeze you
 *  - explodes when it dies
 *
 * No spawn egg and no natural spawning: use /summon per_aspera_ad_astra:ice_ghast
 */
public class IceGhastEntity extends Ghast {

    /** Strength of the death explosion (creeper = 3, TNT = 4). */
    public static final float DEATH_EXPLOSION_POWER = 4.0F;

    public IceGhastEntity(EntityType<? extends Ghast> type, Level level) {
        super(type, level);
    }

    /** Same stats as a normal ghast (10 health), searches for players up to 64 blocks away. */
    public static AttributeSupplier.Builder createAttributes() {
        return Ghast.createAttributes()
                .add(Attributes.FOLLOW_RANGE, 64.0D);
    }

    /**
     * The vanilla ghast goals can't be reused from our package, so these are copies of them.
     * Only the shooting goal is changed (ice charge instead of fireball).
     */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(5, new FloatAroundGoal(this));
        this.goalSelector.addGoal(7, new LookAtTargetGoal(this));
        this.goalSelector.addGoal(7, new ShootIceGoal(this));
        // Looks for players all around it. The vanilla ghast only finds players that are at most
        // 4 blocks above/below it, so it often ignores you – this one searches the full range.
        this.targetSelector.addGoal(2, new FindPlayerGoal(this));
    }

    /** Finds the nearest player in a big cube around the ghast (64 blocks in every direction). */
    static class FindPlayerGoal extends NearestAttackableTargetGoal<Player> {
        FindPlayerGoal(Mob mob) {
            super(mob, Player.class, 10, true, false, null);
        }

        @Override
        protected AABB getTargetSearchArea(double range) {
            return this.mob.getBoundingBox().inflate(range, range, range);
        }
    }

    /** If a player hits it, it goes after that player (also from far away). */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide && source.getEntity() instanceof Player player
                && !player.isCreative() && !player.isSpectator()) {
            this.setTarget(player);
        }
        return hurt;
    }

    /** Boom when it dies. */
    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!this.level().isClientSide) {
            this.level().explode(this, this.getX(), this.getY(0.5D), this.getZ(),
                    DEATH_EXPLOSION_POWER, Level.ExplosionInteraction.MOB);
        }
    }

    // ---------------------------------------------------------------------
    // AI goals (copied from the vanilla Ghast)
    // ---------------------------------------------------------------------

    /** Flies to a random spot nearby. */
    static class FloatAroundGoal extends Goal {
        private final Ghast ghast;

        FloatAroundGoal(Ghast ghast) {
            this.ghast = ghast;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            MoveControl move = this.ghast.getMoveControl();
            if (!move.hasWanted()) {
                return true;
            }
            double dx = move.getWantedX() - this.ghast.getX();
            double dy = move.getWantedY() - this.ghast.getY();
            double dz = move.getWantedZ() - this.ghast.getZ();
            double dist = dx * dx + dy * dy + dz * dz;
            return dist < 1.0D || dist > 3600.0D;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            RandomSource random = this.ghast.getRandom();
            double x = this.ghast.getX() + (random.nextFloat() * 2.0F - 1.0F) * 16.0F;
            double y = this.ghast.getY() + (random.nextFloat() * 2.0F - 1.0F) * 16.0F;
            double z = this.ghast.getZ() + (random.nextFloat() * 2.0F - 1.0F) * 16.0F;
            this.ghast.getMoveControl().setWantedPosition(x, y, z, 1.0D);
        }
    }

    /** Looks where it flies, or at its target. */
    static class LookAtTargetGoal extends Goal {
        private final Ghast ghast;

        LookAtTargetGoal(Ghast ghast) {
            this.ghast = ghast;
            this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return true;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = this.ghast.getTarget();
            if (target == null) {
                Vec3 motion = this.ghast.getDeltaMovement();
                this.ghast.setYRot(-((float) Mth.atan2(motion.x, motion.z)) * (180F / (float) Math.PI));
            } else if (target.distanceToSqr(this.ghast) < 4096.0D) {
                double dx = target.getX() - this.ghast.getX();
                double dz = target.getZ() - this.ghast.getZ();
                this.ghast.setYRot(-((float) Mth.atan2(dx, dz)) * (180F / (float) Math.PI));
            } else {
                return;
            }
            this.ghast.yBodyRot = this.ghast.getYRot();
        }
    }

    /** Charges up (opens mouth + sound) and shoots an ice charge at the target. */
    static class ShootIceGoal extends Goal {
        private final Ghast ghast;
        private int chargeTime;

        ShootIceGoal(Ghast ghast) {
            this.ghast = ghast;
        }

        @Override
        public boolean canUse() {
            return this.ghast.getTarget() != null;
        }

        @Override
        public void start() {
            this.chargeTime = 0;
        }

        @Override
        public void stop() {
            this.ghast.setCharging(false);
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = this.ghast.getTarget();
            if (target == null) {
                return;
            }
            if (target.distanceToSqr(this.ghast) < 4096.0D && this.ghast.hasLineOfSight(target)) {
                Level level = this.ghast.level();
                ++this.chargeTime;
                if (this.chargeTime == 10 && !this.ghast.isSilent()) {
                    level.levelEvent(null, 1015, this.ghast.blockPosition(), 0); // ghast warning sound
                }
                if (this.chargeTime == 20) {
                    Vec3 view = this.ghast.getViewVector(1.0F);
                    double dx = target.getX() - (this.ghast.getX() + view.x * 4.0D);
                    double dy = target.getY(0.5D) - (0.5D + this.ghast.getY(0.5D));
                    double dz = target.getZ() - (this.ghast.getZ() + view.z * 4.0D);
                    if (!this.ghast.isSilent()) {
                        level.levelEvent(null, 1016, this.ghast.blockPosition(), 0); // ghast shoot sound
                    }
                    IceChargeEntity charge = new IceChargeEntity(level, this.ghast, dx, dy, dz);
                    charge.setPos(this.ghast.getX() + view.x * 4.0D, this.ghast.getY(0.5D) + 0.5D, charge.getZ());
                    level.addFreshEntity(charge);
                    this.chargeTime = -40;
                }
            } else if (this.chargeTime > 0) {
                --this.chargeTime;
            }
            this.ghast.setCharging(this.chargeTime > 10);
        }
    }
}
