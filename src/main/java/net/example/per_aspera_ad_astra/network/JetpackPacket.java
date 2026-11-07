package net.example.per_aspera_ad_astra.network;

import net.example.per_aspera_ad_astra.item.custom.SpacesuitChestplateItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Sent every tick while the player is thrusting with the jetpack (client/ModKeyBindings does the movement,
 * because player movement is decided on the client in Minecraft).
 * The server: uses up oxygen, resets fall damage, shows flames + smoke and plays the engine sound for everyone.
 */
public class JetpackPacket {

    public static void encode(JetpackPacket msg, FriendlyByteBuf buf) {
    }

    public static JetpackPacket decode(FriendlyByteBuf buf) {
        return new JetpackPacket();
    }

    public static void handle(JetpackPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player == null || player.isSpectator()) {
            return;
        }
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!SpacesuitChestplateItem.hasUpgrade(chest, SpacesuitChestplateItem.JETPACK)) {
            return;
        }
        if (!player.isCreative()) {
            int oxygen = SpacesuitChestplateItem.getOxygen(chest);
            if (oxygen <= 0) {
                return;
            }
            SpacesuitChestplateItem.setOxygen(chest, oxygen - SpacesuitChestplateItem.JETPACK_OXYGEN_PER_TICK);
        }
        player.fallDistance = 0;

        // Flames + smoke out of the back
        ServerLevel level = player.serverLevel();
        Vec3 back = Vec3.directionFromRotation(0, player.getYRot()).scale(-0.35);
        double x = player.getX() + back.x, y = player.getY() + 0.7, z = player.getZ() + back.z;
        level.sendParticles(ParticleTypes.FLAME, x, y, z, 2, 0.08, 0.05, 0.08, 0.01);
        level.sendParticles(ParticleTypes.SMOKE, x, y - 0.2, z, 1, 0.1, 0.05, 0.1, 0.01);
        if (player.tickCount % 4 == 0) {
            level.playSound(null, x, y, z, SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 0.6F, 1.6F);
        }
    }
}
