package net.example.per_aspera_ad_astra.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Yeti.
 *
 * It extends the vanilla IronGolem, so it moves and fights like one
 * (big punch that throws the target into the air, arm swing animation).
 *
 * Differences to the iron golem:
 *  - attacks players right away (not only when they hit it)
 *  - attacks ALL other mobs (also animals, villagers, creepers, ...) except other yetis
 *  - no village behaviour (no flowers for villagers, no village patrol)
 *  - can't be repaired with iron ingots
 *  - polar bear sounds instead of metal sounds
 */
public class YetiEntity extends IronGolem {

    public YetiEntity(EntityType<? extends IronGolem> type, Level level) {
        super(type, level);
    }

    /** Same as the iron golem: 100 health, 15 damage, slow. */
    public static AttributeSupplier.Builder createAttributes() {
        return IronGolem.createAttributes()
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        // What it does (lower number = more important)
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(2, new MoveTowardsTargetGoal(this, 0.9D, 32.0F));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        // Who it attacks
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, YetiEntity.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Mob.class, 5, true, false,
                target -> !(target instanceof YetiEntity)));
    }

    /** The iron golem refuses to attack creepers (and players who built it). The yeti attacks everything. */
    @Override
    public boolean canAttackType(EntityType<?> type) {
        return type != EntityType.GHAST;
    }

    /** Iron golems get repaired with iron ingots. The yeti doesn't. */
    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    // ---- Sounds: polar bear instead of metal ----

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.POLAR_BEAR_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.POLAR_BEAR_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.POLAR_BEAR_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.POLAR_BEAR_STEP, 1.0F, 0.8F);
    }
}
