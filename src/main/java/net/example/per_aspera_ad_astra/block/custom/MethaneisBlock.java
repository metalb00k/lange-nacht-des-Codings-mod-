package net.example.per_aspera_ad_astra.block.custom;

import net.example.per_aspera_ad_astra.block.ModBlocks;
import net.example.per_aspera_ad_astra.fluid.ModFluids;
import net.minecraft.core.BlockPos;
import net.example.per_aspera_ad_astra.event.ModAdvancements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Thin methane ice: a living entity standing on it for 2 seconds breaks it.
 * If there is Eiswasser below, the hole fills with Eiswasser.
 */
public class MethaneisBlock extends Block {
    /** 2 seconds = 40 ticks */
    private static final int BREAK_DELAY_TICKS = 40;

    public MethaneisBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide && entity instanceof LivingEntity
                && !level.getBlockTicks().hasScheduledTick(pos, this)) {
            level.scheduleTick(pos, this, BREAK_DELAY_TICKS);
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Only break if someone is still standing on it after the 2 seconds
        AABB top = new AABB(pos.getX(), pos.getY() + 1.0, pos.getZ(),
                pos.getX() + 1.0, pos.getY() + 1.25, pos.getZ() + 1.0);
        var standing = level.getEntitiesOfClass(LivingEntity.class, top, e -> !e.isSpectator());
        if (standing.isEmpty()) {
            return;
        }
        for (LivingEntity entity : standing) {
            if (entity instanceof ServerPlayer player) {
                ModAdvancements.award(player, ModAdvancements.FELL_THROUGH_ICE);
            }
        }
        level.destroyBlock(pos, false); // particles + break sound, no drop
        if (level.getFluidState(pos.below()).is(ModFluids.SOURCE_EISWASSER.get())) {
            level.setBlock(pos, ModBlocks.EISWASSER_BLOCK.get().defaultBlockState(), 3);
        }
    }
}
