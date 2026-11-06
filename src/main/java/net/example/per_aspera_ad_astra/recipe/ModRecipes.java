package net.example.per_aspera_ad_astra.recipe;

import net.example.per_aspera_ad_astra.Per_aspera_ad_astra;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Custom recipe types. Used in JSON as "type": "per_aspera_ad_astra:spacesuit_upgrade". */
public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Per_aspera_ad_astra.MOD_ID);

    public static final RegistryObject<RecipeSerializer<SpacesuitUpgradeRecipe>> SPACESUIT_UPGRADE =
            SERIALIZERS.register("spacesuit_upgrade", SpacesuitUpgradeRecipe.Serializer::new);

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
    }
}
