package net.example.per_aspera_ad_astra.block;

import net.example.per_aspera_ad_astra.Per_aspera_ad_astra;
import net.example.per_aspera_ad_astra.block.custom.EiswasserBlock;
import net.example.per_aspera_ad_astra.block.custom.ModPortalBlock;
import net.example.per_aspera_ad_astra.fluid.ModFluids;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/**
 * Central place for all custom blocks of the mod.
 * Every block registered via {@link #registerBlock} automatically gets a matching BlockItem.
 */
public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Per_aspera_ad_astra.MOD_ID);

    public static final DeferredRegister<Item> BLOCK_ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Per_aspera_ad_astra.MOD_ID);

    // WARNING: do NOT add .noLootTable() / .noCollission() / .noOcclusion() to normal blocks!
    // noLootTable = block drops nothing (loot tables are ignored), noCollission = players fall through it
    // (the Astra ground is made of these blocks!), noOcclusion = see-through rendering glitches.
    // Only the portal uses noCollission/noOcclusion.
    public static final RegistryObject<Block> MOD_PORTAL = registerBlock("mod_portal",
            () -> new ModPortalBlock(BlockBehaviour.Properties.copy(Blocks.STONE).noOcclusion().noCollission()));

    // Blöcke Eisdimension
    public static final RegistryObject<Block> EISHOLZ = registerBlock("eisholz",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.OAK_WOOD)));

    public static final RegistryObject<Block> EISBLAETTER = registerBlock("eisblatt",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES)));

    public static final RegistryObject<Block> EISZAPFEN = registerBlock("eiszapfen",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.POINTED_DRIPSTONE)));

    public static final RegistryObject<Block> CRYOTITAN = registerBlock("cryo_titan",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.ANCIENT_DEBRIS)));

    public static final RegistryObject<Block> FROSTKRISTALL = registerBlock("frostkristall",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.BLUE_ICE)));

    public static final RegistryObject<Block> EISERDE = registerBlock("eiserde",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.DIRT)));

    public static final RegistryObject<Block> EISHOLZBRETT = registerBlock("eisholzbrett",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)));

    public static final RegistryObject<Block> ERZ_BLOCK = registerBlock("erz_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_ORE).requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> EIS_STEIN = registerBlock("eis_stein",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.STONE).requiresCorrectToolForDrops()));

    // Green ice water (fluid, see fluid/ModFluids). No BlockItem - you get it with the bucket.
    public static final RegistryObject<LiquidBlock> EISWASSER_BLOCK = BLOCKS.register("eiswasser",
            () -> new EiswasserBlock(ModFluids.SOURCE_EISWASSER, BlockBehaviour.Properties.copy(Blocks.WATER).noLootTable()));

    // Names must be lowercase, otherwise the game crashes on start!
    public static final RegistryObject<Block> EISZIEGELSTEIN = registerBlock("eisziegelstein",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.BRICKS).requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> EISZIEGELSTEINSTUFEN = registerBlock("eisziegelstein_stufen",
            () -> new StairBlock(() -> EISZIEGELSTEIN.get().defaultBlockState(),
                    BlockBehaviour.Properties.copy(Blocks.BRICK_STAIRS).requiresCorrectToolForDrops()));

    // Solid ice block: slippery like packed ice, does not melt
    public static final RegistryObject<Block> EISBLOCK = registerBlock("eisblock",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.PACKED_ICE)));

    //Titan-Block
    public static final RegistryObject<Block> TITANBLOCK = registerBlock("titanblock",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.NETHERITE_BLOCK)));

    //MethanEIs
    public static final RegistryObject<Block> METHANEIS = registerBlock("methaneis",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.ICE)));

    //Schwefel
    public static final RegistryObject<Block> SCHWEFEL = registerBlock("schwefel",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.COAL_BLOCK)));

    //Thermisches Gestein
    public static final RegistryObject<Block> THERMISCHESGESTEIN = registerBlock("thermisches_gestein",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.MAGMA_BLOCK)));




    // ---- Custom blocks go here ----
    // Example (needs blockstate, model, texture and lang entry in src/main/resources/assets/per_aspera_ad_astra/):
    //
    // public static final RegistryObject<Block> EXAMPLE_BLOCK = registerBlock("example_block",
    //         () -> new Block(BlockBehaviour.Properties.of()
    //                 .mapColor(MapColor.STONE)
    //                 .strength(3.0f, 6.0f)
    //                 .sound(SoundType.STONE)
    //                 .requiresCorrectToolForDrops()));

    /** Registers a block and a BlockItem with the same name. */
    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> registered = BLOCKS.register(name, block);
        BLOCK_ITEMS.register(name, () -> new BlockItem(registered.get(), new Item.Properties()));
        return registered;
    }

    /** Call this from the mod constructor. */
    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        BLOCK_ITEMS.register(modEventBus);
    }
}
