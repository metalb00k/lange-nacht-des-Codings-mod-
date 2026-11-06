package net.example.per_aspera_ad_astra.entity.client;

import net.example.per_aspera_ad_astra.Per_aspera_ad_astra;
import net.example.per_aspera_ad_astra.entity.IceSpiderEntity;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

/**
 * Ice spider model, exported from Blockbench (ice spider.java).
 *
 * It extends the vanilla SpiderModel, so the legs move like a spider's.
 * For that the parts were renamed to the vanilla names:
 *   neck -> body0, body -> body1,
 *   leg1/leg2 -> right/left_hind_leg, leg3/leg4 -> right/left_middle_hind_leg,
 *   leg5/leg6 -> right/left_middle_front_leg, leg7/leg8 -> right/left_front_leg
 */
public class IceSpiderModel extends SpiderModel<IceSpiderEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            new ResourceLocation(Per_aspera_ad_astra.MOD_ID, "ice_spider"), "main");

    public IceSpiderModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition root = meshdefinition.getRoot();

        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(32, 4).addBox(-4.0F, -4.0F, -8.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 15.0F, -3.0F));

        root.addOrReplaceChild("body0", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 15.0F, 0.0F));

        root.addOrReplaceChild("body1", CubeListBuilder.create()
                .texOffs(0, 12).addBox(-5.0F, -4.0F, -6.0F, 10.0F, 8.0F, 12.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 15.0F, 9.0F));

        CubeListBuilder rightLeg = CubeListBuilder.create()
                .texOffs(18, 0).addBox(-15.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F, new CubeDeformation(0.0F));
        CubeListBuilder leftLeg = CubeListBuilder.create()
                .texOffs(18, 0).addBox(-1.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F, new CubeDeformation(0.0F));

        root.addOrReplaceChild("right_hind_leg", rightLeg, PartPose.offset(-4.0F, 15.0F, 4.0F));
        root.addOrReplaceChild("left_hind_leg", leftLeg, PartPose.offset(4.0F, 15.0F, 4.0F));
        root.addOrReplaceChild("right_middle_hind_leg", rightLeg, PartPose.offset(-4.0F, 15.0F, 1.0F));
        root.addOrReplaceChild("left_middle_hind_leg", leftLeg, PartPose.offset(4.0F, 15.0F, 1.0F));
        root.addOrReplaceChild("right_middle_front_leg", rightLeg, PartPose.offset(-4.0F, 15.0F, -2.0F));
        root.addOrReplaceChild("left_middle_front_leg", leftLeg, PartPose.offset(4.0F, 15.0F, -2.0F));
        root.addOrReplaceChild("right_front_leg", rightLeg, PartPose.offset(-4.0F, 15.0F, -5.0F));
        root.addOrReplaceChild("left_front_leg", leftLeg, PartPose.offset(4.0F, 15.0F, -5.0F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }
}
