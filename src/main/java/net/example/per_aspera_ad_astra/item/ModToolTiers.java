package net.example.per_aspera_ad_astra.item;

import net.example.per_aspera_ad_astra.Per_aspera_ad_astra;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.TierSortingRegistry;

import java.util.List;

/**
 * Tool tiers of the mod (how strong pickaxes/swords/... made of a material are).
 *
 * For comparison, Netherite is: level 4, 2031 uses, speed 9.0, damage bonus 4.0, enchantability 15.
 */
public class ModToolTiers {
    /** Blocks in this tag can only be mined with Cryo-Titan tools (or better). Empty for now. */
    public static final TagKey<Block> NEEDS_CRYO_TOOL =
            BlockTags.create(new ResourceLocation(Per_aspera_ad_astra.MOD_ID, "needs_cryo_tool"));

    public static final Tier CRYO_TITAN = TierSortingRegistry.registerTier(
            new ForgeTier(
                    5,      // mining level (Netherite = 4)
                    2500,   // durability   (Netherite = 2031)
                    10.0f,  // mining speed (Netherite = 9.0)
                    5.0f,   // damage bonus (Netherite = 4.0)
                    18,     // enchantability (Netherite = 15)
                    NEEDS_CRYO_TOOL,
                    () -> Ingredient.of(ModItems.CRYOTITAN_INGOT.get())), // repair in anvil with Cryo-Titan ingots
            new ResourceLocation(Per_aspera_ad_astra.MOD_ID, "cryo_titan"),
            List.of(Tiers.NETHERITE), // ranks above Netherite
            List.of());
}
