package net.example.per_aspera_ad_astra.event;

import net.example.per_aspera_ad_astra.Per_aspera_ad_astra;
import net.example.per_aspera_ad_astra.block.custom.EiswasserBlock;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Advancements that vanilla triggers can't detect (files in data/per_aspera_ad_astra/advancements/astra).
 * They use the "minecraft:impossible" trigger and are given out from code via {@link #award}.
 * (Entering Astra and killing a yeti use normal vanilla triggers in the JSON.)
 */
@Mod.EventBusSubscriber(modid = Per_aspera_ad_astra.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModAdvancements {
    public static final String FROZEN_IN_EISWASSER = "astra/frozen_in_eiswasser";
    public static final String NO_OXYGEN = "astra/no_oxygen";
    public static final String FELL_THROUGH_ICE = "astra/fell_through_ice";
    /** Killed by a yeti's earthquake shockwave (awarded in YetiEntity#tickEarthquake). */
    public static final String DUNKED_ON = "astra/dunked_on";
    /** Was near the lake when the Great Frostworm got summoned (awarded in FrostBaitItem). */
    public static final String SOMETHING_BITING = "astra/something_biting";

    /** Completes all criteria of the given mod advancement for the player. */
    public static void award(ServerPlayer player, String name) {
        Advancement advancement = player.server.getAdvancements()
                .getAdvancement(new ResourceLocation(Per_aspera_ad_astra.MOD_ID, name));
        if (advancement == null) {
            return;
        }
        AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
        for (String criterion : progress.getRemainingCriteria()) {
            player.getAdvancements().award(advancement, criterion);
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        DamageSource source = event.getSource();
        if (source.is(OxygenEvents.NO_OXYGEN)) {
            award(player, NO_OXYGEN);
        } else if (source.is(DamageTypes.FREEZE) && isInEiswasser(player)) {
            award(player, FROZEN_IN_EISWASSER);
        }
    }

    private static boolean isInEiswasser(ServerPlayer player) {
        return player.level().getBlockState(player.blockPosition()).getBlock() instanceof EiswasserBlock
                || player.level().getBlockState(player.blockPosition().above()).getBlock() instanceof EiswasserBlock;
    }
}
