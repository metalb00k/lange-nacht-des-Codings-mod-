package net.example.per_aspera_ad_astra.network;

import net.example.per_aspera_ad_astra.entity.IceShardProjectile;
import net.example.per_aspera_ad_astra.item.custom.SpacesuitChestplateItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Sent when the player presses the ice-shot key (default V, see client/ModKeyBindings).
 * The server checks the chestplate has the freezer upgrade, then fires an ice shard.
 */
public class ShootIcePacket {
    /** Ticks between two shots (10 = 0.5 s). */
    private static final int COOLDOWN_TICKS = 10;

    public static void encode(ShootIcePacket msg, FriendlyByteBuf buf) {
    }

    public static ShootIcePacket decode(FriendlyByteBuf buf) {
        return new ShootIcePacket();
    }

    public static void handle(ShootIcePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player == null || player.isSpectator()) {
            return;
        }
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!SpacesuitChestplateItem.hasUpgrade(chest, SpacesuitChestplateItem.FREEZER)
                || player.getCooldowns().isOnCooldown(chest.getItem())) {
            return;
        }

        IceShardProjectile shard = new IceShardProjectile(player.level(), player);
        shard.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, 2.5f, 0.5f);
        player.level().addFreshEntity(shard);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SNOW_GOLEM_SHOOT, SoundSource.PLAYERS, 1.0f, 1.2f);
        player.getCooldowns().addCooldown(chest.getItem(), COOLDOWN_TICKS);
    }
}
