package net.example.per_aspera_ad_astra.event;

import net.example.per_aspera_ad_astra.Per_aspera_ad_astra;
import net.example.per_aspera_ad_astra.item.custom.SpacesuitChestplateItem;
import net.example.per_aspera_ad_astra.worldgen.dimension.ModDimensions;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraftforge.event.entity.living.LivingDrownEvent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingBreatheEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Oxygen system for the Astra dimension:
 *  - There is no air in Astra: players lose air like under water and then drown.
 *  - Wearing the FULL spacesuit with oxygen in the chestplate tank lets you breathe.
 *  - The tank empties by 1 per tick (20 per second). Refill it with oxygen canisters.
 */
@Mod.EventBusSubscriber(modid = Per_aspera_ad_astra.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class OxygenEvents {

    /**
     * Custom damage type for suffocating in Astra -> own death message.
     * Defined in data/per_aspera_ad_astra/damage_type/no_oxygen.json,
     * message text in lang/en_us.json ("death.attack.per_aspera_ad_astra.no_oxygen").
     */
    public static final ResourceKey<DamageType> NO_OXYGEN = ResourceKey.create(Registries.DAMAGE_TYPE,
            new ResourceLocation(Per_aspera_ad_astra.MOD_ID, "no_oxygen"));

    /**
     * When a player in Astra runs out of air, replace the vanilla drowning damage ("X drowned")
     * with our NO_OXYGEN damage (same amount: 2 = one heart per hit).
     */
    @SubscribeEvent
    public static void onDrown(LivingDrownEvent event) {
        if (!(event.getEntity() instanceof Player player) || !needsOxygen(player)
                || player.level().isClientSide || !event.isDrowning()) {
            return;
        }
        float damage = event.getDamageAmount();
        event.setDamageAmount(0); // vanilla still resets the air bar + shows bubbles, but deals no damage
        DamageSource noOxygen = new DamageSource(player.level().registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(NO_OXYGEN));
        player.hurt(noOxygen, damage);
    }

    private static boolean needsOxygen(Player player) {
        return player.level().dimension() == ModDimensions.ASTRA_DIM_LEVEL_KEY
                && !player.isCreative() && !player.isSpectator();
    }

    @SubscribeEvent
    public static void onBreathe(LivingBreatheEvent event) {
        if (!(event.getEntity() instanceof Player player) || !needsOxygen(player)) {
            return; // only players in Astra are affected (mobs live there normally)
        }

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (SpacesuitChestplateItem.hasFullSuit(player) && SpacesuitChestplateItem.getOxygen(chest) > 0) {
            // Breathing from the tank (also works under water)
            event.setCanBreathe(true);
            event.setCanRefillAir(true);
            if (!player.level().isClientSide) {
                SpacesuitChestplateItem.setOxygen(chest, SpacesuitChestplateItem.getOxygen(chest) - 1);
            }
        } else {
            // No air: the bubbles go down and you start drowning like under water
            event.setCanBreathe(false);
            event.setCanRefillAir(false);
        }
    }

    /** Shows the oxygen level above the hotbar once per second while in Astra. */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (event.phase != TickEvent.Phase.END || player.level().isClientSide
                || player.tickCount % 20 != 0 || !needsOxygen(player)) {
            return;
        }

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        MutableComponent message;
        if (!SpacesuitChestplateItem.hasFullSuit(player)) {
            message = Component.translatable("message.per_aspera_ad_astra.no_suit").withStyle(ChatFormatting.RED);
        } else {
            int percent = SpacesuitChestplateItem.getOxygenPercent(chest);
            ChatFormatting color = percent > 50 ? ChatFormatting.AQUA : percent > 20 ? ChatFormatting.YELLOW : ChatFormatting.RED;
            message = Component.translatable("message.per_aspera_ad_astra.oxygen_level", percent).withStyle(color);
        }

        // Overheating in the Schwefelfelder (see HeatEvents): shown next to the oxygen
        int heat = HeatEvents.getHeatPercent(player);
        if (heat > 0) {
            ChatFormatting heatColor = heat >= 100 ? ChatFormatting.DARK_RED : heat > 50 ? ChatFormatting.RED : ChatFormatting.GOLD;
            String key = heat >= 100 ? "message.per_aspera_ad_astra.burning" : "message.per_aspera_ad_astra.heat_level";
            message.append(Component.literal("  |  ").withStyle(ChatFormatting.GRAY))
                    .append(Component.translatable(key, heat).withStyle(heatColor));
        }
        player.displayClientMessage(message, true);
    }
}
