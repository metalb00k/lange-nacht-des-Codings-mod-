package net.example.per_aspera_ad_astra.entity;

import net.example.per_aspera_ad_astra.Per_aspera_ad_astra;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
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

    // =====================================================================
    // Miniboss ("Yeti-Anführer"): the one that lives in the Yeti nest structure.
    // It is spawned by the nest (structures/yeti_nest.nbt has the entity with {Miniboss:1b}).
    // Test it with: /summon per_aspera_ad_astra:yeti ~ ~ ~ {Miniboss:1b}
    // =====================================================================
    public static final double BOSS_HEALTH = 200.0D;   // normal yeti: 100
    public static final double BOSS_DAMAGE = 22.0D;    // normal yeti: 15
    public static final float BOSS_SCALE = 1.35F;      // size compared to a normal yeti (model + hitbox)

    private static final EntityDataAccessor<Boolean> MINIBOSS =
            SynchedEntityData.defineId(YetiEntity.class, EntityDataSerializers.BOOLEAN);
    private final ServerBossEvent bossBar = (ServerBossEvent) new ServerBossEvent(
            Component.translatable("entity.per_aspera_ad_astra.yeti_boss"),
            BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10).setDarkenScreen(true);

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(MINIBOSS, false);
    }

    public boolean isMiniboss() {
        return this.entityData.get(MINIBOSS);
    }

    public void setMiniboss(boolean miniboss) {
        this.entityData.set(MINIBOSS, miniboss);
        if (miniboss) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(BOSS_HEALTH);
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(BOSS_DAMAGE);
            this.xpReward = 100;
            this.setPersistenceRequired();
        }
        this.refreshDimensions();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Miniboss", this.isMiniboss());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        boolean firstLoad = !tag.contains("Health"); // freshly spawned from the structure -> start with full boss health
        super.readAdditionalSaveData(tag);
        if (tag.getBoolean("Miniboss")) {
            this.setMiniboss(true);
            if (firstLoad) {
                this.setHealth(this.getMaxHealth());
            }
        }
        if (this.hasCustomName()) {
            this.bossBar.setName(this.getDisplayName());
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (MINIBOSS.equals(key)) {
            this.refreshDimensions(); // bigger hitbox on the client too
        }
    }

    /** The miniboss has a bigger hitbox. */
    @Override
    public EntityDimensions getDimensions(Pose pose) {
        EntityDimensions size = super.getDimensions(pose);
        return this.isMiniboss() ? size.scale(BOSS_SCALE) : size;
    }

    /** Boss bar: updated every tick, shown to players who can see the miniboss. */
    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        this.bossBar.setProgress(this.getHealth() / this.getMaxHealth());
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        if (this.isMiniboss()) {
            this.bossBar.addPlayer(player);
        }
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossBar.removePlayer(player);
    }

    /** The miniboss has its own loot: loot_tables/entities/yeti_boss.json */
    @Override
    protected ResourceLocation getDefaultLootTable() {
        return this.isMiniboss()
                ? new ResourceLocation(Per_aspera_ad_astra.MOD_ID, "entities/yeti_boss")
                : super.getDefaultLootTable();
    }

    /** The miniboss can't be knocked back by normal hits. */
    @Override
    public void knockback(double strength, double x, double z) {
        if (!this.isMiniboss()) {
            super.knockback(strength, x, z);
        }
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
