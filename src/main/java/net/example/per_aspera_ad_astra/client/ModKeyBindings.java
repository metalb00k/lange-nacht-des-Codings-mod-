package net.example.per_aspera_ad_astra.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.example.per_aspera_ad_astra.Per_aspera_ad_astra;
import net.example.per_aspera_ad_astra.network.ModNetwork;
import net.example.per_aspera_ad_astra.network.ShootIcePacket;
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
        }
    }
}
