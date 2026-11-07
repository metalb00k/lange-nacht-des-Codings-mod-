package net.example.per_aspera_ad_astra.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.example.per_aspera_ad_astra.Per_aspera_ad_astra;
import net.example.per_aspera_ad_astra.network.ModNetwork;
import net.example.per_aspera_ad_astra.network.ShootIcePacket;
import net.example.per_aspera_ad_astra.network.JetpackPacket;
import net.example.per_aspera_ad_astra.item.custom.SpacesuitChestplateItem;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/** Key to shoot ice with the freezer-upgraded spacesuit. Default: V (change it in Options > Controls). */
public class ModKeyBindings {
    public static final KeyMapping SHOOT_ICE = new KeyMapping("key.per_aspera_ad_astra.shoot_ice",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, "key.categories.per_aspera_ad_astra");

    @Mod.EventBusSubscriber(modid = Per_aspera_ad_astra.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(SHOOT_ICE);
        }
    }

    @Mod.EventBusSubscriber(modid = Per_aspera_ad_astra.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ForgeBusEvents {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END || Minecraft.getInstance().player == null) {
                return;
            }
            // Holding the key keeps firing; the server cooldown limits the rate
            if (SHOOT_ICE.isDown()) {
                ModNetwork.CHANNEL.sendToServer(new ShootIcePacket());
            }
            tickJetpack(Minecraft.getInstance());
        }

        /**
         * Jetpack: hold JUMP while in the air -> thrust upwards (max 0.5 blocks/tick up).
         * Movement has to happen here on the client; the server part (oxygen, particles, no fall damage)
         * is in JetpackPacket.
         */
        private static void tickJetpack(Minecraft mc) {
            LocalPlayer player = mc.player;
            if (player == null || player.isSpectator() || player.getAbilities().flying
                    || player.onGround() || player.isInWater() || !mc.options.keyJump.isDown()) {
                return;
            }
            ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
            if (!SpacesuitChestplateItem.hasUpgrade(chest, SpacesuitChestplateItem.JETPACK)
                    || (!player.isCreative() && SpacesuitChestplateItem.getOxygen(chest) <= 0)) {
                return;
            }
            Vec3 motion = player.getDeltaMovement();
            player.setDeltaMovement(motion.x, Math.min(motion.y + 0.15, 0.5), motion.z);
            player.fallDistance = 0;
            ModNetwork.CHANNEL.sendToServer(new JetpackPacket());
        }
    }
}
