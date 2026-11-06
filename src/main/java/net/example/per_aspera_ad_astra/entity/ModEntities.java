package net.example.per_aspera_ad_astra.entity;

import net.example.per_aspera_ad_astra.Per_aspera_ad_astra;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * All custom mobs of the mod live here.
 *
 * To add a new mob:
 *  1. Register its EntityType below.
 *  2. Register its attributes in registerAttributes.
 *  3. Model + renderer in entity/client, register them in ModEntityClientEvents.
 *  4. Texture in resources/assets/per_aspera_ad_astra/textures/entity/<name>.png
 *  5. Name in lang/en_us.json ("entity.per_aspera_ad_astra.<name>")
 */
@Mod.EventBusSubscriber(modid = Per_aspera_ad_astra.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Per_aspera_ad_astra.MOD_ID);

    // Hitbox matches the 1.5x model (cat = 0.6 x 0.7). Keep in sync with SCALE in SchneeleopardRenderer.
    public static final RegistryObject<EntityType<SchneeleopardEntity>> SCHNEELEOPARD =
            ENTITY_TYPES.register("schneeleopard", () ->
                    EntityType.Builder.of(SchneeleopardEntity::new, MobCategory.CREATURE)
                            .sized(1.05F, 1.2F)
                            .clientTrackingRange(8)
                            .build("schneeleopard"));

    // Same size as an iron golem. No spawn egg: use /summon per_aspera_ad_astra:yeti
    public static final RegistryObject<EntityType<YetiEntity>> YETI =
            ENTITY_TYPES.register("yeti", () ->
                    EntityType.Builder.of(YetiEntity::new, MobCategory.MONSTER)
                            .sized(1.4F, 2.7F)
                            .clientTrackingRange(10)
                            .build("yeti"));

    // Same size as a vanilla spider. No spawn egg, no natural spawning: use /summon per_aspera_ad_astra:ice_spider
    public static final RegistryObject<EntityType<IceSpiderEntity>> ICE_SPIDER =
            ENTITY_TYPES.register("ice_spider", () ->
                    EntityType.Builder.of(IceSpiderEntity::new, MobCategory.MONSTER)
                            .sized(1.4F, 0.9F)
                            .clientTrackingRange(8)
                            .build("ice_spider"));

    // Same size as a vanilla fox. No spawn egg, no natural spawning: use /summon per_aspera_ad_astra:ice_fox
    public static final RegistryObject<EntityType<IceFoxEntity>> ICE_FOX =
            ENTITY_TYPES.register("ice_fox", () ->
                    EntityType.Builder.of(IceFoxEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 0.7F)
                            .clientTrackingRange(8)
                            .build("ice_fox"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(SCHNEELEOPARD.get(), SchneeleopardEntity.createAttributes().build());
        event.put(YETI.get(), YetiEntity.createAttributes().build());
        event.put(ICE_SPIDER.get(), IceSpiderEntity.createAttributes().build());
        event.put(ICE_FOX.get(), IceFoxEntity.createAttributes().build());
    }

    /** Where natural spawns are allowed: on solid ground (snow, stone, ...), like vanilla cats. */
    @SubscribeEvent
    public static void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        event.register(SCHNEELEOPARD.get(), SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE);
    }
}
