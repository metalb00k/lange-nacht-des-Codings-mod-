package net.example.per_aspera_ad_astra.event;

import net.example.per_aspera_ad_astra.Per_aspera_ad_astra;
import net.example.per_aspera_ad_astra.item.ModItems;
import net.example.per_aspera_ad_astra.item.custom.SpacesuitChestplateItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Overheating in the Schwefelfelder (sulfur_wastes biome):
 *  - Without protection the heat goes up by 1 per tick. At MAX_HEAT (15 s) you start to burn.
 *  - Protection = a Freezer anywhere in the inventory OR a spacesuit chestplate with the freezer upgrade.
 *  - Outside the biome (or protected) you cool down 3x as fast.
 * The heat is shown next to the oxygen display (see OxygenEvents#onPlayerTick).
 * Stored on the player (not saved through death: you respawn cooled down).
 */
@Mod.EventBusSubscriber(modid = Per_aspera_ad_astra.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class HeatEvents {
    public static final ResourceKey<Biome> SULFUR_WASTES = ResourceKey.create(Registries.BIOME,
            new ResourceLocation(Per_aspera_ad_astra.MOD_ID, "sulfur_wastes"));

    /** Ticks in the heat until you start burning (300 = 15 seconds). */
    public static final int MAX_HEAT = 6000;
    private static final int COOL_DOWN_PER_TICK = 3;
    private static final String HEAT_TAG = "per_aspera_ad_astra.Heat";

    public static int getHeat(Player player) {
        return player.getPersistentData().getInt(HEAT_TAG);
    }

    /** Heat in percent (0-100), for display. */
    public static int getHeatPercent(Player player) {
        return getHeat(player) * 100 / MAX_HEAT;
    }

    /** Freezer in the inventory or freezer-upgraded spacesuit chestplate. */
    public static boolean isCooled(Player player) {
        return SpacesuitChestplateItem.hasUpgrade(player.getItemBySlot(EquipmentSlot.CHEST), SpacesuitChestplateItem.FREEZER)
                || player.getInventory().hasAnyMatching(stack -> stack.is(ModItems.FREEZER.get()));
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (event.phase != TickEvent.Phase.END || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        boolean inHeat = !player.isCreative() && !player.isSpectator()
                && level.getBiome(player.blockPosition()).is(SULFUR_WASTES) && !isCooled(player);

        int heat = getHeat(player);
        heat = inHeat ? Math.min(MAX_HEAT, heat + 1) : Math.max(0, heat - COOL_DOWN_PER_TICK);
        player.getPersistentData().putInt(HEAT_TAG, heat);
        if (!inHeat) {
            return;
        }

        // Over 50%: you start to steam
        if (heat > MAX_HEAT / 2 && player.tickCount % 10 == 0) {
            level.sendParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + 1.0, player.getZ(),
                    3, 0.3, 0.5, 0.3, 0.01);
        }
        // Over 75%: sizzling sound as a warning
        if (heat > MAX_HEAT * 3 / 4 && player.tickCount % 40 == 0) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.4F, 1.5F);
        }
        // 100%: you burn (Fire Resistance protects as usual)
        if (heat >= MAX_HEAT && player.tickCount % 20 == 0) {
            player.setSecondsOnFire(3);
        }
    }
}
