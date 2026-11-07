package net.example.per_aspera_ad_astra.entity;

import net.example.per_aspera_ad_astra.block.ModBlocks;
import net.example.per_aspera_ad_astra.fluid.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * The Great Frostworm ("Der Große Frostwurm") – boss of the Eiswasser lakes.
 *
 * Summoned by throwing Frost Bait into Eiswasser (see item/custom/FrostBaitItem).
 * Test it with: /summon per_aspera_ad_astra:great_frostworm
 *
 * Attacks:
 *  - BITE (close): big damage, freezes + slows the target
 *  - ICE BARRAGE (range 5-28 blocks): rears up, then spits a fan of ice shards
 *  - FROST NOVA (phase 2 = below 50% health): rears up, then freezes, hurts and
 *    knocks back everything within 10 blocks
 *  - BREACH LEAP (only from Eiswasser, target 4-24 blocks away): dives, jumps out of the water
 *    in an arc at the target, smashes all ice on the way and SLAMS when it lands
 *    (also happens when it is summoned)
 * Other:
 *  - smashes through Methaneis it touches, swims in Eiswasser, immune to cold
 *  - boss bar, never despawns, drops the Liver of Frost (loot_tables/entities/great_frostworm.json)
 */
public class GreatFrostwormEntity extends Monster {
    // ---- Stats (tweak here) ----
    public static final double MAX_HEALTH = 300.0D;
    public static final double BITE_DAMAGE = 8.0D;
    public static final int BARRAGE_SHARDS = 4, BARRAGE_SHARDS_PHASE2 = 6;
    public static final float SHARD_DAMAGE = 3.0F;   // per ice shard (the player's freezer shards do 5)
    public static final int BARRAGE_COOLDOWN = 80, BARRAGE_COOLDOWN_PHASE2 = 50;   // ticks (20 = 1 s)
    public static final int NOVA_COOLDOWN = 200;
    public static final double NOVA_RADIUS = 10.0D;
    public static final float NOVA_DAMAGE = 5.0F;
    private static final int BARRAGE_CAST_TIME = 20, NOVA_CAST_TIME = 30;           // wind-up before the attack
    // Breach leap: out of the Eiswasser, through the ice, slam on landing
    public static final int LEAP_COOLDOWN = 140, LEAP_COOLDOWN_PHASE2 = 90;
    public static final double SLAM_RADIUS = 6.0D;
    public static final float SLAM_DAMAGE = 7.0F;
    private static final int LEAP_CAST_TIME = 12;   // dives down before it jumps
    private static final double LEAP_MIN_DIST = 4.0D, LEAP_MAX_DIST = 24.0D;

    private static final int CAST_NONE = 0, CAST_BARRAGE = 1, CAST_NOVA = 2, CAST_LEAP = 3;
    private static final EntityDataAccessor<Integer> CAST =
            SynchedEntityData.defineId(GreatFrostwormEntity.class, EntityDataSerializers.INT);

    private final ServerBossEvent bossBar = (ServerBossEvent) new ServerBossEvent(
            Component.translatable("entity.per_aspera_ad_astra.great_frostworm"),
            BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.NOTCHED_10).setDarkenScreen(true);

    private int castTicks;
    private int barrageCooldown = 60;
    private int novaCooldown = 100;
    private int leapCooldown = 40;
    private boolean phase2;
    // Leap state: while in the air
    private boolean leaping;
    private int leapTicks;
    private Vec3 leapVelocity = Vec3.ZERO;

    // Client only: smooth "rearing up" value for the model (0 = normal, 1 = reared up)
    private float rear, rearO;

