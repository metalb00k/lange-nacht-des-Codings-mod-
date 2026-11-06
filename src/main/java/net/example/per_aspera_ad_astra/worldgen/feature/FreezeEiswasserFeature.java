package net.example.per_aspera_ad_astra.worldgen.feature;

import com.mojang.serialization.Codec;
import net.example.per_aspera_ad_astra.fluid.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Covers every Eiswasser surface in a chunk with ONE layer of ice
 * (like vanilla "freeze_top_layer", which only works on normal water).
 *
 * Used by worldgen/configured_feature/freeze_eiswasser.json -> placed in the biome's last feature step.
 */
public class FreezeEiswasserFeature extends Feature<NoneFeatureConfiguration> {

    public FreezeEiswasserFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        BlockPos.MutableBlockPos surface = new BlockPos.MutableBlockPos();
        boolean placedAny = false;

        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                // Highest block that blocks motion or is a fluid -> the water surface
                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z) - 1;
                surface.set(x, y, z);

                if (level.getFluidState(surface).is(ModFluids.SOURCE_EISWASSER.get())
                        && level.isEmptyBlock(surface.above())) {
                    level.setBlock(surface, Blocks.ICE.defaultBlockState(), 2);
                    placedAny = true;
                }
            }
        }
        return placedAny;
    }
}
