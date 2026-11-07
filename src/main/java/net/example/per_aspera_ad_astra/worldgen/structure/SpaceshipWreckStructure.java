package net.example.per_aspera_ad_astra.worldgen.structure;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

/**
 * Crashed spaceship structure (Schwefelfelder). Findable with
 *   /locate structure per_aspera_ad_astra:spaceship_wreck
 * and placeable with
 *   /place structure per_aspera_ad_astra:spaceship_wreck
 *
 * This class only decides WHERE a wreck goes (on dry land in the middle of the chunk).
 * The building is done by {@link SpaceshipWreckPiece}.
 * JSON: worldgen/structure/spaceship_wreck.json (which biomes), worldgen/structure_set/spaceship_wreck.json (how often).
 */
public class SpaceshipWreckStructure extends Structure {
    public static final Codec<SpaceshipWreckStructure> CODEC = simpleCodec(SpaceshipWreckStructure::new);

    public SpaceshipWreckStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX();
        int z = chunk.getMiddleBlockZ();
        int y = context.chunkGenerator().getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG,
                context.heightAccessor(), context.randomState());

        // Not on lakes / the ocean
        NoiseColumn column = context.chunkGenerator().getBaseColumn(x, z, context.heightAccessor(), context.randomState());
        if (!column.getBlock(y).getFluidState().isEmpty() || !column.getBlock(y + 1).getFluidState().isEmpty()) {
            return Optional.empty();
        }

        WorldgenRandom random = context.random();
        long seed = random.nextLong();
        boolean alongX = random.nextBoolean();
        int dirSign = random.nextBoolean() ? 1 : -1;
        BlockPos pos = new BlockPos(x, y, z);
        return Optional.of(new GenerationStub(pos,
                builder -> builder.addPiece(new SpaceshipWreckPiece(seed, x, y, z, alongX, dirSign))));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.SPACESHIP_WRECK.get();
    }
}
