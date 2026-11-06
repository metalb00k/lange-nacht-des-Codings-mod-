package net.example.per_aspera_ad_astra.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.example.per_aspera_ad_astra.Per_aspera_ad_astra;
import net.minecraft.client.model.FoxModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.FoxHeldItemLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Fox;

/**
 * Draws the ice fox. Works like the vanilla FoxRenderer:
 * shows the item in its mouth and tilts the body when pouncing.
 */
public class IceFoxRenderer extends MobRenderer<Fox, FoxModel<Fox>> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Per_aspera_ad_astra.MOD_ID, "textures/entity/ice_fox.png");

    public IceFoxRenderer(EntityRendererProvider.Context context) {
        super(context, new IceFoxModel(context.bakeLayer(IceFoxModel.LAYER_LOCATION)), 0.4F);
        this.addLayer(new FoxHeldItemLayer(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(Fox entity) {
        return TEXTURE;
    }

    @Override
    protected void setupRotations(Fox fox, PoseStack poseStack, float ageInTicks,
                                  float rotationYaw, float partialTicks) {
        super.setupRotations(fox, poseStack, ageInTicks, rotationYaw, partialTicks);
        if (fox.isPouncing() || fox.isFaceplanted()) {
            float pitch = -Mth.lerp(partialTicks, fox.xRotO, fox.getXRot());
            poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        }
    }
}
