package net.example.per_aspera_ad_astra.worldgen.structure;

import net.example.per_aspera_ad_astra.Per_aspera_ad_astra;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * Structure TYPES written in Java (the sunken house + yeti nest use the vanilla "minecraft:jigsaw" type with .nbt files instead).
 * Used in JSON with "type": "per_aspera_ad_astra:<name>" in worldgen/structure/.
 */
public class ModStructures {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, Per_aspera_ad_astra.MOD_ID);
    public static final DeferredRegister<StructurePieceType> PIECE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, Per_aspera_ad_astra.MOD_ID);

    public static final RegistryObject<StructureType<SpaceshipWreckStructure>> SPACESHIP_WRECK =
            STRUCTURE_TYPES.register("spaceship_wreck", () -> () -> SpaceshipWreckStructure.CODEC);

    public static final RegistryObject<StructurePieceType> SPACESHIP_WRECK_PIECE =
            PIECE_TYPES.register("spaceship_wreck", () -> (StructurePieceType.ContextlessType) SpaceshipWreckPiece::new);

    public static final RegistryObject<StructureType<AlienVillageStructure>> ALIEN_VILLAGE =
            STRUCTURE_TYPES.register("alien_village", () -> () -> AlienVillageStructure.CODEC);

    public static final RegistryObject<StructurePieceType> ALIEN_VILLAGE_PIECE =
            PIECE_TYPES.register("alien_village", () -> (StructurePieceType.ContextlessType) AlienVillagePiece::new);

    public static void register(IEventBus eventBus) {
        STRUCTURE_TYPES.register(eventBus);
        PIECE_TYPES.register(eventBus);
    }
}
