package net.example.per_aspera_ad_astra.entity;

import net.example.per_aspera_ad_astra.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.InteractGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.LookAtTradingPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.TradeWithPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Alien villager ("Alien-Dorfbewohner"). Lives in alien villages (worldgen/structure/AlienVillage*).
 *
 * Works like the vanilla wandering trader (extends AbstractVillager): right-click to trade.
 * Currency = Star Fragments. Every alien gets a random selection of trades when it first spawns:
 *   2 "buy" trades (you give Astra resources, you get Star Fragments)
 *   3 "sell" trades (you pay Star Fragments, you get gear)
 *   40% chance of 1 rare trade
 * The trade lists are below (BUYS / SELLS / RARE) - add or change trades there.
 *
 * Stays near its village (restricted to 24 blocks around where it spawned), runs away from monsters,
 * never despawns. Test: /summon per_aspera_ad_astra:alien_villager
 */
public class AlienVillagerEntity extends AbstractVillager {

    // ---- Trades: offer(what you pay, count, what you get, count, how often before it runs out) ----
    private static final List<Supplier<MerchantOffer>> BUYS = List.of(
            () -> buy(ModItems.FOX_MEAT.get(), 10, 1),
            () -> buy(item("schwefel"), 16, 1),
            () -> buy(item("frostkristall"), 12, 1),
            () -> buy(item("eisholz"), 8, 1),
            () -> buy(ModItems.CRYOTITAN_INGOT.get(), 2, 3)
    );
    private static final List<Supplier<MerchantOffer>> SELLS = List.of(
            () -> sell(2, ModItems.OXYGEN_CANISTER.get(), 3, 8),
            () -> sell(3, Items.IRON_INGOT, 4, 12),
            () -> sell(2, ModItems.FOX_MEAT_COOKED.get(), 4, 12),
            () -> sell(5, ModItems.CRYOTITAN_INGOT.get(), 1, 6),
            () -> sell(8, ModItems.HEATER.get(), 1, 2),
            () -> sell(10, ModItems.SPACESUIT_HELMET.get(), 1, 2),
            () -> sell(10, ModItems.SPACESUIT_BOOTS.get(), 1, 2),
            () -> sell(1, Items.GLASS_BOTTLE, 3, 12)
    );
    private static final List<Supplier<MerchantOffer>> RARE = List.of(
            () -> sell(14, ModItems.FREEZER.get(), 1, 1),
            () -> sell(16, ModItems.LIVER_OF_FROST.get(), 1, 1),
            () -> sell(10, ModItems.FROST_BAIT.get(), 1, 2),
            () -> sell(20, ModItems.SPACESUIT_CHESTPLATE.get(), 1, 1)
    );

    public AlienVillagerEntity(EntityType<? extends AbstractVillager> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractVillager.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.5D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TradeWithPlayerGoal(this));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Monster.class, 8.0F, 0.5D, 0.5D));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, YetiEntity.class, 12.0F, 0.5D, 0.6D));
        this.goalSelector.addGoal(1, new PanicGoal(this, 0.5D));
        this.goalSelector.addGoal(1, new LookAtTradingPlayerGoal(this));
        this.goalSelector.addGoal(4, new MoveTowardsRestrictionGoal(this, 0.35D));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.35D));
        this.goalSelector.addGoal(9, new InteractGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0F));
    }

    /** Keeps the alien near its home (the village). Called when it spawns. */
    public void setHome(BlockPos home) {
        this.restrictTo(home, 24);
    }

    // =====================================================================
    // Trading
    // =====================================================================

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.isAlive() && !this.isTrading() && !this.isBaby()) {
            if (hand == InteractionHand.MAIN_HAND) {
                player.awardStat(Stats.TALKED_TO_VILLAGER);
            }
            if (this.getOffers().isEmpty()) {
                this.setUnhappy();
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
            if (!this.level().isClientSide) {
                this.setTradingPlayer(player);
                this.openTradingScreen(player, this.getDisplayName(), 1);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    private void setUnhappy() {
        this.setUnhappyCounter(40);
        if (!this.level().isClientSide()) {
            this.playSound(SoundEvents.VILLAGER_NO, this.getSoundVolume(), this.getVoicePitch());
        }
    }

    /** Fills the trades the first time someone looks at them. */
    @Override
    protected void updateTrades() {
        MerchantOffers offers = this.getOffers();
        RandomSource random = this.random;
        pick(offers, BUYS, 2, random);
        pick(offers, SELLS, 3, random);
        if (random.nextFloat() < 0.4F) {
            pick(offers, RARE, 1, random);
        }
    }

    private static void pick(MerchantOffers offers, List<Supplier<MerchantOffer>> pool, int count, RandomSource random) {
        List<Supplier<MerchantOffer>> shuffled = new ArrayList<>(pool);
        Collections.shuffle(shuffled, new java.util.Random(random.nextLong()));
        for (int i = 0; i < Math.min(count, shuffled.size()); i++) {
            offers.add(shuffled.get(i).get());
        }
    }

    /** You give {@code count} x {@code item}, you get {@code stars} Star Fragments. */
    private static MerchantOffer buy(Item item, int count, int stars) {
        return new MerchantOffer(new ItemStack(item, count),
                new ItemStack(ModItems.STAR_FRAGMENT.get(), stars), 12, 2, 0.05F);
    }

    /** You pay {@code stars} Star Fragments, you get {@code count} x {@code item}. */
    private static MerchantOffer sell(int stars, Item item, int count, int maxUses) {
        return new MerchantOffer(new ItemStack(ModItems.STAR_FRAGMENT.get(), stars),
                new ItemStack(item, count), maxUses, 5, 0.05F);
    }

    /** Blocks of our mod as items (by name, so this file doesn't need ModBlocks). */
    private static Item item(String name) {
        return net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(
                new net.minecraft.resources.ResourceLocation(net.example.per_aspera_ad_astra.Per_aspera_ad_astra.MOD_ID, name));
    }

    /** XP orbs after a trade, like the wandering trader. */
    @Override
    protected void rewardTradeXp(MerchantOffer offer) {
        if (offer.shouldRewardExp()) {
            int xp = 3 + this.random.nextInt(4);
            this.level().addFreshEntity(new ExperienceOrb(this.level(), this.getX(), this.getY() + 0.5D, this.getZ(), xp));
        }
    }

    @Override
    public boolean showProgressBar() {
        return false;
    }

    /** No alien babies. */
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    // ---- Sounds: villager sounds, but higher (alien voice) ----
    @Override
    public float getVoicePitch() {
        return 1.6F + (this.random.nextFloat() - 0.5F) * 0.2F;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isTrading() ? SoundEvents.VILLAGER_TRADE : SoundEvents.VILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.VILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.VILLAGER_DEATH;
    }

    @Override
    protected SoundEvent getTradeUpdatedSound(boolean yes) {
        return yes ? SoundEvents.VILLAGER_YES : SoundEvents.VILLAGER_NO;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.VILLAGER_YES;
    }
}
