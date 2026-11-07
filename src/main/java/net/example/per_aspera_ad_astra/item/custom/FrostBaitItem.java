package net.example.per_aspera_ad_astra.item.custom;

import net.example.per_aspera_ad_astra.entity.GreatFrostwormEntity;
import net.example.per_aspera_ad_astra.entity.ModEntities;
import net.example.per_aspera_ad_astra.fluid.ModFluids;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Frost Bait ("Frostköder"): throw it (Q) into Eiswasser -> after 3 seconds of bubbling
 * the Great Frostworm bursts out of the water.
 * Only one worm per area: nothing happens if one is already alive within 64 blocks.
 */
public class FrostBaitItem extends Item {
    /** How long the bait has to lie in the Eiswasser before the worm comes (60 = 3 s). */
    private static final int SUMMON_DELAY = 60;
    private static final String TICKS_TAG = "per_aspera_ad_astra.BaitTicks";

    public FrostBaitItem(Properties properties) {
        super(properties);
    }

    /** Called every tick while the item lies on the ground / floats as a dropped item. */
    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity item) {
        if (!(item.level() instanceof ServerLevel level)) {
            return false;
        }
        if (!isInEiswasser(item)) {
            item.getPersistentData().putInt(TICKS_TAG, 0);
            return false;
        }
        if (!level.getEntitiesOfClass(GreatFrostwormEntity.class, item.getBoundingBox().inflate(64)).isEmpty()) {
            return false; // the worm is already here
        }

        int ticks = item.getPersistentData().getInt(TICKS_TAG) + 1;
        item.getPersistentData().putInt(TICKS_TAG, ticks);

        // Something is coming...
        if (ticks % 4 == 0) {
            level.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, item.getX(), item.getY(), item.getZ(),
                    8 + ticks / 4, 0.6 + ticks * 0.02, 0.1, 0.6 + ticks * 0.02, 0.1);
        }
        if (ticks == 1) {
            level.playSound(null, item.getX(), item.getY(), item.getZ(),
                    SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.HOSTILE, 2.0F, 0.5F);
        }
        if (ticks % 20 == 0 && ticks < SUMMON_DELAY) {
            level.playSound(null, item.getX(), item.getY(), item.getZ(),
                    SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_AMBIENT, SoundSource.HOSTILE, 3.0F, 0.5F);
        }

        if (ticks >= SUMMON_DELAY) {
            GreatFrostwormEntity worm = ModEntities.GREAT_FROSTWORM.get().create(level);
            if (worm != null) {
                Player nearest = level.getNearestPlayer(item, 64);
                float yaw = nearest != null
                        ? (float) Math.toDegrees(Math.atan2(nearest.getZ() - item.getZ(), nearest.getX() - item.getX())) - 90.0F
                        : item.getYRot();
                worm.moveTo(item.getX(), item.getY() - 2.0, item.getZ(), yaw, 0.0F);
                worm.setYHeadRot(yaw);
                worm.finalizeSpawn(level, level.getCurrentDifficultyAt(worm.blockPosition()), MobSpawnType.EVENT, null, null);
                level.addFreshEntity(worm);
                // Advancement "Something's Biting" for everyone who sees it happen
                for (var player : level.getEntitiesOfClass(net.minecraft.server.level.ServerPlayer.class,
                        item.getBoundingBox().inflate(48))) {
                    net.example.per_aspera_ad_astra.event.ModAdvancements.award(player,
                            net.example.per_aspera_ad_astra.event.ModAdvancements.SOMETHING_BITING);
                }
                // Bursts out of the water (through the ice) and slams down next to the nearest player
                worm.launchLeapAt(nearest != null ? nearest.position() : worm.position());

                level.sendParticles(ParticleTypes.SPLASH, item.getX(), item.getY() + 0.5, item.getZ(), 200, 2.5, 1.0, 2.5, 0.5);
                level.sendParticles(ParticleTypes.EXPLOSION, item.getX(), item.getY() + 1, item.getZ(), 5, 2, 1, 2, 0);
                level.playSound(null, item.getX(), item.getY(), item.getZ(),
                        SoundEvents.ELDER_GUARDIAN_DEATH, SoundSource.HOSTILE, 4.0F, 0.5F);
                level.playSound(null, item.getX(), item.getY(), item.getZ(),
                        SoundEvents.GENERIC_SPLASH, SoundSource.HOSTILE, 3.0F, 0.6F);
            }
            item.discard(); // the bait is eaten
            return true;
        }
        return false;
    }

    private static boolean isInEiswasser(ItemEntity item) {
        FluidState here = item.level().getFluidState(item.blockPosition());
        FluidState below = item.level().getFluidState(item.blockPosition().below());
        return isEiswasser(here) || isEiswasser(below);
    }

    private static boolean isEiswasser(FluidState fluid) {
        return fluid.is(ModFluids.SOURCE_EISWASSER.get()) || fluid.is(ModFluids.FLOWING_EISWASSER.get());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.per_aspera_ad_astra.frost_bait").withStyle(ChatFormatting.AQUA));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
