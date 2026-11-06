package net.example.per_aspera_ad_astra.entity.client;

import net.example.per_aspera_ad_astra.Per_aspera_ad_astra;
import net.example.per_aspera_ad_astra.entity.IceSpiderEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Draws the ice spider. */
public class IceSpiderRenderer extends MobRenderer<IceSpiderEntity, IceSpiderModel> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Per_aspera_ad_astra.MOD_ID, "textures/entity/ice_spider.png");

    public IceSpiderRenderer(EntityRendererProvider.Context context) {
        super(context, new IceSpiderModel(context.bakeLayer(IceSpiderModel.LAYER_LOCATION)), 0.8F);
    }

    @Override
    public ResourceLocation getTextureLocation(IceSpiderEntity entity) {
        return TEXTURE;
    }

    /** Spiders roll onto their back when they die. */
    @Override
    protected float getFlipDegrees(IceSpiderEntity entity) {
        return 180.0F;
    }
}
