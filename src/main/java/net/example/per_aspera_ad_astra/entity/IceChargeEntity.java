package net.example.per_aspera_ad_astra.entity;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.item.ItemStack;
import net.example.per_aspera_ad_astra.item.ModItems;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Ice charge ("Eisgeschoss"): the projectile the ice ghast shoots.
 *
 * Flies like a ghast fireball (and can be punched back like one), but instead
 * of exploding it shatters and freezes everything close to where it hits
 * (same freezing as powder snow). It does not break blocks.
 */
public class IceChargeEntity extends Fireball {

    /** Damage to the entity that is hit directly. */
    public static final float DIRECT_DAMAGE = 6.0F;
    /** Everything within this many blocks of the hit gets frozen. */
    public static final double FREEZE_RADIUS = 2.5D;
    /** How long it stays fully frozen, in ticks (20 = 1 second; it thaws 2 per tick, so 120 = ~3 s). */
    public static final int FREEZE_EXTRA_TICKS = 120;

    /** Used by Minecraft (e.g. when the client creates it). */
    public IceChargeEntity(EntityType<? extends IceChargeEntity> type, Level level) {
        super(type, level);
        this.setItem(new ItemStack(ModItems.ICE_CHARGE.get()));
    }

    /** Used by the ice ghast when it shoots. dx/dy/dz = direction to the target. */
    public IceChargeEntity(Level level, LivingEntity shooter, double dx, double dy, double dz) {
        super(ModEntities.ICE_CHARGE.get(), shooter, dx, dy, dz, level);
        this.setItem(new ItemStack(ModItems.ICE_CHARGE.get()));
    }

    /** Fireballs are drawn burning – ice shouldn't be. */
    @Override
    protected boolean shouldBurn() {
        return false;
    }

    /** Snowflakes instead of smoke behind it. */
    @Override
    protected ParticleOptions getTrailParticle() {
        return ParticleTypes.SNOWFLAKE;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide) {
            Entity owner = this.getOwner();
            result.getEntity().hurt(this.damageSources().mobProjectile(this,
                    owner instanceof LivingEntity living ? living : null), DIRECT_DAMAGE);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            // Freeze everything nearby (not the ghast that shot it)
            for (LivingEntity living : this.level().getEntitiesOfClass(LivingEntity.class,
                    this.getBoundingBox().inflate(FREEZE_RADIUS))) {
                if (living != this.getOwner()) {
                    freeze(living);
                }
            }
            // Shatter effect
            if (this.level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.SNOWFLAKE, this.getX(), this.getY(), this.getZ(),
                        40, 1.0D, 1.0D, 1.0D, 0.05D);
            }
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 1.5F, 0.8F);
            this.discard();
        }
    }

    /**
     * Freezes an entity like powder snow. Leather armor protects (canFreeze() = false),
     * same rule as in vanilla.
     */
    public static void freeze(LivingEntity living) {
        if (living.canFreeze()) {
            int freeze = living.getTicksRequiredToFreeze() + FREEZE_EXTRA_TICKS;
            living.setTicksFrozen(Math.max(living.getTicksFrozen(), freeze));
        }
    }
}
