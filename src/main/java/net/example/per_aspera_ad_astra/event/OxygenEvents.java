package net.example.per_aspera_ad_astra.event;

import net.example.per_aspera_ad_astra.Per_aspera_ad_astra;
import net.example.per_aspera_ad_astra.item.custom.SpacesuitChestplateItem;
import net.example.per_aspera_ad_astra.worldgen.dimension.ModDimensions;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
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
        if (!SpacesuitChestplateItem.hasFullSuit(player)) {
            player.displayClientMessage(Component.translatable("message.per_aspera_ad_astra.no_suit")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }

        int percent = SpacesuitChestplateItem.getOxygenPercent(chest);
        ChatFormatting color = percent > 50 ? ChatFormatting.AQUA : percent > 20 ? ChatFormatting.YELLOW : ChatFormatting.RED;
        player.displayClientMessage(Component.translatable("message.per_aspera_ad_astra.oxygen_level", percent)
                .withStyle(color), true);
    }
}
