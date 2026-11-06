package net.example.per_aspera_ad_astra.event;

import net.example.per_aspera_ad_astra.Per_aspera_ad_astra;
import net.example.per_aspera_ad_astra.block.ModBlocks;
import net.example.per_aspera_ad_astra.item.custom.SpacesuitBootsItem;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

/** Ice skates (spacesuit boots upgrade): +50% speed while standing on ice or Methaneis. */
@Mod.EventBusSubscriber(modid = Per_aspera_ad_astra.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class IceSkateEvents {
    private static final UUID SKATE_SPEED_ID = UUID.fromString("6f3c1a52-2d1e-4b8a-9c41-5e7a0b9d3f21");
    private static final double SKATE_SPEED_BONUS = 0.5; // +50%

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (event.phase != TickEvent.Phase.END || player.level().isClientSide) {
            return;
        }
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }
        boolean skating = player.onGround() && SpacesuitBootsItem.wearsSkates(player)
                && isIce(player.level().getBlockState(player.getOnPos()));
        boolean hasBonus = speed.getModifier(SKATE_SPEED_ID) != null;
        if (skating && !hasBonus) {
            speed.addTransientModifier(new AttributeModifier(SKATE_SPEED_ID, "Ice skates",
                    SKATE_SPEED_BONUS, AttributeModifier.Operation.MULTIPLY_TOTAL));
        } else if (!skating && hasBonus) {
            speed.removeModifier(SKATE_SPEED_ID);
        }
    }

    private static boolean isIce(BlockState state) {
        return state.is(BlockTags.ICE) || state.is(ModBlocks.METHANEIS.get()) || state.is(ModBlocks.EISBLOCK.get());
    }
}
