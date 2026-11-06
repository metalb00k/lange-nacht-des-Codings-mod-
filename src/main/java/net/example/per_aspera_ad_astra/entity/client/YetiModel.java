package net.example.per_aspera_ad_astra.entity.client;

import net.example.per_aspera_ad_astra.Per_aspera_ad_astra;
import net.example.per_aspera_ad_astra.entity.YetiEntity;
import net.minecraft.client.model.IronGolemModel;
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
 * Yeti model, exported from Blockbench (yeti.java).
 *
 * The shape is the iron golem shape, so it extends the vanilla IronGolemModel
 * and gets its walking and punching animations. The part names
 * (head, body, right_arm, left_arm, right_leg, left_leg) already match.
 */
public class YetiModel extends IronGolemModel<YetiEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            new ResourceLocation(Per_aspera_ad_astra.MOD_ID, "yeti"), "main");

    public YetiModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition root = meshdefinition.getRoot();

        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 40).addBox(-9.0F, -2.0F, -6.0F, 18.0F, 12.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(0, 70).addBox(-4.5F, 10.0F, -3.0F, 9.0F, 5.0F, 6.0F, new CubeDeformation(0.5F)),
                PartPose.offset(0.0F, -7.0F, 0.0F));

        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -12.0F, -5.5F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(24, 0).addBox(-1.0F, -5.0F, -7.5F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -7.0F, -2.0F));

        root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(60, 21).addBox(-13.0F, -2.5F, -3.0F, 4.0F, 30.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -7.0F, 0.0F));

        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(60, 58).addBox(9.0F, -2.5F, -3.0F, 4.0F, 30.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -7.0F, 0.0F));

        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(37, 0).addBox(-3.5F, -3.0F, -3.0F, 6.0F, 16.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-4.0F, 11.0F, 0.0F));

        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(60, 0).addBox(-3.5F, -3.0F, -3.0F, 6.0F, 16.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(5.0F, 11.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }
}
