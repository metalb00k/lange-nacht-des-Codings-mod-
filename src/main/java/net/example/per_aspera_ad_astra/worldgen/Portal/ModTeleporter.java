package net.example.per_aspera_ad_astra.worldgen.Portal;

import net.example.per_aspera_ad_astra.block.ModBlocks;
import net.example.per_aspera_ad_astra.block.custom.ModPortalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.util.ITeleporter;

import java.util.function.Function;

/**
 * Moves the player to the other dimension at the same X/Z, finds a free spot,
 * and (when entering the Astra dimension) places a portal block there so the player can get back.
 */
public class ModTeleporter implements ITeleporter {
    private final BlockPos fromPos;
    private final boolean goingIntoAstra;

    public ModTeleporter(BlockPos fromPos, boolean goingIntoAstra) {
        this.fromPos = fromPos;
        this.goingIntoAstra = goingIntoAstra;
    }

    @Override
    public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destinationWorld,
                              float yaw, Function<Boolean, Entity> repositionEntity) {
        entity = repositionEntity.apply(false);

        int startY = goingIntoAstra ? 61 : fromPos.getY();
        BlockPos destinationPos = new BlockPos(fromPos.getX(), startY, fromPos.getZ());

        // Move up until there are two free blocks for the player
        int tries = 0;
        while ((!destinationWorld.isEmptyBlock(destinationPos) || !destinationWorld.isEmptyBlock(destinationPos.above()))
                && tries < 100) {
            destinationPos = destinationPos.above();
            tries++;
        }

        entity.teleportTo(destinationPos.getX() + 0.5, destinationPos.getY(), destinationPos.getZ() + 0.5);

        if (goingIntoAstra) {
            // Only place a return portal if there isn't one nearby already
            boolean portalNearby = false;
            for (BlockPos checkPos : BlockPos.betweenClosed(destinationPos.offset(-10, -10, -10), destinationPos.offset(10, 10, 10))) {
                if (destinationWorld.getBlockState(checkPos).getBlock() instanceof ModPortalBlock) {
                    portalNearby = true;
                    break;
                }
            }
            if (!portalNearby) {
                destinationWorld.setBlock(destinationPos.east(2), ModBlocks.MOD_PORTAL.get().defaultBlockState(), 3);
            }
        }
        return entity;
    }
}
