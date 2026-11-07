package net.example.per_aspera_ad_astra.network;

import net.example.per_aspera_ad_astra.Per_aspera_ad_astra;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/** Client <-> server messages (e.g. "player pressed the ice-shot key"). */
public class ModNetwork {
    private static final String VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Per_aspera_ad_astra.MOD_ID, "main"), () -> VERSION, VERSION::equals, VERSION::equals);

    public static void register() {
        int id = 0;
        CHANNEL.messageBuilder(ShootIcePacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ShootIcePacket::encode)
                .decoder(ShootIcePacket::decode)
                .consumerMainThread(ShootIcePacket::handle)
                .add();
        CHANNEL.messageBuilder(JetpackPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(JetpackPacket::encode)
                .decoder(JetpackPacket::decode)
                .consumerMainThread(JetpackPacket::handle)
                .add();
    }
}
