package net.example.per_aspera_ad_astra.worldgen.structure;

import net.example.per_aspera_ad_astra.Per_aspera_ad_astra;
import net.example.per_aspera_ad_astra.block.ModBlocks;
import net.example.per_aspera_ad_astra.entity.AlienVillagerEntity;
import net.example.per_aspera_ad_astra.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/**
 * One building of an alien village (built block by block in code, no .nbt file):
 *
 *  PLAZA: round Eisziegelstein square in the middle with a glowing Frostkristall monument and 4 lanterns.
 *  HUT:   dome of Eisziegelstein (radius 3 or 4) with blue glass windows, a door facing the plaza,
 *         a glowing ceiling + antenna, a chest (loot_tables/chests/alien_village.json), a work block,
 *         and 1 alien (2 in big huts) living inside.
 *
 * Like the spaceship wreck, Minecraft calls postProcess once per chunk the building touches,
 * so everything random comes from the saved seed and only blocks inside chunkBox are placed.
 */
public class AlienVillagePiece extends StructurePiece {
    public static final ResourceLocation LOOT = new ResourceLocation(Per_aspera_ad_astra.MOD_ID, "chests/alien_village");
    public static final int PLAZA = 0, HUT = 1;
    private static final int PLAZA_RADIUS = 5;

    // Saved with the structure
    private final int kind, radius;
    private final long seed;
    private final int x, y, z;              // middle of the building, y = floor
    private final int doorX, doorZ;         // direction of the door (-1/0/1), huts only
    private final int homeX, homeY, homeZ;  // village middle (the aliens stay near it)

    // Only valid during postProcess
    private WorldGenLevel level;
    private BoundingBox chunkBox;

    public AlienVillagePiece(int kind, long seed, int x, int y, int z, int radius,
                             int doorX, int doorZ, BlockPos home) {
        super(ModStructures.ALIEN_VILLAGE_PIECE.get(), 0, makeBox(kind, x, y, z, radius));
        this.kind = kind;
        this.seed = seed;
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
        this.doorX = doorX;
        this.doorZ = doorZ;
        this.homeX = home.getX();
        this.homeY = home.getY();
        this.homeZ = home.getZ();
    }

    /** Loading from the save file. */
    public AlienVillagePiece(CompoundTag tag) {
        super(ModStructures.ALIEN_VILLAGE_PIECE.get(), tag);
        this.kind = tag.getInt("Kind");
        this.seed = tag.getLong("Seed");
        this.x = tag.getInt("X");
        this.y = tag.getInt("Y");
        this.z = tag.getInt("Z");
        this.radius = tag.getInt("R");
        this.doorX = tag.getInt("DoorX");
        this.doorZ = tag.getInt("DoorZ");
        this.homeX = tag.getInt("HomeX");
        this.homeY = tag.getInt("HomeY");
        this.homeZ = tag.getInt("HomeZ");
    }

    /** The box is used for terrain flattening (its bottom = the floor) and to know which chunks to build in. */
    private static BoundingBox makeBox(int kind, int x, int y, int z, int radius) {
        int r = kind == PLAZA ? PLAZA_RADIUS + 1 : radius + 2;
        int height = kind == PLAZA ? 8 : radius + 4;
        return new BoundingBox(x - r, y, z - r, x + r, y + height, z + r);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("Kind", kind);
        tag.putLong("Seed", seed);
        tag.putInt("X", x);
        tag.putInt("Y", y);
        tag.putInt("Z", z);
        tag.putInt("R", radius);
        tag.putInt("DoorX", doorX);
        tag.putInt("DoorZ", doorZ);
        tag.putInt("HomeX", homeX);
        tag.putInt("HomeY", homeY);
        tag.putInt("HomeZ", homeZ);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
                            RandomSource unusedRandom, BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        this.level = level;
        this.chunkBox = chunkBox;
        if (kind == PLAZA) {
            buildPlaza();
        } else {
            buildHut(RandomSource.create(seed));
        }
    }

