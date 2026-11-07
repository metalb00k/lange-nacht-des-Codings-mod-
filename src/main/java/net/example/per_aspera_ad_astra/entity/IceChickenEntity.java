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
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Ice chicken ("Eishuhn").
 *
 * It extends the vanilla Chicken and uses the vanilla chicken model (only the texture is new:
 * textures/entity/ice_chicken.png). So it flutters down slowly, lays eggs, panics when hit.
 *
 * Differences to the normal chicken:
 *  - there are no seeds on Astra, so it is bred/lured with Eisblatt (ice leaves) - seeds still work too
 *  - babies are ice chickens (the eggs it lays are normal vanilla eggs)
 *  - spawns naturally in Astra Fields and the Eiswald (worldgen/biome/*.json)
 *  - drops: loot_tables/entities/ice_chicken.json (feathers + chicken, like a chicken)
 */
public class IceChickenEntity extends Chicken {

    public IceChickenEntity(EntityType<? extends Chicken> type, Level level) {
        super(type, level);
    }

    /** Same stats as a normal chicken. */
    public static AttributeSupplier.Builder createAttributes() {
        return Chicken.createAttributes();
    }

    /** Eisblatt (ice leaves) or the vanilla seeds. */
    private static Ingredient food() {
        return Ingredient.of(new ItemStack(Items.WHEAT_SEEDS), new ItemStack(Items.MELON_SEEDS),
                new ItemStack(Items.PUMPKIN_SEEDS), new ItemStack(Items.BEETROOT_SEEDS),
                new ItemStack(ForgeRegistries.ITEMS.getValue(new net.minecraft.resources.ResourceLocation(
                        net.example.per_aspera_ad_astra.Per_aspera_ad_astra.MOD_ID, "eisblatt"))));
    }

    /** Same goals as the vanilla chicken, but lured with Eisblatt too. */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.4D));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.0D, food(), false));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.1D));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return food().test(stack);
    }

    /** Babies are ice chickens (the vanilla chicken would make a normal chicken). */
    @Override
    public Chicken getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return ModEntities.ICE_CHICKEN.get().create(level);
    }
}
