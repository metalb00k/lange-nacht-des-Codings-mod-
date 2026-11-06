package net.example.per_aspera_ad_astra.recipe;

import com.google.gson.JsonObject;
import net.example.per_aspera_ad_astra.item.custom.SpacesuitChestplateItem;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Shapeless recipe that upgrades the spacesuit chestplate WITHOUT losing its oxygen:
 * the chestplate from the grid is copied and gets the upgrade tag added.
 *
 * JSON = a normal "minecraft:crafting_shapeless" recipe plus
 *   "upgrade":  "heater" or "freezer"      (the tag that gets added)
 *   "requires": "heater"                   (optional: the chestplate must already have this upgrade)
 */
public class SpacesuitUpgradeRecipe extends ShapelessRecipe {
    private final String upgrade;
    @Nullable
    private final String requires;

    public SpacesuitUpgradeRecipe(ShapelessRecipe base, String upgrade, @Nullable String requires) {
        super(base.getId(), base.getGroup(), base.category(), base.getResultItem(RegistryAccess.EMPTY), base.getIngredients());
        this.upgrade = upgrade;
        this.requires = requires;
    }

    private static ItemStack findChestplate(CraftingContainer grid) {
        for (int i = 0; i < grid.getContainerSize(); i++) {
            ItemStack stack = grid.getItem(i);
            if (stack.getItem() instanceof SpacesuitChestplateItem) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean matches(CraftingContainer grid, Level level) {
        if (!super.matches(grid, level)) {
            return false;
        }
        ItemStack chest = findChestplate(grid);
        return !chest.isEmpty()
                && !SpacesuitChestplateItem.hasUpgrade(chest, upgrade)                    // not twice
                && (requires == null || SpacesuitChestplateItem.hasUpgrade(chest, requires));
    }

    @Override
    public ItemStack assemble(CraftingContainer grid, RegistryAccess registryAccess) {
        ItemStack result = findChestplate(grid).copy(); // keeps oxygen, enchantments, damage, ...
        result.setCount(1);
        SpacesuitChestplateItem.addUpgrade(result, upgrade);
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.SPACESUIT_UPGRADE.get();
    }

    public static class Serializer implements RecipeSerializer<SpacesuitUpgradeRecipe> {
        @Override
        public SpacesuitUpgradeRecipe fromJson(ResourceLocation id, JsonObject json) {
            ShapelessRecipe base = RecipeSerializer.SHAPELESS_RECIPE.fromJson(id, json);
            String requires = json.has("requires") ? GsonHelper.getAsString(json, "requires") : null;
            return new SpacesuitUpgradeRecipe(base, GsonHelper.getAsString(json, "upgrade"), requires);
        }

        @Override
        public SpacesuitUpgradeRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            String upgrade = buf.readUtf();
            String requires = buf.readBoolean() ? buf.readUtf() : null;
            ShapelessRecipe base = RecipeSerializer.SHAPELESS_RECIPE.fromNetwork(id, buf);
            return new SpacesuitUpgradeRecipe(base, upgrade, requires);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, SpacesuitUpgradeRecipe recipe) {
            buf.writeUtf(recipe.upgrade);
            buf.writeBoolean(recipe.requires != null);
            if (recipe.requires != null) {
                buf.writeUtf(recipe.requires);
            }
            RecipeSerializer.SHAPELESS_RECIPE.toNetwork(buf, recipe);
        }
    }
}