    // ---------------------------------------------------------------------
    // Helpers (dx/dy/dz are relative to the middle of the building, dy 0 = floor)
    // ---------------------------------------------------------------------

    private void set(int dx, int dy, int dz, BlockState state) {
        BlockPos p = new BlockPos(x + dx, y + dy, z + dz);
        if (chunkBox.isInside(p)) {
            level.setBlock(p, state, 2);
        }
    }

    /** Fills air / water under the floor so nothing floats on slopes. */
    private void foundation(int dx, int dz) {
        for (int dy = -1; dy >= -5; dy--) {
            BlockPos p = new BlockPos(x + dx, y + dy, z + dz);
            if (!chunkBox.isInside(p)) {
                return;
            }
            BlockState state = level.getBlockState(p);
            if (!state.isAir() && state.getFluidState().isEmpty()) {
                return;
            }
            level.setBlock(p, ModBlocks.EIS_STEIN.get().defaultBlockState(), 2);
        }
    }

    private static BlockState wall() {
        return ModBlocks.EISZIEGELSTEIN.get().defaultBlockState();
    }

    // ---------------------------------------------------------------------
    // Plaza
    // ---------------------------------------------------------------------

    private void buildPlaza() {
        BlockState air = Blocks.AIR.defaultBlockState();
        double r = PLAZA_RADIUS + 0.5;
        for (int dx = -PLAZA_RADIUS; dx <= PLAZA_RADIUS; dx++) {
            for (int dz = -PLAZA_RADIUS; dz <= PLAZA_RADIUS; dz++) {
                if (dx * dx + dz * dz > r * r) {
                    continue;
                }
                foundation(dx, dz);
                boolean ring = dx * dx + dz * dz > (r - 1.2) * (r - 1.2);
                set(dx, 0, dz, ring ? ModBlocks.EIS_STEIN.get().defaultBlockState() : wall());
                for (int dy = 1; dy <= 6; dy++) {
                    set(dx, dy, dz, air); // clear the square
                }
            }
        }
        // Monument: Titanblock base, Frostkristall pillar, glowing top
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                set(dx, 1, dz, ModBlocks.TITANBLOCK.get().defaultBlockState());
            }
        }
        for (int dy = 2; dy <= 6; dy++) {
            set(0, dy, 0, ModBlocks.FROSTKRISTALL.get().defaultBlockState());
        }
        set(0, 7, 0, Blocks.SEA_LANTERN.defaultBlockState());
        set(0, 8, 0, Blocks.END_ROD.defaultBlockState());
        // Four lanterns on posts
        for (int[] c : new int[][]{{3, 3}, {-3, 3}, {3, -3}, {-3, -3}}) {
            set(c[0], 1, c[1], Blocks.IRON_BARS.defaultBlockState());
            set(c[0], 2, c[1], Blocks.IRON_BARS.defaultBlockState());
            set(c[0], 3, c[1], Blocks.SEA_LANTERN.defaultBlockState());
        }
        // Two Ice Beehives facing outwards, each with 2 ice bees flying around it
        placeBeehive(0, 4, net.minecraft.core.Direction.SOUTH);
        placeBeehive(0, -4, net.minecraft.core.Direction.NORTH);
    }

    private void placeBeehive(int dx, int dz, net.minecraft.core.Direction facing) {
        set(dx, 1, dz, ModBlocks.ICE_BEEHIVE.get().defaultBlockState()
                .setValue(net.minecraft.world.level.block.BeehiveBlock.FACING, facing));
        BlockPos hive = new BlockPos(x + dx, y + 1, z + dz);
        if (!chunkBox.isInside(hive)) {
            return;
        }
        for (int i = 0; i < 2; i++) {
            var bee = ModEntities.ICE_BEE.get().create(level.getLevel());
            if (bee == null) {
                continue;
            }
            bee.moveTo(hive.getX() + 0.5 + facing.getStepX(), hive.getY() + 1.0 + i * 0.5,
                    hive.getZ() + 0.5 + facing.getStepZ(), 0.0F, 0.0F);
            bee.finalizeSpawn(level, level.getCurrentDifficultyAt(hive), MobSpawnType.STRUCTURE, null, null);
            bee.setPersistenceRequired();
            level.addFreshEntityWithPassengers(bee);
        }
    }

    // ---------------------------------------------------------------------
    // Hut
    // ---------------------------------------------------------------------

    private void buildHut(RandomSource random) {
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState glass = Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState();
        BlockState floor = ModBlocks.EISHOLZBRETT.get().defaultBlockState();
        int r = radius;

        for (int dx = -r - 1; dx <= r + 1; dx++) {
            for (int dz = -r - 1; dz <= r + 1; dz++) {
                double flat = Math.sqrt(dx * dx + dz * dz);
                if (flat > r + 0.5) {
                    continue;
                }
                foundation(dx, dz);
                set(dx, 0, dz, floor);
                for (int dy = 1; dy <= r + 1; dy++) {
                    double dist = Math.sqrt(dx * dx + dz * dz + (dy - 1) * (dy - 1));
                    if (dist > r + 0.5) {
                        continue;
                    }
                    boolean shell = dist > r - 0.5;
                    boolean window = shell && dy == 2 && (dx == 0 || dz == 0);
                    BlockState block = random.nextFloat() < 0.08F
                            ? ModBlocks.FROSTKRISTALL.get().defaultBlockState() : wall(); // roll always (keeps chunks in sync)
                    set(dx, dy, dz, !shell ? air : window ? glass : block);
                }
            }
        }

        // Door towards the plaza (2 high), with a step in front
        for (int k = r - 1; k <= r + 1; k++) {
            set(doorX * k, 1, doorZ * k, air);
            set(doorX * k, 2, doorZ * k, air);
        }
        set(doorX * (r + 1), 0, doorZ * (r + 1), wall());

        // Glowing ceiling + antenna on top
        set(0, r + 1, 0, Blocks.SEA_LANTERN.defaultBlockState());
        set(0, r + 2, 0, Blocks.IRON_BARS.defaultBlockState());
        set(0, r + 3, 0, Blocks.IRON_BARS.defaultBlockState());
        set(0, r + 4, 0, Blocks.END_ROD.defaultBlockState());

        // Furniture on the wall opposite the door
        int backX = -doorX * (r - 1), backZ = -doorZ * (r - 1);
        int sideX = doorZ, sideZ = doorX; // 90 degrees to the door direction
        BlockPos chestPos = new BlockPos(x + backX, y + 1, z + backZ);
        if (chunkBox.isInside(chestPos)) {
            level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
            RandomizableContainerBlockEntity.setLootTable(level, RandomSource.create(seed + 1), chestPos, LOOT);
        }
        BlockState work = switch (random.nextInt(3)) {
            case 0 -> Blocks.CRAFTING_TABLE.defaultBlockState();
            case 1 -> Blocks.FURNACE.defaultBlockState();
            default -> Blocks.CARTOGRAPHY_TABLE.defaultBlockState();
        };
        set(backX + sideX, 1, backZ + sideZ, work);

        // The aliens living here (spawned in the chunk that has the middle of the hut)
        BlockPos middle = new BlockPos(x, y + 1, z);
        if (chunkBox.isInside(middle)) {
            int aliens = r >= 4 ? 2 : 1;
            for (int i = 0; i < aliens; i++) {
                AlienVillagerEntity alien = ModEntities.ALIEN_VILLAGER.get().create(level.getLevel());
                if (alien == null) {
                    continue;
                }
                alien.moveTo(x + 0.5 + i * 0.5, y + 1, z + 0.5, random.nextFloat() * 360.0F, 0.0F);
                alien.finalizeSpawn(level, level.getCurrentDifficultyAt(middle), MobSpawnType.STRUCTURE, null, null);
                alien.setHome(new BlockPos(homeX, homeY, homeZ));
                level.addFreshEntityWithPassengers(alien);
            }
        }
    }
}
