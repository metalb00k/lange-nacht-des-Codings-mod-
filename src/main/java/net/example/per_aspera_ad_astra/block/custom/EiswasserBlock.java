package net.example.per_aspera_ad_astra.block.custom;

import net.example.per_aspera_ad_astra.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

import java.util.function.Supplier;

/**
 * Green ice water: everything alive that stands/swims in it freezes and takes cold damage.
 */
public class EiswasserBlock extends LiquidBlock {
    // Damage per hit (1.0 = half a heart) and how often (20 ticks = 1 second)
    private static final float COLD_DAMAGE = 2.0f;
    private static final int DAMAGE_INTERVAL_TICKS = 20;

    public EiswasserBlock(Supplier<? extends FlowingFluid> fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        if (level.isClientSide || !(entity instanceof LivingEntity living)) {
            return;
        }
        // A heater keeps you warm: no freezing, no damage
        if (hasHeater(living)) {
            return;
        }

        // Frost overlay on the screen (like powder snow). Vanilla melts it by 2 per tick, so +3 makes it grow.
        int maxFrozen = living.getTicksRequiredToFreeze() + 20;
        living.setTicksFrozen(Math.min(living.getTicksFrozen() + 3, maxFrozen));

        if (living.tickCount % DAMAGE_INTERVAL_TICKS == 0) {
            living.hurt(level.damageSources().freeze(), COLD_DAMAGE);
        }
    }

    /** Players: heater anywhere in the inventory. Mobs: heater in a hand. */
    private static boolean hasHeater(LivingEntity living) {
        if (living instanceof Player player) {
            return player.getInventory().hasAnyMatching(stack -> stack.is(ModItems.HEATER.get()));
        }
        return living.getMainHandItem().is(ModItems.HEATER.get())
                || living.getOffhandItem().is(ModItems.HEATER.get());
    }
}
