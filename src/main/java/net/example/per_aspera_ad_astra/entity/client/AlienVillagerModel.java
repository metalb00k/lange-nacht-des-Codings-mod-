package net.example.per_aspera_ad_astra.entity.client;

import net.example.per_aspera_ad_astra.Per_aspera_ad_astra;
import net.example.per_aspera_ad_astra.entity.AlienVillagerEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Alien villager, made in Blockbench ("alien.java").
 *
 * The export had all cubes in one group (bb_main). Here the same cubes (same sizes + texture offsets,
 * so it looks identical) are split into parts that can move:
 *
 *   head (the big head-body blob)
 *    ├─ left_eye / right_eye   (eye stalks, wobble)
 *    └─ snout                  (swings like a trunk)
 *        └─ trunk
 *            └─ trunk_tip
 *   left_leg / right_leg (with feet)   (walk)
 *   left_arm / right_arm (long arms)   (swing + sway)
 *
 * Animations are all in setupAnim (code, no keyframes).
 */
public class AlienVillagerModel extends HierarchicalModel<AlienVillagerEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            new ResourceLocation(Per_aspera_ad_astra.MOD_ID, "alien_villager"), "main");

    private final ModelPart root;
    private final ModelPart head, leftEye, rightEye, snout, trunk, trunkTip;
    private final ModelPart leftLeg, rightLeg, leftArm, rightArm;

    public AlienVillagerModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.leftEye = head.getChild("left_eye");
        this.rightEye = head.getChild("right_eye");
        this.snout = head.getChild("snout");
        this.trunk = snout.getChild("trunk");
        this.trunkTip = trunk.getChild("trunk_tip");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
        this.leftArm = root.getChild("left_arm");
        this.rightArm = root.getChild("right_arm");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        CubeDeformation none = new CubeDeformation(0.0F);

        // Head-body blob, pivot at its bottom middle
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-8.5F, -11.0F, -8.5F, 17.0F, 11.0F, 17.0F, none),
                PartPose.offset(0.5F, 15.0F, -0.5F));

        head.addOrReplaceChild("left_eye", CubeListBuilder.create()
                        .texOffs(8, 47).addBox(-1.0F, -1.0F, -2.0F, 2.0F, 2.0F, 2.0F, none),
                PartPose.offset(4.5F, -7.0F, -8.5F));
        head.addOrReplaceChild("right_eye", CubeListBuilder.create()
                        .texOffs(44, 44).addBox(-1.0F, -1.0F, -2.0F, 2.0F, 2.0F, 2.0F, none),
                PartPose.offset(-4.5F, -7.0F, -8.5F));

        PartDefinition snout = head.addOrReplaceChild("snout", CubeListBuilder.create()
                        .texOffs(44, 33).addBox(-1.5F, -2.0F, -2.0F, 3.0F, 4.0F, 2.0F, none),
                PartPose.offset(0.0F, -4.0F, -8.5F));
        PartDefinition trunk = snout.addOrReplaceChild("trunk", CubeListBuilder.create()
                        .texOffs(44, 28).addBox(-1.5F, -1.0F, -3.0F, 3.0F, 2.0F, 3.0F, none),
                PartPose.offset(0.0F, 0.0F, -2.0F));
        trunk.addOrReplaceChild("trunk_tip", CubeListBuilder.create()
                        .texOffs(44, 39).addBox(-0.5F, -1.0F, -3.0F, 1.0F, 2.0F, 3.0F, none),
                PartPose.offset(0.0F, 0.0F, -3.0F));

        // Legs (pivot at the hip) with their feet
        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                        .texOffs(18, 28).addBox(-2.5F, 0.0F, -2.0F, 5.0F, 9.0F, 4.0F, none)
                        .texOffs(8, 41).addBox(-1.5F, 7.0F, -5.0F, 3.0F, 2.0F, 4.0F, none),
                PartPose.offset(4.5F, 15.0F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                        .texOffs(0, 28).addBox(-2.5F, 0.0F, -2.0F, 5.0F, 9.0F, 4.0F, none)
                        .texOffs(22, 41).addBox(-1.5F, 7.0F, -5.0F, 3.0F, 2.0F, 4.0F, none),
                PartPose.offset(-3.5F, 15.0F, 0.0F));

        // Long arms (pivot at the shoulder)
        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                        .texOffs(36, 28).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 17.0F, 2.0F, none),
                PartPose.offset(10.0F, 7.0F, 0.0F));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                        .texOffs(0, 41).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 17.0F, 2.0F, none),
                PartPose.offset(-9.0F, 7.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(AlienVillagerEntity alien, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        float deg = Mth.DEG_TO_RAD;
        float walk = Math.min(limbSwingAmount, 1.0F);
        float step = Mth.cos(limbSwing * 0.6662F);

        // Walking: legs swing, arms swing the other way (small, they almost touch the ground)
        leftLeg.xRot = step * 1.0F * walk;
        rightLeg.xRot = -step * 1.0F * walk;
        leftArm.xRot = -step * 0.5F * walk;
        rightArm.xRot = step * 0.5F * walk;

        // Idle: arms sway a little outwards, body bobs while walking
        leftArm.zRot = -0.05F - Mth.sin(ageInTicks * 0.07F) * 0.05F;
        rightArm.zRot = 0.05F + Mth.sin(ageInTicks * 0.07F) * 0.05F;
        head.y += Math.abs(Mth.sin(limbSwing * 0.6662F)) * walk * -0.8F;

        // Head (whole blob) turns a bit towards what it looks at
        head.yRot = Mth.clamp(netHeadYaw, -40.0F, 40.0F) * deg * 0.5F;
        head.xRot = headPitch * deg * 0.3F;

        // "No" head shake when you click it without trades / it can't trade right now
        if (alien.getUnhappyCounter() > 0) {
            head.zRot = 0.3F * Mth.sin(ageInTicks * 0.9F);
        }

        // Eye stalks wobble independently
        leftEye.xRot = Mth.sin(ageInTicks * 0.15F) * 0.15F;
        leftEye.yRot = Mth.cos(ageInTicks * 0.11F) * 0.15F;
        rightEye.xRot = Mth.sin(ageInTicks * 0.13F + 1.5F) * 0.15F;
        rightEye.yRot = Mth.cos(ageInTicks * 0.12F + 1.0F) * 0.15F;

        // Trunk: swings and curls like an elephant trunk, raised up while trading
        float trading = alien.isTrading() ? 1.0F : 0.0F;
        snout.yRot = Mth.sin(ageInTicks * 0.08F) * 0.25F;
        snout.xRot = Mth.sin(ageInTicks * 0.06F) * 0.1F - trading * 0.4F;
        trunk.xRot = Mth.sin(ageInTicks * 0.08F + 1.0F) * 0.25F + 0.1F - trading * 0.3F;
        trunkTip.xRot = Mth.sin(ageInTicks * 0.08F + 2.0F) * 0.35F + 0.15F - trading * 0.4F;
    }
}
