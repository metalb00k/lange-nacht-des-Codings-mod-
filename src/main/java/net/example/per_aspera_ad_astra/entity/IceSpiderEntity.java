package net.example.per_aspera_ad_astra.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Ice spider ("Eisspinne").
 *
 * It extends the vanilla Spider, so it climbs walls and jumps at its target.
 *
 * Differences to the normal spider:
 *  - ALWAYS attacks players, also in daylight
 *  - every hit freezes the target (like standing in powder snow:
 *    blue frost screen, slower, and damage while fully frozen)
 *
 * No spawn egg and no natural spawning: use /summon per_aspera_ad_astra:ice_spider
 */
public class IceSpiderEntity extends Spider {

    /**
     * How long a hit keeps the target fully frozen, in ticks (20 ticks = 1 second).
     * Freezing wears off by 2 ticks per tick, so 120 = about 3 seconds fully frozen,
     * then another ~3.5 seconds of thawing.
     */
    public static final int FREEZE_EXTRA_TICKS = 120;

    public IceSpiderEntity(EntityType<? extends Spider> type, Level level) {
        super(type, level);
    }

    /** Same stats as a normal spider (16 health, 2 damage). */
    public static AttributeSupplier.Builder createAttributes() {
        return Spider.createAttributes();
    }

    /**
     * Same goals as the vanilla spider, but without the "stop attacking in daylight" rule.
     */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(3, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }

    /** Every successful hit freezes the target. */
    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        // canFreeze() is false e.g. when wearing leather armor (same rule as powder snow)
        if (hit && target instanceof LivingEntity living && living.canFreeze()) {
            // Refresh to "fully frozen + FREEZE_EXTRA_TICKS" (does not stack endlessly with many hits)
            int freeze = living.getTicksRequiredToFreeze() + FREEZE_EXTRA_TICKS;
            living.setTicksFrozen(Math.max(living.getTicksFrozen(), freeze));
        }
        return hit;
    }
}
