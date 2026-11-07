package net.example.per_aspera_ad_astra.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Ice cow ("Eiskuh").
 *
 * It extends the vanilla Cow and uses the vanilla cow model (only the texture is new:
 * textures/entity/ice_cow.png). So it moos, can be milked with a bucket, follows its parents, panics when hit.
 *
 * Differences to the normal cow:
 *  - there is no wheat on Astra, so it is bred/lured with Eisblatt (ice leaves) - wheat still works too
 *  - babies are ice cows
 *  - spawns naturally in Astra Fields and the Eiswald (worldgen/biome/*.json)
 *  - drops: loot_tables/entities/ice_cow.json (beef + leather, like a cow)
 */
public class IceCowEntity extends Cow {

    public IceCowEntity(EntityType<? extends Cow> type, Level level) {
        super(type, level);
    }

    /** Same stats as a normal cow. */
    public static AttributeSupplier.Builder createAttributes() {
        return Cow.createAttributes();
    }

    /** Eisblatt (ice leaves) or wheat. */
    private static Ingredient food() {
        return Ingredient.of(new ItemStack(Items.WHEAT), new ItemStack(ForgeRegistries.ITEMS.getValue(
                new net.minecraft.resources.ResourceLocation(net.example.per_aspera_ad_astra.Per_aspera_ad_astra.MOD_ID, "eisblatt"))));
    }

    /** Same goals as the vanilla cow, but lured with Eisblatt too. */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 2.0D));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.25D, food(), false));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.25D));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return food().test(stack);
    }

    /** Babies are ice cows (the vanilla cow would make a normal cow). */
    @Override
    public Cow getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return ModEntities.ICE_COW.get().create(level);
    }
}
