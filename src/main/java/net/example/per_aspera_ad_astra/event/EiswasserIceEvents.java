package net.example.per_aspera_ad_astra.event;

import net.example.per_aspera_ad_astra.Per_aspera_ad_astra;
import net.example.per_aspera_ad_astra.block.ModBlocks;
import net.example.per_aspera_ad_astra.fluid.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Vanilla ice turns into NORMAL water when you break it. If the ice is sitting on Eiswasser,
 * turn it into Eiswasser instead, so the frozen lakes stay Eiswasser.
 * (With Silk Touch you get the ice block and nothing is placed, same as vanilla.)
 */
@Mod.EventBusSubscriber(modid = Per_aspera_ad_astra.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EiswasserIceEvents {

    @SubscribeEvent
    public static void onBreakIce(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof Level level) || level.isClientSide) {
            return;
        }
        BlockPos pos = event.getPos();
        boolean isMethaneis = event.getState().is(ModBlocks.METHANEIS.get());
        if (isMethaneis && level.getFluidState(pos.below()).is(ModFluids.SOURCE_EISWASSER.get())
                && !event.getPlayer().isCreative()) {
            // Methaneis (the frozen surface of Astra's lakes) -> the Eiswasser below shows through.
            // The block still drops normally (loot table), only the hole is filled with Eiswasser.
            level.getServer().execute(() -> {
                if (level.isEmptyBlock(pos)) {
                    level.setBlock(pos, ModBlocks.EISWASSER_BLOCK.get().defaultBlockState(), 3);
                }
            });
            return;
        }
        if (!event.getState().is(Blocks.ICE)
                || event.getPlayer().isCreative()
                || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, event.getPlayer().getMainHandItem()) > 0) {
            return;
        }
        if (level.getFluidState(pos.below()).is(ModFluids.SOURCE_EISWASSER.get())) {
            // Ice on Eiswasser -> Eiswasser (anywhere)
            event.setCanceled(true);
            level.setBlock(pos, ModBlocks.EISWASSER_BLOCK.get().defaultBlockState(), 3);
        } else if (AstraEnvironmentEvents.isAstra(level)) {
            // In Astra, normal water would instantly freeze back into ice (see AstraEnvironmentEvents),
            // so broken ice just disappears instead of turning into water.
            event.setCanceled(true);
            level.removeBlock(pos, false);
        }
    }
}
