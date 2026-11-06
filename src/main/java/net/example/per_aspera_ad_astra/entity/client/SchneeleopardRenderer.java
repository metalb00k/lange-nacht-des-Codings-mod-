package net.example.per_aspera_ad_astra.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.example.per_aspera_ad_astra.Per_aspera_ad_astra;
import net.example.per_aspera_ad_astra.entity.SchneeleopardEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

/**
 * Draws the snow leopard. Same scaling and "lie down next to a sleeping player"
 * rotation as the vanilla CatRenderer.
 */
public class SchneeleopardRenderer extends MobRenderer<SchneeleopardEntity, SchneeleopardModel> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Per_aspera_ad_astra.MOD_ID, "textures/entity/schneeleopard.png");

    /** Size of the snow leopard. Change this to make it bigger/smaller (also change .sized(...) in ModEntities). */
    private static final float SCALE = 1.5F;
    /** The lie-down offsets below were made for a cat at 0.8, so they are scaled to our size. */
    private static final float LIE_OFFSET_SCALE = SCALE / 0.8F;

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
        // Vanilla cats are drawn at 0.8. The snow leopard is drawn at SCALE (1.5 = one and a half size).
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    protected void setupRotations(SchneeleopardEntity entity, PoseStack poseStack, float ageInTicks,
                                  float rotationYaw, float partialTicks) {
        super.setupRotations(entity, poseStack, ageInTicks, rotationYaw, partialTicks);
        float lie = entity.getLieDownAmount(partialTicks);
        if (lie > 0.0F) {
            float s = LIE_OFFSET_SCALE;
            poseStack.translate(0.4F * lie * s, 0.15F * lie * s, 0.1F * lie * s);
            poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.rotLerp(lie, 0.0F, 90.0F)));
            BlockPos pos = entity.blockPosition();
            for (Player player : entity.level().getEntitiesOfClass(Player.class, new AABB(pos).inflate(2.0D))) {
                if (player.isSleeping()) {
                    poseStack.translate(0.15F * lie * s, 0.0F, 0.0F);
                    break;
                }
            }
        }
    }
}
