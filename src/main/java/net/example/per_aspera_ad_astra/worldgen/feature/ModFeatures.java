package net.example.per_aspera_ad_astra.worldgen.feature;

import net.example.per_aspera_ad_astra.Per_aspera_ad_astra;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Custom worldgen feature TYPES written in Java.
 * They are used from JSON with  "type": "per_aspera_ad_astra:<name>"  in worldgen/configured_feature/.
 */
public class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(ForgeRegistries.FEATURES, Per_aspera_ad_astra.MOD_ID);

    /** Puts a thin ice layer on Eiswasser. */
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> FREEZE_EISWASSER =
            FEATURES.register("freeze_eiswasser", () -> new FreezeEiswasserFeature(NoneFeatureConfiguration.CODEC));

    /** Lines deep caves with Thermisches Gestein, Schwefel and Cryo-Titan ore. */
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> THERMAL_CAVES =
            FEATURES.register("thermal_caves", () -> new ThermalCaveFeature(NoneFeatureConfiguration.CODEC));

    public static void register(IEventBus eventBus) {
        FEATURES.register(eventBus);
    }
}
