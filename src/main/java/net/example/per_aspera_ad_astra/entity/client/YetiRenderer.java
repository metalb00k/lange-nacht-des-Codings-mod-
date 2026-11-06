package net.example.per_aspera_ad_astra.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.example.per_aspera_ad_astra.Per_aspera_ad_astra;
import net.example.per_aspera_ad_astra.entity.YetiEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Draws the yeti. Same body sway while walking as the vanilla IronGolemRenderer. */
public class YetiRenderer extends MobRenderer<YetiEntity, YetiModel> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Per_aspera_ad_astra.MOD_ID, "textures/entity/yeti.png");

    public YetiRenderer(EntityRendererProvider.Context context) {
        super(context, new YetiModel(context.bakeLayer(YetiModel.LAYER_LOCATION)), 0.7F);
    }

    @Override
    public ResourceLocation getTextureLocation(YetiEntity entity) {
        return TEXTURE;
    }

    /** The miniboss (Yeti-Anführer from the nest) is drawn bigger. */
    @Override
    protected void scale(YetiEntity entity, PoseStack poseStack, float partialTick) {
        super.scale(entity, poseStack, partialTick);
        if (entity.isMiniboss()) {
            poseStack.scale(YetiEntity.BOSS_SCALE, YetiEntity.BOSS_SCALE, YetiEntity.BOSS_SCALE);
        }
    }

    @Override
    protected void setupRotations(YetiEntity entity, PoseStack poseStack, float ageInTicks,
                                  float rotationYaw, float partialTicks) {
        super.setupRotations(entity, poseStack, ageInTicks, rotationYaw, partialTicks);
        if (entity.walkAnimation.speed() >= 0.01F) {
            float pos = entity.walkAnimation.position(partialTicks) + 6.0F;
            float sway = (Math.abs(pos % 13.0F - 6.5F) - 3.25F) / 3.25F;
            poseStack.mulPose(Axis.ZP.rotationDegrees(6.5F * sway));
        }
    }
}
