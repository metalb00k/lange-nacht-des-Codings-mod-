package net.example.per_aspera_ad_astra.worldgen.structure;

import net.example.per_aspera_ad_astra.Per_aspera_ad_astra;
import net.example.per_aspera_ad_astra.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/**
 * The crashed spaceship itself (built block by block in code, no .nbt file):
 *  - scorched crater of Thermisches Gestein around it
 *  - ~15 block long hull of Titanblock / iron, nose buried in the ground, random holes + one big breach
 *  - cockpit glass, a tail fin, one intact wing and one snapped-off wing, debris lying around
 *  - smoking engines (campfire) and glowing Thermisches Gestein exhausts
 *  - 1-2 loot chests inside: loot_tables/chests/spaceship_wreck.json (can contain Frost Bait!)
 *
 * Minecraft builds a structure one chunk at a time (postProcess is called once per chunk it touches).
 * So every random choice comes from a random generator with the SAME seed each time,
 * and only blocks inside the current chunk (chunkBox) are placed.
 *
 * Coordinates in here are "ship coordinates":
 *   a = along the ship (-8 = engines, +7 = nose), y = up from the floor, s = sideways (-2..2 = hull width)
 */
public class SpaceshipWreckPiece extends StructurePiece {
    public static final ResourceLocation LOOT = new ResourceLocation(Per_aspera_ad_astra.MOD_ID, "chests/spaceship_wreck");

    private static final int CRATER_RADIUS = 9;
    private static final float HULL_DAMAGE = 0.15F; // chance for each hull block to be missing

    // Saved with the structure
    private final long seed;
    private final int ox, baseY, oz;
    private final boolean alongX;
    private final int dirSign;

    // Only valid during postProcess
    private WorldGenLevel level;
    private RandomSource random;
    private BoundingBox chunkBox;

    public SpaceshipWreckPiece(long seed, int x, int y, int z, boolean alongX, int dirSign) {
        super(ModStructures.SPACESHIP_WRECK_PIECE.get(), 0,
                new BoundingBox(x - 10, y - 6, z - 10, x + 10, y + 9, z + 10));
        this.seed = seed;
        this.ox = x;
        this.baseY = y;
        this.oz = z;
        this.alongX = alongX;
        this.dirSign = dirSign;
    }

