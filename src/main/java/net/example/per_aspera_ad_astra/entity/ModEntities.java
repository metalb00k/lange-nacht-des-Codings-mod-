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

    // Same size as a vanilla ghast. No spawn egg, no natural spawning: use /summon per_aspera_ad_astra:ice_ghast
    public static final RegistryObject<EntityType<IceGhastEntity>> ICE_GHAST =
            ENTITY_TYPES.register("ice_ghast", () ->
                    EntityType.Builder.of(IceGhastEntity::new, MobCategory.MONSTER)
                            .sized(4.0F, 4.0F)
                            .fireImmune()
                            .clientTrackingRange(10)
                            .build("ice_ghast"));

    // The projectile the ice ghast shoots (same settings as the vanilla fireball)
    public static final RegistryObject<EntityType<IceChargeEntity>> ICE_CHARGE =
            ENTITY_TYPES.register("ice_charge", () ->
                    EntityType.Builder.<IceChargeEntity>of(IceChargeEntity::new, MobCategory.MISC)
                            .sized(1.0F, 1.0F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("ice_charge"));

    // Boss: summoned with Frost Bait in Eiswasser, or /summon per_aspera_ad_astra:great_frostworm
    // Hitbox matches the 2.5x model (see GreatFrostwormRenderer.SCALE)
    public static final RegistryObject<EntityType<GreatFrostwormEntity>> GREAT_FROSTWORM =
            ENTITY_TYPES.register("great_frostworm", () ->
                    EntityType.Builder.of(GreatFrostwormEntity::new, MobCategory.MONSTER)
                            .sized(3.0F, 3.5F)
                            .clientTrackingRange(10)
                            .build("great_frostworm"));

    // Ice shard shot by the freezer-upgraded spacesuit
    public static final RegistryObject<EntityType<IceShardProjectile>> ICE_SHARD =
            ENTITY_TYPES.register("ice_shard", () ->
                    EntityType.Builder.<IceShardProjectile>of(IceShardProjectile::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("ice_shard"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(SCHNEELEOPARD.get(), SchneeleopardEntity.createAttributes().build());
        event.put(YETI.get(), YetiEntity.createAttributes().build());
        event.put(ICE_SPIDER.get(), IceSpiderEntity.createAttributes().build());
        event.put(ICE_FOX.get(), IceFoxEntity.createAttributes().build());
        event.put(ICE_GHAST.get(), IceGhastEntity.createAttributes().build());
        event.put(GREAT_FROSTWORM.get(), GreatFrostwormEntity.createAttributes().build());
    }

    /** Where natural spawns are allowed: on solid ground (snow, stone, ...), like vanilla cats. */
    @SubscribeEvent
    public static void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        event.register(SCHNEELEOPARD.get(), SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE);
        // Natural spawning in the Astra biomes (see the "spawners" in worldgen/biome/*.json)
        event.register(YETI.get(), SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(ICE_SPIDER.get(), SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(ICE_FOX.get(), SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE);
        // Flying: may spawn anywhere there is room
        event.register(ICE_GHAST.get(), SpawnPlacements.Type.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE);
    }
}
