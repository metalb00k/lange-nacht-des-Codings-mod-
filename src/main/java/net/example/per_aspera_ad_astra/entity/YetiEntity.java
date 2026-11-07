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
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import net.example.per_aspera_ad_astra.event.ModAdvancements;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
 *  - earthquake slam when the target keeps its distance (YetiSlamGoal + startEarthquake)
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
        this.tickEarthquake();
    }

    // =====================================================================
    // Earthquake slam (started by YetiSlamGoal): a ring of breaking ground rolls outward,
    // 1 block per tick. Everything standing ON THE GROUND in the ring gets hurt and thrown up.
    // Jumping over the wave dodges it!
    // =====================================================================
    public static final int QUAKE_RADIUS = 8, BOSS_QUAKE_RADIUS = 12;   // how far the wave travels (blocks)
    public static final float QUAKE_DAMAGE = 8.0F, BOSS_QUAKE_DAMAGE = 12.0F;

    private int quakeRadius = -1; // current ring radius, -1 = no wave running
    private Vec3 quakeCenter = Vec3.ZERO;
    private final Set<UUID> quakeHit = new HashSet<>(); // every entity is hit only once per wave

    public boolean isEarthquakeRunning() {
        return quakeRadius >= 0;
    }

    /** The slam itself: big impact at the yeti's feet, then the wave starts. */
    public void startEarthquake() {
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        quakeCenter = this.position();
        quakeRadius = 0;
        quakeHit.clear();
        this.level().broadcastEntityEvent(this, (byte) 4); // iron golem arm-swing animation
        server.playSound(null, getX(), getY(), getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 2.0F, 0.5F);
        server.playSound(null, getX(), getY(), getZ(), SoundEvents.POLAR_BEAR_WARNING, SoundSource.HOSTILE, 3.0F, 0.5F);
        server.sendParticles(ParticleTypes.EXPLOSION, getX(), getY() + 0.2, getZ(), 3, 1.0, 0.1, 1.0, 0.0);
    }

    private void tickEarthquake() {
        if (quakeRadius < 0 || !(this.level() instanceof ServerLevel server)) {
            return;
        }
        quakeRadius++;
        int maxRadius = isMiniboss() ? BOSS_QUAKE_RADIUS : QUAKE_RADIUS;
        if (quakeRadius > maxRadius) {
            quakeRadius = -1;
            return;
        }
        double r = quakeRadius;

        // Visuals: ground blocks burst up along the ring
        int points = (int) Math.ceil(2 * Math.PI * r * 1.5);
        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            double x = quakeCenter.x + Math.cos(angle) * r;
            double z = quakeCenter.z + Math.sin(angle) * r;
            BlockPos ground = findGround(BlockPos.containing(x, quakeCenter.y, z));
            if (ground == null) {
                continue;
            }
            BlockState state = server.getBlockState(ground);
            server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state),
                    x, ground.getY() + 1.1, z, 5, 0.2, 0.1, 0.2, 0.15);
            if (i % 4 == 0) {
                server.sendParticles(ParticleTypes.CLOUD, x, ground.getY() + 1.1, z, 1, 0.1, 0.1, 0.1, 0.02);
            }
            if (i == 0 && quakeRadius % 2 == 0) {
                server.playSound(null, x, ground.getY() + 1, z, state.getSoundType().getBreakSound(),
                        SoundSource.HOSTILE, 1.5F, 0.6F);
            }
        }

        // Damage: entities on the ground inside the ring band (r-1 .. r+0.5)
        float damage = isMiniboss() ? BOSS_QUAKE_DAMAGE : QUAKE_DAMAGE;
        AABB area = new AABB(quakeCenter, quakeCenter).inflate(r + 1, 2.0, r + 1);
        for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, area, this::canQuakeHit)) {
            double dx = victim.getX() - quakeCenter.x, dz = victim.getZ() - quakeCenter.z;
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist < r - 1.0 || dist > r + 0.5 || !quakeHit.add(victim.getUUID())) {
                continue;
            }
            victim.hurt(this.damageSources().mobAttack(this), damage);
            if (victim instanceof ServerPlayer player && player.isDeadOrDying()) {
                ModAdvancements.award(player, ModAdvancements.DUNKED_ON); // killed by the shockwave
            }
            // Thrown up and away from the yeti
            double len = Math.max(dist, 0.1);
            victim.push(dx / len * 0.6, 0.7, dz / len * 0.6);
            victim.hurtMarked = true; // send the new motion to players
        }
    }

    private boolean canQuakeHit(LivingEntity e) {
        return e != this && !(e instanceof YetiEntity) && e.isAlive() && !e.isSpectator()
                && e.onGround() // in the air = dodged
                && !(e instanceof Player p && p.isCreative())
                && Math.abs(e.getY() - quakeCenter.y) < 2.5;
    }

    /** The top solid block near the wave's height (so the wave follows small hills). */
    private BlockPos findGround(BlockPos start) {
        for (int dy = 1; dy >= -3; dy--) {
            BlockPos pos = start.offset(0, dy - 1, 0);
            if (!level().getBlockState(pos).isAir() && level().getBlockState(pos.above()).isAir()) {
                return pos;
            }
        }
        return null;
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
        this.goalSelector.addGoal(0, new YetiSlamGoal(this)); // earthquake slam, interrupts walking/punching
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
