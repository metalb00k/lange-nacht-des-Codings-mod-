package net.example.per_aspera_ad_astra.block;

import com.google.common.collect.ImmutableSet;
import net.example.per_aspera_ad_astra.Per_aspera_ad_astra;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

/**
 * Makes the Ice Beehive work like a vanilla beehive. Two things are needed:
 *
 *  1. A "point of interest" so bees can FIND it. The POI is registered here and added to the vanilla
 *     bee-home list in data/minecraft/tags/point_of_interest_type/bee_home.json.
 *  2. The vanilla beehive block entity (stores the bees + honey) only accepts the vanilla beehive and
 *     bee nest. {@link #allowIceBeehive()} adds the Ice Beehive to that list (called in common setup).
 */
public class ModBeehives {
    public static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(ForgeRegistries.POI_TYPES, Per_aspera_ad_astra.MOD_ID);

    // Same settings as the vanilla beehive POI
    public static final RegistryObject<PoiType> ICE_BEEHIVE_POI = POI_TYPES.register("ice_beehive",
            () -> new PoiType(ImmutableSet.copyOf(ModBlocks.ICE_BEEHIVE.get().getStateDefinition().getPossibleStates()), 0, 1));

    public static void register(IEventBus eventBus) {
        POI_TYPES.register(eventBus);
    }

    /** Adds the Ice Beehive to the blocks the vanilla beehive block entity is allowed on. */
    @SuppressWarnings("unchecked")
    public static void allowIceBeehive() {
        try {
            for (Field field : BlockEntityType.class.getDeclaredFields()) {
                if (Set.class.isAssignableFrom(field.getType())) {       // the "valid blocks" set
                    field.setAccessible(true);
                    Set<Block> valid = new HashSet<>((Set<Block>) field.get(BlockEntityType.BEEHIVE));
                    valid.add(ModBlocks.ICE_BEEHIVE.get());
                    field.set(BlockEntityType.BEEHIVE, valid);
                    return;
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Could not register the ice beehive as a beehive", e);
        }
    }
}
