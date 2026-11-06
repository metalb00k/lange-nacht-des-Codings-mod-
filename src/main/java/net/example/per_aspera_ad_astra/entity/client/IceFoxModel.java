package net.example.per_aspera_ad_astra.entity.client;

import net.example.per_aspera_ad_astra.Per_aspera_ad_astra;
import net.minecraft.client.model.FoxModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Fox;

/**
 * Ice fox model, exported from Blockbench (icefox.java).
 *
 * It extends the vanilla FoxModel, so it walks, sits, sleeps, crouches and pounces
 * like a fox. For that the parts were renamed to the vanilla names:
 *   leg1 -> right_hind_leg, leg2 -> left_hind_leg,
 *   leg3 -> right_front_leg, leg4 -> left_front_leg
 * and the tail is a child of the body (the vanilla fox animation needs that),
 * so its position is now relative to the body.
 *
 * It is a FoxModel<Fox> (not <IceFoxEntity>) so the vanilla "item in mouth" layer works.
 */
public class IceFoxModel extends FoxModel<Fox> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            new ResourceLocation(Per_aspera_ad_astra.MOD_ID, "ice_fox"), "main");

    public IceFoxModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition root = meshdefinition.getRoot();

        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(1, 5).addBox(-3.0F, -2.0F, -5.0F, 8.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(15, 1).addBox(3.0F, -4.0F, -4.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(8, 1).addBox(-3.0F, -4.0F, -4.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(6, 18).addBox(-1.0F, 2.0F, -8.0F, 4.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-1.0F, 16.5F, -3.0F));

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(24, 15).addBox(-3.0F, 4.0F, -3.5F, 6.0F, 11.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 16.0F, -6.0F, 1.5708F, 0.0F, 0.0F));

        // Tail hangs off the back of the body (same place as on the vanilla fox)
        body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(30, 0).addBox(2.0F, 0.0F, -1.0F, 4.0F, 9.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-4.0F, 15.0F, -1.0F, -0.05235988F, 0.0F, 0.0F));

        root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create()
                .texOffs(13, 24).addBox(1.999F, 0.5F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-5.0F, 17.5F, 7.0F));
        root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create()
                .texOffs(4, 24).addBox(2.001F, 0.5F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-1.0F, 17.5F, 7.0F));
        root.addOrReplaceChild("right_front_leg", CubeListBuilder.create()
                .texOffs(13, 24).addBox(1.999F, 0.5F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-5.0F, 17.5F, 0.0F));
        root.addOrReplaceChild("left_front_leg", CubeListBuilder.create()
                .texOffs(4, 24).addBox(2.001F, 0.5F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-1.0F, 17.5F, 0.0F));

        return LayerDefinition.create(meshdefinition, 48, 32);
    }
}
