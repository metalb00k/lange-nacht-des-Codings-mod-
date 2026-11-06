package net.example.per_aspera_ad_astra.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.example.per_aspera_ad_astra.Per_aspera_ad_astra;
import net.example.per_aspera_ad_astra.entity.SchneeleopardEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Draws the snow leopard. */
public class SchneeleopardRenderer extends MobRenderer<SchneeleopardEntity, SchneeleopardModel> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Per_aspera_ad_astra.MOD_ID, "textures/entity/schneeleopard.png");

    /** Size of the snow leopard. Change this to make it bigger/smaller (also change .sized(...) in ModEntities). */
    private static final float SCALE = 1.5F;

    public SchneeleopardRenderer(EntityRendererProvider.Context context) {
        super(context, new SchneeleopardModel(context.bakeLayer(SchneeleopardModel.LAYER_LOCATION)), 0.4F * SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(SchneeleopardEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(SchneeleopardEntity entity, PoseStack poseStack, float partialTick) {
        super.scale(entity, poseStack, partialTick);
        // SCALE 1.5 = one and a half times the size of the Blockbench model.
        poseStack.scale(SCALE, SCALE, SCALE);
    }
}
