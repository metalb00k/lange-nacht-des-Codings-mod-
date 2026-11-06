package net.example.per_aspera_ad_astra.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.example.per_aspera_ad_astra.Per_aspera_ad_astra;
import net.example.per_aspera_ad_astra.entity.IceGhastEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Draws the ice ghast, 4.5x bigger than the model – same as the vanilla ghast. */
public class IceGhastRenderer extends MobRenderer<IceGhastEntity, IceGhastModel> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Per_aspera_ad_astra.MOD_ID, "textures/entity/ice_ghast.png");

    public IceGhastRenderer(EntityRendererProvider.Context context) {
        super(context, new IceGhastModel(context.bakeLayer(IceGhastModel.LAYER_LOCATION)), 1.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(IceGhastEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(IceGhastEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(4.5F, 4.5F, 4.5F);
    }
}
