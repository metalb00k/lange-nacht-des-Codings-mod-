package net.example.per_aspera_ad_astra.worldgen.dimension;

import com.mojang.datafixers.util.Pair;
import net.example.per_aspera_ad_astra.Per_aspera_ad_astra;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

import java.util.List;
import java.util.OptionalLong;

/**
 * NOTE: The dimension and its biomes that are actually used in game come from the JSON files in
 *   src/main/resources/data/per_aspera_ad_astra/dimension/astra_dim.json
 *   src/main/resources/data/per_aspera_ad_astra/worldgen/biome/astra_biome.json
 * Edit those to change the dimension. The bootstrap methods below are only used by a data generator (not set up).
 * The keys (ASTRA_DIM_LEVEL_KEY etc.) ARE used by the portal.
 */
public class ModDimensions {
    public static final ResourceKey<LevelStem> ASTRA_DIM_KEY = ResourceKey.create(Registries.LEVEL_STEM,
            new ResourceLocation(Per_aspera_ad_astra.MOD_ID, "astra_dim"));
    public static final ResourceKey<Level> ASTRA_DIM_LEVEL_KEY = ResourceKey.create(Registries.DIMENSION,
            new ResourceLocation(Per_aspera_ad_astra.MOD_ID, "astra_dim"));
    public static final ResourceKey<DimensionType> ASTRA_DIM_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE,
            new ResourceLocation(Per_aspera_ad_astra.MOD_ID, "astra_dim_type"));


    public static void bootstrapType(BootstapContext<DimensionType> context) {
        context.register(ASTRA_DIM_TYPE, new DimensionType(
                OptionalLong.of(12000), // fixedTime
                false, // hasSkylight
                false, // hasCeiling
                false, // ultraWarm
                false, // natural
                1.0, // coordinateScale
                true, // bedWorks
                false, // respawnAnchorWorks
                0, // minY
                256, // height
                256, // logicalHeight
                BlockTags.INFINIBURN_OVERWORLD, // infiniburn
                BuiltinDimensionTypes.OVERWORLD_EFFECTS, // effectsLocation
                1.0f, // ambientLight
                new DimensionType.MonsterSettings(false, false, ConstantInt.of(0), 0)));
    }

    public static void bootstrapStem(BootstapContext<LevelStem> context) {
        HolderGetter<Biome> biomeRegistry = context.lookup(Registries.BIOME);
        HolderGetter<DimensionType> dimTypes = context.lookup(Registries.DIMENSION_TYPE);
        HolderGetter<NoiseGeneratorSettings> noiseGenSettings = context.lookup(Registries.NOISE_SETTINGS);

        // TODO: replace Biomes.PLAINS with your own biome once you have one (e.g. ModBiomes.SOME_BIOME)
        NoiseBasedChunkGenerator noiseBasedChunkGenerator = new NoiseBasedChunkGenerator(
                MultiNoiseBiomeSource.createFromList(
                        new Climate.ParameterList<>(List.of(
                                Pair.of(
                                        Climate.parameters(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F), biomeRegistry.getOrThrow(Biomes.PLAINS)),
                                Pair.of(
                                        Climate.parameters(0.1F, 0.2F, 0.0F, 0.2F, 0.0F, 0.0F, 0.0F), biomeRegistry.getOrThrow(Biomes.BIRCH_FOREST)),
                                Pair.of(
                                        Climate.parameters(0.3F, 0.6F, 0.1F, 0.1F, 0.0F, 0.0F, 0.0F), biomeRegistry.getOrThrow(Biomes.OCEAN)),
                                Pair.of(
                                        Climate.parameters(0.4F, 0.3F, 0.2F, 0.1F, 0.0F, 0.0F, 0.0F), biomeRegistry.getOrThrow(Biomes.DARK_FOREST))
                        ))),
                noiseGenSettings.getOrThrow(NoiseGeneratorSettings.AMPLIFIED));

        LevelStem stem = new LevelStem(dimTypes.getOrThrow(ModDimensions.ASTRA_DIM_TYPE), noiseBasedChunkGenerator);

        context.register(ASTRA_DIM_KEY, stem);
    }
}