    /** Loading from the save file. */
    public SpaceshipWreckPiece(CompoundTag tag) {
        super(ModStructures.SPACESHIP_WRECK_PIECE.get(), tag);
        this.seed = tag.getLong("Seed");
        this.ox = tag.getInt("OX");
        this.baseY = tag.getInt("OY");
        this.oz = tag.getInt("OZ");
        this.alongX = tag.getBoolean("AlongX");
        this.dirSign = tag.getInt("Dir");
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putLong("Seed", seed);
        tag.putInt("OX", ox);
        tag.putInt("OY", baseY);
        tag.putInt("OZ", oz);
        tag.putBoolean("AlongX", alongX);
        tag.putInt("Dir", dirSign);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
                            RandomSource unusedRandom, BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        this.level = level;
        this.chunkBox = chunkBox;
        this.random = RandomSource.create(seed); // same sequence in every chunk

        scorchCrater();
        buildHull();
        buildWingsAndFin();
        buildEngines();
        placeLoot();
        scatterDebris();
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    /** The nose is buried: the further to the front, the deeper. */
    private int tilt(int a) {
        return a > 1 ? -((a - 1) / 2) : 0;
    }

    private BlockPos pos(int a, int y, int s) {
        int worldY = baseY + y + tilt(a);
        return alongX
                ? new BlockPos(ox + dirSign * a, worldY, oz + s)
                : new BlockPos(ox + s, worldY, oz + dirSign * a);
    }

    /** Places a block, but only if it is in the chunk that is being generated right now. */
    private void set(int a, int y, int s, BlockState state) {
        put(pos(a, y, s), state);
    }

    private void put(BlockPos p, BlockState state) {
        if (chunkBox.isInside(p)) {
            level.setBlock(p, state, 2);
        }
    }

    private boolean inChunkColumn(int x, int z) {
        return x >= chunkBox.minX() && x <= chunkBox.maxX() && z >= chunkBox.minZ() && z <= chunkBox.maxZ();
    }

    private BlockState hullBlock() {
        return random.nextFloat() < 0.7F ? ModBlocks.TITANBLOCK.get().defaultBlockState()
                : Blocks.IRON_BLOCK.defaultBlockState();
    }

    // ---------------------------------------------------------------------
    // Parts of the wreck
    // ---------------------------------------------------------------------

    /** Burnt ground: Thermisches Gestein, solid in the middle, patchy towards the edge. */
    private void scorchCrater() {
        for (int dx = -CRATER_RADIUS; dx <= CRATER_RADIUS; dx++) {
            for (int dz = -CRATER_RADIUS; dz <= CRATER_RADIUS; dz++) {
                int x = ox + dx, z = oz + dz;
                if (!inChunkColumn(x, z)) {
                    continue;
                }
                double dist = Math.sqrt(dx * dx + dz * dz) / CRATER_RADIUS;
                // Own random per column, so it doesn't matter in which chunk it is generated
                RandomSource columnRandom = RandomSource.create(seed ^ (x * 341873128712L + z * 132897987541L));
                if (dist > 1.0 || columnRandom.nextDouble() < (dist - 0.5) * 2.0) {
                    continue;
                }
                BlockPos top = new BlockPos(x, level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1, z);
                if (level.getFluidState(top).isEmpty() && !level.getBlockState(top).isAir()) {
                    level.setBlock(top, ModBlocks.THERMISCHESGESTEIN.get().defaultBlockState(), 2);
                }
            }
        }
    }

    /** Octagonal tube from the tail (a=-7) to the nose (a=7), with damage and a big breach. */
    private void buildHull() {
        BlockState air = Blocks.CAVE_AIR.defaultBlockState();
        BlockState glass = Blocks.GLASS.defaultBlockState();
        int breachSide = random.nextBoolean() ? 2 : -2;

        for (int a = -7; a <= 7; a++) {
            // Shape of the cross-section at this point (narrower towards the nose)
            int halfWidth = a <= 4 ? 2 : (a <= 6 ? 1 : 0);
            int bottom = a <= 5 ? 0 : 1;
            int top = a <= 4 ? 4 : (a == 5 ? 3 : 2);

            for (int y = bottom; y <= top; y++) {
                for (int s = -halfWidth; s <= halfWidth; s++) {
                    boolean side = Math.abs(s) == halfWidth;
                    boolean capY = y == bottom || y == top;
                    boolean corner = side && capY && halfWidth == 2;   // cut corners -> octagon
                    boolean endCap = a == -7 || a == 7;
                    if (corner) {
                        continue;
                    }
                    if (!(endCap || side || capY)) {
                        set(a, y, s, air); // hollow inside (also below ground)
                        continue;
                    }
                    if ((a == 4 || a == 5) && y >= 3 && Math.abs(s) <= 1) {
                        set(a, y, s, glass); // cockpit window
                        continue;
                    }
                    if (s == breachSide && a >= -1 && a <= 1 && y >= 1 && y <= 3) {
                        set(a, y, s, air); // big breach in one side wall
                        continue;
                    }
                    BlockState block = hullBlock();                      // always roll, so all chunks stay in sync
                    boolean hole = y != bottom && random.nextFloat() < HULL_DAMAGE;
                    set(a, y, s, hole ? air : block);
                }
            }
        }
    }

    private void buildWingsAndFin() {
        BlockState wing = Blocks.IRON_BLOCK.defaultBlockState();
        int intactSide = random.nextBoolean() ? 1 : -1;
        for (int a = -3; a <= 2; a++) {
            int length = 6 - Math.abs(a + 1);                 // swept wing shape
            for (int s = 3; s <= 2 + length; s++) {
                BlockState block = hullBlock();
                set(a, 1, intactSide * s, random.nextFloat() < 0.1F ? block : wing);
            }
            int stump = 3 + random.nextInt(2);                 // the other wing snapped off after 1-2 blocks
            for (int s = 3; s <= stump; s++) {
                set(a, 1, -intactSide * s, wing);
            }
        }
        // Tail fin, rising towards the back
        for (int a = -7; a <= -5; a++) {
            for (int y = 5; y <= -a; y++) {
                set(a, y, 0, hullBlock());
            }
        }
    }

    /** Two glowing exhausts and smoke from the engines. */
    private void buildEngines() {
        BlockState glow = ModBlocks.THERMISCHESGESTEIN.get().defaultBlockState();
        set(-8, 1, -1, glow);
        set(-8, 1, 1, glow);
        set(-8, 2, -1, hullBlock());
        set(-8, 2, 1, hullBlock());
        set(-8, 1, 0, Blocks.CAMPFIRE.defaultBlockState());  // smoke rising from the wreck
        set(-8, 2, 0, Blocks.CAVE_AIR.defaultBlockState());
        set(-8, 3, 0, Blocks.CAVE_AIR.defaultBlockState());
    }

    private void placeLoot() {
        boolean secondChest = random.nextBoolean();
        placeChest(pos(1, 1, -1), 1);
        if (secondChest) {
            placeChest(pos(-4, 1, 1), 2); // sometimes a second chest in the back
        }
        // Broken pipes inside
        set(-2, 3, 1, Blocks.IRON_BARS.defaultBlockState());
        set(-1, 3, -1, Blocks.IRON_BARS.defaultBlockState());
    }

    private void placeChest(BlockPos chestPos, int index) {
        if (chunkBox.isInside(chestPos)) {
            level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
            RandomizableContainerBlockEntity.setLootTable(level, RandomSource.create(seed + index), chestPos, LOOT);
        }
    }

    /** Pieces of hull lying around the crash site. */
    private void scatterDebris() {
        int count = 6 + random.nextInt(5);
        for (int i = 0; i < count; i++) {
            int x = ox + random.nextInt(17) - 8;
            int z = oz + random.nextInt(17) - 8;
            BlockState block = random.nextInt(3) == 0 ? Blocks.IRON_BARS.defaultBlockState() : hullBlock();
            if (!inChunkColumn(x, z)) {
                continue;
            }
            BlockPos top = new BlockPos(x, level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z), z);
            if (level.isEmptyBlock(top) && level.getFluidState(top.below()).isEmpty()) {
                put(top, block);
            }
        }
    }
}
