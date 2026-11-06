package net.example.per_aspera_ad_astra.worldgen.feature;

import com.mojang.serialization.Codec;
import net.example.per_aspera_ad_astra.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Thermal caves: the deep caves of Astra are lined with Thermisches Gestein, with Schwefel deposits
 * (mostly on the floor) and Cryo-Titan ore in the walls. Cryo-Titan ONLY generates here.
 *
 * Runs once per chunk AFTER the caves are carved (biome step "underground_decoration").
 * It looks at every stone block next to a cave (air or fluid) and replaces it.
 * Used by worldgen/configured_feature/thermal_caves.json.
 */
public class ThermalCaveFeature extends Feature<NoneFeatureConfiguration> {
    /** Below this height caves are always thermal. */
    private static final int FULL_HEAT_Y = 35;
    /** Above this height caves are normal. Between the two, more and more of the wall becomes thermal. */
    private static final int NO_HEAT_Y = 55;
    /** Lowest y to check (bedrock is at 0-5). */
    private static final int MIN_Y = 5;

    private static final float SULFUR_CHANCE_FLOOR = 0.30f; // Schwefel on cave floors
    private static final float SULFUR_CHANCE_WALL = 0.06f;  // Schwefel on walls / ceilings
    private static final float CRYO_CHANCE = 0.012f;        // Cryo-Titan ore in the walls (1.2%)

    public ThermalCaveFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        int chunkX = origin.getX() & ~15;
        int chunkZ = origin.getZ() & ~15;
        int minY = Math.max(MIN_Y, level.getMinBuildHeight() + MIN_Y);

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos neighbor = new BlockPos.MutableBlockPos();
        boolean placedAny = false;

        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                for (int y = minY; y < NO_HEAT_Y; y++) {
                    pos.set(chunkX + dx, y, chunkZ + dz);
                    BlockState state = level.getBlockState(pos);
                    if (!state.is(BlockTags.BASE_STONE_OVERWORLD)) {
                        continue; // only replace natural stone (Eisstein), not ores / other blocks
                    }

                    // Is this stone touching a cave (air)? Remember if the open side is above (= cave floor)
                    boolean touchesCave = false;
                    boolean isFloor = false;
                    for (Direction dir : Direction.values()) {
                        neighbor.setWithOffset(pos, dir);
                        BlockState n = level.getBlockState(neighbor);
                        if (n.isAir()) { // only dry caves (not the ocean floor / flooded caves)
                            touchesCave = true;
                            if (dir == Direction.UP) isFloor = true;
                        }
                    }
                    if (!touchesCave) continue;

                    // Higher up = less heat (smooth transition between FULL_HEAT_Y and NO_HEAT_Y)
                    if (y > FULL_HEAT_Y) {
                        float heat = 1f - (y - FULL_HEAT_Y) / (float) (NO_HEAT_Y - FULL_HEAT_Y);
                        if (random.nextFloat() > heat) continue;
                    }

                    float roll = random.nextFloat();
                    if (roll < CRYO_CHANCE) {
                        placeCryoCluster(level, random, pos);
                    } else if (roll < CRYO_CHANCE + (isFloor ? SULFUR_CHANCE_FLOOR : SULFUR_CHANCE_WALL)) {
                        level.setBlock(pos, ModBlocks.SCHWEFEL.get().defaultBlockState(), 2);
                    } else {
                        level.setBlock(pos, ModBlocks.THERMISCHESGESTEIN.get().defaultBlockState(), 2);
                    }
                    placedAny = true;
                }
            }
        }
        return placedAny;
    }

    /** One visible Cryo-Titan block in the wall, plus 0-3 more hidden behind it in the rock. */
    private static void placeCryoCluster(WorldGenLevel level, RandomSource random, BlockPos start) {
        level.setBlock(start, ModBlocks.CRYOTITAN.get().defaultBlockState(), 2);
        int extra = random.nextInt(4);
        for (int i = 0; i < extra; i++) {
            BlockPos p = start.offset(random.nextInt(3) - 1, random.nextInt(3) - 1, random.nextInt(3) - 1);
            if (level.getBlockState(p).is(BlockTags.BASE_STONE_OVERWORLD)) {
                level.setBlock(p, ModBlocks.CRYOTITAN.get().defaultBlockState(), 2);
            }
        }
    }
}
