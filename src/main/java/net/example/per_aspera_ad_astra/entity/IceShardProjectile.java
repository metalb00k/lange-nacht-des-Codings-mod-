package net.example.per_aspera_ad_astra.entity;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Ice shard shot by the spacesuit's freezer upgrade (see network/ShootIcePacket).
 * Deals damage, slows the target and freezes it a bit.
 */
public class IceShardProjectile extends ThrowableItemProjectile {
    private static final float DAMAGE = 5.0f;

    public IceShardProjectile(EntityType<? extends IceShardProjectile> type, Level level) {
        super(type, level);
    }

    public IceShardProjectile(Level level, LivingEntity shooter) {
        super(ModEntities.ICE_SHARD.get(), shooter, level);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.PACKED_ICE;
    }

    /** Flies almost straight (snowball = 0.03). */
    @Override
    protected float getGravity() {
        return 0.01f;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity target = result.getEntity();
        target.hurt(damageSources().thrown(this, getOwner()), DAMAGE);
        if (target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
            living.setTicksFrozen(Math.min(living.getTicksFrozen() + 80, living.getTicksRequiredToFreeze() + 40));
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide) {
            level().broadcastEntityEvent(this, (byte) 3); // ice particles on the client
            discard();
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            ItemParticleOption particle = new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.ICE));
            for (int i = 0; i < 8; i++) {
                level().addParticle(particle, getX(), getY(), getZ(), 0, 0, 0);
            }
        } else {
            super.handleEntityEvent(id);
        }
    }
}
