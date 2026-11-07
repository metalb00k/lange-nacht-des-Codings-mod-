package net.example.per_aspera_ad_astra.worldgen.structure;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

/**
 * Alien village: a plaza in the middle and 4-6 dome huts around it, each with aliens to trade with.
 *   /locate structure per_aspera_ad_astra:alien_village
 *   /place structure per_aspera_ad_astra:alien_village
 *
 * This class decides WHERE the buildings go; {@link AlienVillagePiece} builds them.
 * JSON: worldgen/structure/alien_village.json (biomes, terrain flattening), worldgen/structure_set/alien_village.json (how often).
 */
public class AlienVillageStructure extends Structure {
    public static final Codec<AlienVillageStructure> CODEC = simpleCodec(AlienVillageStructure::new);

    public AlienVillageStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        int cx = chunk.getMiddleBlockX();
        int cz = chunk.getMiddleBlockZ();
        Integer cy = dryGroundHeight(context, cx, cz);
        if (cy == null) {
            return Optional.empty(); // no village on water
        }

        WorldgenRandom random = context.random();
        BlockPos home = new BlockPos(cx, cy + 1, cz);
        long plazaSeed = random.nextLong();
        int huts = 4 + random.nextInt(3);
        double startAngle = random.nextDouble() * Math.PI * 2;

        // Work out all huts first (every value from the random generator, in a fixed order)
        int[][] hutData = new int[huts][];
        for (int i = 0; i < huts; i++) {
            double angle = startAngle + Math.PI * 2 * i / huts + (random.nextDouble() - 0.5) * 0.5;
            int dist = 11 + random.nextInt(4);
            int radius = random.nextInt(3) == 0 ? 4 : 3;
            long seed = random.nextLong();
            int hx = cx + (int) Math.round(Math.cos(angle) * dist);
            int hz = cz + (int) Math.round(Math.sin(angle) * dist);
            Integer hy = dryGroundHeight(context, hx, hz);
            if (hy == null || Math.abs(hy - cy) > 12) {
                continue; // skip huts on water or way up a mountain
            }
            // Door points at the plaza (along the bigger axis)
            int ddx = cx - hx, ddz = cz - hz;
            int doorX = Math.abs(ddx) >= Math.abs(ddz) ? Integer.signum(ddx) : 0;
            int doorZ = doorX == 0 ? Integer.signum(ddz) : 0;
            hutData[i] = new int[]{hx, hy, hz, radius, doorX, doorZ, (int) (seed >> 32), (int) seed};
        }

        return Optional.of(new GenerationStub(new BlockPos(cx, cy, cz), builder -> {
            builder.addPiece(new AlienVillagePiece(AlienVillagePiece.PLAZA, plazaSeed, cx, cy, cz, 0, 0, 0, home));
            for (int[] h : hutData) {
                if (h != null) {
                    long seed = ((long) h[6] << 32) | (h[7] & 0xFFFFFFFFL);
                    builder.addPiece(new AlienVillagePiece(AlienVillagePiece.HUT, seed, h[0], h[1], h[2], h[3], h[4], h[5], home));
                }
            }
        }));
    }

    /** Height of the ground at x/z, or null if the top is water. */
    private static Integer dryGroundHeight(GenerationContext context, int x, int z) {
        int y = context.chunkGenerator().getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG,
                context.heightAccessor(), context.randomState());
        var column = context.chunkGenerator().getBaseColumn(x, z, context.heightAccessor(), context.randomState());
        if (!column.getBlock(y).getFluidState().isEmpty() || !column.getBlock(y + 1).getFluidState().isEmpty()) {
            return null;
        }
        return y;
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.ALIEN_VILLAGE.get();
    }
}