    public GreatFrostwormEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 150;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ATTACK_DAMAGE, BITE_DAMAGE)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this)); // stays at the water surface
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(CAST, CAST_NONE);
    }

    private int getCast() {
        return this.entityData.get(CAST);
    }

    /** For the model: how far it is reared up (0..1), smoothed between ticks. */
    public float getRearAmount(float partialTick) {
        return Mth.lerp(partialTick, rearO, rear);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            rearO = rear;
            float goal = getCast() != CAST_NONE ? 1.0F : 0.0F;
            rear += Mth.clamp(goal - rear, -0.15F, 0.15F);
        }
    }

    // =====================================================================
    // Special attacks (server side)
    // =====================================================================
    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        bossBar.setProgress(this.getHealth() / this.getMaxHealth());

        if (!phase2 && this.getHealth() <= this.getMaxHealth() / 2) {
            enterPhase2();
        }
        if (this.tickCount % 5 == 0) {
            smashIce(this.getBoundingBox().inflate(0.5));
        }

        if (barrageCooldown > 0) barrageCooldown--;
        if (novaCooldown > 0) novaCooldown--;
        if (leapCooldown > 0) leapCooldown--;

        // In the air after a breach leap: break ice on the way, slam when landing
        if (leaping) {
            tickLeap();
            return;
        }

        // Casting: wind up, then release
        if (getCast() != CAST_NONE) {
            this.getNavigation().stop();
            LivingEntity target = this.getTarget();
            if (target != null) {
                this.getLookControl().setLookAt(target, 30.0F, 30.0F);
            }
            if (getCast() == CAST_LEAP) {
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.5, 0, 0.5).add(0, -0.15, 0)); // dive
                if (this.tickCount % 3 == 0 && this.level() instanceof ServerLevel server) {
                    server.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, getX(), getY() + 2, getZ(), 15, 1.5, 0.2, 1.5, 0.1);
                }
            }
            if (--castTicks <= 0) {
                if (getCast() == CAST_BARRAGE) {
                    fireBarrage();
                    this.entityData.set(CAST, CAST_NONE);
                } else if (getCast() == CAST_NOVA) {
                    frostNova();
                    this.entityData.set(CAST, CAST_NONE);
                } else if (target != null) {
                    launchLeapAt(target.position()); // CAST stays LEAP (reared up) while flying
                } else {
                    this.entityData.set(CAST, CAST_NONE);
                }
            }
            return;
        }

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }
        double distSq = this.distanceToSqr(target);
        if (leapCooldown <= 0 && isInEiswasser()
                && distSq > LEAP_MIN_DIST * LEAP_MIN_DIST && distSq < LEAP_MAX_DIST * LEAP_MAX_DIST) {
            startCast(CAST_LEAP, LEAP_CAST_TIME);
            leapCooldown = phase2 ? LEAP_COOLDOWN_PHASE2 : LEAP_COOLDOWN;
            this.playSound(SoundEvents.ELDER_GUARDIAN_CURSE, 3.0F, 0.4F);
        } else if (phase2 && novaCooldown <= 0 && distSq < NOVA_RADIUS * NOVA_RADIUS) {
            startCast(CAST_NOVA, NOVA_CAST_TIME);
            novaCooldown = NOVA_COOLDOWN;
            this.playSound(SoundEvents.ELDER_GUARDIAN_CURSE, 3.0F, 0.6F);
        } else if (barrageCooldown <= 0 && distSq > 25.0D && distSq < 28.0D * 28.0D && this.hasLineOfSight(target)) {
            startCast(CAST_BARRAGE, BARRAGE_CAST_TIME);
            barrageCooldown = phase2 ? BARRAGE_COOLDOWN_PHASE2 : BARRAGE_COOLDOWN;
            this.playSound(SoundEvents.ELDER_GUARDIAN_AMBIENT, 3.0F, 0.6F);
        }
    }

    private void startCast(int type, int ticks) {
        this.entityData.set(CAST, type);
        castTicks = ticks;
    }

    /** Where the mouth is: ~3.5 blocks in front of the body, ~2.5 blocks up. */
    private Vec3 mouthPos() {
        Vec3 forward = Vec3.directionFromRotation(0, this.getYHeadRot());
        return this.position().add(forward.scale(3.5)).add(0, 2.5, 0);
    }

    private void fireBarrage() {
        LivingEntity target = this.getTarget();
        if (target == null) {
            return;
        }
        Vec3 mouth = mouthPos();
        int shards = phase2 ? BARRAGE_SHARDS_PHASE2 : BARRAGE_SHARDS;
        for (int i = 0; i < shards; i++) {
            IceShardProjectile shard = new IceShardProjectile(this.level(), this);
            shard.setPos(mouth.x, mouth.y, mouth.z);
            shard.setDamage(SHARD_DAMAGE);
            double dx = target.getX() - mouth.x;
            double dy = target.getY(0.5) - mouth.y;
            double dz = target.getZ() - mouth.z;
            // Fan: high inaccuracy spreads the shards out
            shard.shoot(dx, dy + Math.sqrt(dx * dx + dz * dz) * 0.05, dz, 1.6F, 8.0F);
            this.level().addFreshEntity(shard);
        }
        this.playSound(SoundEvents.SNOW_GOLEM_SHOOT, 3.0F, 0.5F);
        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.SNOWFLAKE, mouth.x, mouth.y, mouth.z, 20, 0.5, 0.5, 0.5, 0.1);
        }
    }

    private void frostNova() {
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        // Expanding ring of snowflakes
        for (int r = 2; r <= NOVA_RADIUS; r += 2) {
            int points = r * 8;
            for (int i = 0; i < points; i++) {
                double a = 2 * Math.PI * i / points;
                server.sendParticles(ParticleTypes.SNOWFLAKE, getX() + Math.cos(a) * r, getY() + 0.5,
                        getZ() + Math.sin(a) * r, 2, 0.1, 0.3, 0.1, 0.02);
            }
        }
        server.sendParticles(ParticleTypes.EXPLOSION, getX(), getY() + 1, getZ(), 4, 1.5, 0.5, 1.5, 0);
        server.playSound(null, getX(), getY(), getZ(), SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 3.0F, 0.5F);
        server.playSound(null, getX(), getY(), getZ(), SoundEvents.PLAYER_HURT_FREEZE, SoundSource.HOSTILE, 3.0F, 0.6F);

        for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(NOVA_RADIUS), e -> e != this && e.isAlive())) {
            if (victim.distanceTo(this) > NOVA_RADIUS || (victim instanceof Player p && (p.isCreative() || p.isSpectator()))) {
                continue;
            }
            victim.hurt(this.damageSources().freeze(), NOVA_DAMAGE);
            if (victim.canFreeze()) {
                victim.setTicksFrozen(victim.getTicksRequiredToFreeze() + 100);
            }
            victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 2));
            Vec3 away = victim.position().subtract(this.position()).multiply(1, 0, 1).normalize();
            victim.push(away.x * 1.2, 0.5, away.z * 1.2);
            victim.hurtMarked = true;
        }
    }

    private void enterPhase2() {
        phase2 = true;
        bossBar.setColor(BossEvent.BossBarColor.PURPLE);
        this.playSound(SoundEvents.ELDER_GUARDIAN_DEATH, 4.0F, 0.5F);
        novaCooldown = 0; // nova right away
        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.SNOWFLAKE, getX(), getY() + 2, getZ(), 150, 3, 2, 3, 0.2);
        }
    }

    // =====================================================================
    // Breach leap: dive -> jump out of the water in an arc at the target,
    // smashing all ice on the way -> slam on landing
    // =====================================================================

    /**
     * Jumps from the current position so it lands at {@code landAt}.
     * Also used when the worm is summoned (it bursts out of the lake at the nearest player).
     */
    public void launchLeapAt(Vec3 landAt) {
        Vec3 flat = landAt.subtract(this.position()).multiply(1, 0, 1);
        // In the air a mob keeps 91% of its sideways speed per tick -> over a ~30 tick jump it travels ~10.6x the start speed
        double speed = Math.min(flat.length() / 10.6D, 2.5D);
        Vec3 dir = flat.lengthSqr() > 0.01 ? flat.normalize() : Vec3.ZERO;
        leapVelocity = new Vec3(dir.x * speed, 1.25D, dir.z * speed);
        leaping = true;
        leapTicks = 0;
        this.entityData.set(CAST, CAST_LEAP);
        this.setDeltaMovement(leapVelocity);
        this.hasImpulse = true;
        this.getNavigation().stop();

        // Break the ice above it so it can get out
        smashIce(this.getBoundingBox().expandTowards(0, 4, 0).inflate(1.0));
        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.SPLASH, getX(), getY() + 1, getZ(), 150, 2.0, 0.5, 2.0, 0.5);
            server.playSound(null, getX(), getY(), getZ(), SoundEvents.GENERIC_SPLASH, SoundSource.HOSTILE, 3.0F, 0.5F);
            server.playSound(null, getX(), getY(), getZ(), SoundEvents.ELDER_GUARDIAN_AMBIENT, SoundSource.HOSTILE, 4.0F, 0.5F);
        }
    }

    private void tickLeap() {
        leapTicks++;
        this.getNavigation().stop();
        // Water slows everything down a lot: keep pushing until it is out
        if (leapTicks < 6 && isInEiswasser()) {
            this.setDeltaMovement(leapVelocity);
        }
        // Break ice where it is about to fly through
        smashIce(this.getBoundingBox().expandTowards(this.getDeltaMovement().scale(2)).inflate(0.5));

        boolean landed = leapTicks > 5 && (this.onGround()
                || (this.getDeltaMovement().y < 0 && isInEiswasser()));
        if (landed || leapTicks > 80) {
            leaping = false;
            this.entityData.set(CAST, CAST_NONE);
            slam();
        }
    }

    /** Landing: shockwave that hurts, throws up and knocks back everything around, and shatters ice. */
    private void slam() {
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        smashIce(this.getBoundingBox().inflate(3.0, 1.0, 3.0));

        BlockState ground = server.getBlockState(this.blockPosition().below());
        if (ground.isAir()) {
            ground = ModBlocks.METHANEIS.get().defaultBlockState();
        }
        for (int r = 1; r <= SLAM_RADIUS; r++) {
            int points = r * 8;
            for (int i = 0; i < points; i++) {
                double a = 2 * Math.PI * i / points;
                server.sendParticles(new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK, ground),
                        getX() + Math.cos(a) * r, getY() + 0.3, getZ() + Math.sin(a) * r, 3, 0.2, 0.2, 0.2, 0.15);
            }
        }
        server.sendParticles(ParticleTypes.EXPLOSION, getX(), getY() + 0.5, getZ(), 6, 2.0, 0.3, 2.0, 0);
        server.sendParticles(ParticleTypes.SPLASH, getX(), getY() + 0.5, getZ(), 100, 3.0, 0.5, 3.0, 0.4);
        server.playSound(null, getX(), getY(), getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 3.0F, 0.5F);
        server.playSound(null, getX(), getY(), getZ(), SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 3.0F, 0.4F);

        for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(SLAM_RADIUS, 2.0, SLAM_RADIUS), e -> e != this && e.isAlive())) {
            if (victim.distanceTo(this) > SLAM_RADIUS + 1.5 || (victim instanceof Player p && (p.isCreative() || p.isSpectator()))) {
                continue;
            }
            victim.hurt(this.damageSources().mobAttack(this), SLAM_DAMAGE);
            Vec3 away = victim.position().subtract(this.position()).multiply(1, 0, 1);
            away = away.lengthSqr() > 0.01 ? away.normalize() : Vec3.ZERO;
            victim.push(away.x * 1.0, 0.8, away.z * 1.0);
            victim.hurtMarked = true;
        }
    }

    /** No fall damage from its own jumps. */
    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    private boolean isInEiswasser() {
        return isEiswasser(this.blockPosition()) || isEiswasser(this.blockPosition().above());
    }

    private boolean isEiswasser(BlockPos pos) {
        var fluid = this.level().getFluidState(pos);
        return fluid.is(ModFluids.SOURCE_EISWASSER.get()) || fluid.is(ModFluids.FLOWING_EISWASSER.get());
    }

    /**
     * Breaks all ice in the area: Methaneis, Eisblock and vanilla ice.
     * If there is Eiswasser below, the hole fills with Eiswasser.
     */
    private void smashIce(net.minecraft.world.phys.AABB area) {
        BlockPos.betweenClosedStream(area).forEach(pos -> {
            BlockState state = this.level().getBlockState(pos);
            if (state.is(ModBlocks.METHANEIS.get()) || state.is(ModBlocks.EISBLOCK.get())
                    || state.is(net.minecraft.tags.BlockTags.ICE)) {
                BlockPos hole = pos.immutable();
                this.level().destroyBlock(hole, false, this);
                if (isEiswasser(hole.below())) {
                    this.level().setBlock(hole, ModBlocks.EISWASSER_BLOCK.get().defaultBlockState(), 3);
                }
            }
        });
    }

    // =====================================================================
    // Bite: freezes the target
    // =====================================================================
    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity living) {
            if (living.canFreeze()) {
                living.setTicksFrozen(Math.max(living.getTicksFrozen(), living.getTicksRequiredToFreeze() + 60));
            }
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
            this.playSound(SoundEvents.EVOKER_FANGS_ATTACK, 2.0F, 0.6F);
        }
        return hit;
    }

    // =====================================================================
    // Immunities: it lives in Eiswasser
    // =====================================================================
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_FREEZING) || source.is(DamageTypeTags.IS_DROWNING)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean canFreeze() {
        return false;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    // ---- Boss bar ----
    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossBar.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossBar.removePlayer(player);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Phase2", phase2);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        phase2 = tag.getBoolean("Phase2");
        if (phase2) {
            bossBar.setColor(BossEvent.BossBarColor.PURPLE);
        }
        if (this.hasCustomName()) {
            bossBar.setName(this.getDisplayName());
        }
    }

    // ---- Sounds: deep elder guardian sounds ----
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ELDER_GUARDIAN_AMBIENT_LAND;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ELDER_GUARDIAN_HURT_LAND;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ELDER_GUARDIAN_DEATH_LAND;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.SILVERFISH_STEP, 1.0F, 0.4F);
    }

    @Override
    public float getVoicePitch() {
        return 0.6F;
    }

    @Override
    protected float getSoundVolume() {
        return 3.0F;
    }
}
