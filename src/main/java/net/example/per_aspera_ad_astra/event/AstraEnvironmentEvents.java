package net.example.per_aspera_ad_astra.event;

import net.example.per_aspera_ad_astra.Per_aspera_ad_astra;
import net.example.per_aspera_ad_astra.worldgen.dimension.ModDimensions;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BambooSaplingBlock;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.BigDripleafBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChorusFlowerBlock;
import net.minecraft.world.level.block.ChorusPlantBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.GrowingPlantBlock;
import net.minecraft.world.level.block.SporeBlossomBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Rules of the Astra dimension:
 *  1. Normal (vanilla / other mods') plants can't be planted - it's too cold. Our own blocks are allowed.
 *  2. Normal water freezes into ice the moment it is placed (buckets, flowing water, melting ice).
 *     Waterloggable blocks (slabs, stairs, ...) simply can't hold water. Eiswasser is NOT affected.
 */
@Mod.EventBusSubscriber(modid = Per_aspera_ad_astra.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AstraEnvironmentEvents {

    public static boolean isAstra(LevelAccessor level) {
        return level instanceof Level l && l.dimension() == ModDimensions.ASTRA_DIM_LEVEL_KEY;
    }

    // ---------------------------------------------------------------- 1. no plants

    /** True for anything that counts as a "normal plant" (saplings, seeds/crops, flowers, cactus, vines, kelp, ...). */
    private static boolean isForbiddenPlant(BlockState state) {
        Block block = state.getBlock();
        var id = ForgeRegistries.BLOCKS.getKey(block);
        if (id != null && id.getNamespace().equals(Per_aspera_ad_astra.MOD_ID)) {
            return false; // our own blocks are made for Astra
        }
        return block instanceof IPlantable              // saplings, crops, flowers, mushrooms, sugar cane, cactus, ...
                || block instanceof GrowingPlantBlock   // kelp, cave vines, weeping/twisting vines
                || block instanceof VineBlock
                || block instanceof CocoaBlock
                || block instanceof ChorusFlowerBlock || block instanceof ChorusPlantBlock
                || block instanceof BambooSaplingBlock || block instanceof BambooStalkBlock
                || block instanceof BigDripleafBlock
                || block instanceof SporeBlossomBlock
                || state.is(BlockTags.SAPLINGS) || state.is(BlockTags.FLOWERS) || state.is(BlockTags.CROPS);
    }

    @SubscribeEvent
    public static void onPlacePlant(BlockEvent.EntityPlaceEvent event) {
        if (!isAstra(event.getLevel()) || !isForbiddenPlant(event.getPlacedBlock())) {
            return;
        }
        event.setCanceled(true);
        if (event.getEntity() instanceof Player player && !player.level().isClientSide) {
            player.displayClientMessage(Component.translatable("message.per_aspera_ad_astra.too_cold_to_plant")
                    .withStyle(ChatFormatting.AQUA), true);
        }
    }

    // ---------------------------------------------------------------- 2. water freezes instantly

    @SubscribeEvent
    public static void onBlockChanged(BlockEvent.NeighborNotifyEvent event) {
        if (!(event.getLevel() instanceof Level level) || level.isClientSide || !isAstra(level)) {
            return;
        }
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);

        if (state.is(Blocks.WATER)) {
            // Water block (source or flowing) -> ice
            level.setBlock(pos, Blocks.ICE.defaultBlockState(), 3);
            level.playSound(null, pos, SoundEvents.POWDER_SNOW_PLACE, SoundSource.BLOCKS, 1.0f, 0.8f);
        } else if (state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED)) {
            // Waterlogged slab/stair/...: the water freezes away
            level.setBlock(pos, state.setValue(BlockStateProperties.WATERLOGGED, false), 3);
            level.playSound(null, pos, SoundEvents.POWDER_SNOW_PLACE, SoundSource.BLOCKS, 1.0f, 0.8f);
        }
    }
}
