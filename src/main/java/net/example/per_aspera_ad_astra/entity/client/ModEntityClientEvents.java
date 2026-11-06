package net.example.per_aspera_ad_astra.entity.client;

import net.example.per_aspera_ad_astra.Per_aspera_ad_astra;
import net.example.per_aspera_ad_astra.entity.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Client side only: tells Minecraft which model and renderer each mob uses. */
@Mod.EventBusSubscriber(modid = Per_aspera_ad_astra.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEntityClientEvents {

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SchneeleopardModel.LAYER_LOCATION, SchneeleopardModel::createBodyLayer);
        event.registerLayerDefinition(YetiModel.LAYER_LOCATION, YetiModel::createBodyLayer);
        event.registerLayerDefinition(IceSpiderModel.LAYER_LOCATION, IceSpiderModel::createBodyLayer);
        event.registerLayerDefinition(IceFoxModel.LAYER_LOCATION, IceFoxModel::createBodyLayer);
        event.registerLayerDefinition(IceGhastModel.LAYER_LOCATION, IceGhastModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SCHNEELEOPARD.get(), SchneeleopardRenderer::new);
        event.registerEntityRenderer(ModEntities.YETI.get(), YetiRenderer::new);
        event.registerEntityRenderer(ModEntities.ICE_SPIDER.get(), IceSpiderRenderer::new);
        event.registerEntityRenderer(ModEntities.ICE_FOX.get(), IceFoxRenderer::new);
        event.registerEntityRenderer(ModEntities.ICE_GHAST.get(), IceGhastRenderer::new);
        // The ice charge is drawn as a big flying ice sprite (textures/item/ice_charge.png, 3x size, always bright)
        event.registerEntityRenderer(ModEntities.ICE_CHARGE.get(),
                context -> new net.minecraft.client.renderer.entity.ThrownItemRenderer<>(context, 3.0F, true));
        event.registerEntityRenderer(ModEntities.ICE_SHARD.get(), net.minecraft.client.renderer.entity.ThrownItemRenderer::new);
    }
}
