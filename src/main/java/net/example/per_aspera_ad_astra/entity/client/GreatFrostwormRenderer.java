package net.example.per_aspera_ad_astra.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.example.per_aspera_ad_astra.Per_aspera_ad_astra;
import net.example.per_aspera_ad_astra.entity.GreatFrostwormEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Draws the Great Frostworm 2.5x bigger than the Blockbench model (~7 blocks long, ~5.5 blocks tall). */
public class GreatFrostwormRenderer extends MobRenderer<GreatFrostwormEntity, GreatFrostwormModel> {
    public static final float SCALE = 2.5F;
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Per_aspera_ad_astra.MOD_ID, "textures/entity/the_great_frostworm.png");

    public GreatFrostwormRenderer(EntityRendererProvider.Context context) {
        super(context, new GreatFrostwormModel(context.bakeLayer(GreatFrostwormModel.LAYER_LOCATION)), 2.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(GreatFrostwormEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(GreatFrostwormEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }
}
