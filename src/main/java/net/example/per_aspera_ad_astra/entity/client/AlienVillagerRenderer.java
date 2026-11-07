package net.example.per_aspera_ad_astra.entity.client;

import net.example.per_aspera_ad_astra.Per_aspera_ad_astra;
import net.example.per_aspera_ad_astra.entity.AlienVillagerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Draws the alien villager (normal size, about 1.25 blocks tall). */
public class AlienVillagerRenderer extends MobRenderer<AlienVillagerEntity, AlienVillagerModel> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Per_aspera_ad_astra.MOD_ID, "textures/entity/alien.png");

    public AlienVillagerRenderer(EntityRendererProvider.Context context) {
        super(context, new AlienVillagerModel(context.bakeLayer(AlienVillagerModel.LAYER_LOCATION)), 0.6F);
    }

    @Override
    public ResourceLocation getTextureLocation(AlienVillagerEntity entity) {
        return TEXTURE;
    }
}
