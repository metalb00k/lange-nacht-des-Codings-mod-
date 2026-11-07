package net.example.per_aspera_ad_astra.entity.client;

import net.example.per_aspera_ad_astra.Per_aspera_ad_astra;
import net.example.per_aspera_ad_astra.entity.GreatFrostwormEntity;
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
 * The Great Frostworm, made in Blockbench ("the great frostworm.java").
 *
 * The Blockbench export had all cubes in one group (bb_main), which can't be animated.
 * Here the same cubes (same sizes + texture offsets, so it looks identical) are split into a skeleton:
 *
 *   body (middle segment)
 *    ├─ neck (front segment)
 *    │   └─ head
 *    │       ├─ jaw              (opens when biting / casting)
 *    │       ├─ mandible_left    (pincers, chatter + pinch)
 *    │       └─ mandible_right
 *    ├─ back (curled rear segment)
 *    └─ tail_base (pivot where the tail meets the back -> sways)
 *        ├─ tail
 *        └─ tail_tip
 *
 * The whole model is shifted 2.5 px to the side so the head/body line sits in the middle of the hitbox.
 * Animations are all in setupAnim (code, no keyframes). Rendered 2.5x bigger (see GreatFrostwormRenderer).
 */
public class GreatFrostwormModel extends HierarchicalModel<GreatFrostwormEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            new ResourceLocation(Per_aspera_ad_astra.MOD_ID, "great_frostworm"), "main");

    private final ModelPart root;
    private final ModelPart body, neck, head, jaw, mandibleLeft, mandibleRight, tailBase;

    public GreatFrostwormModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.neck = body.getChild("neck");
        this.head = neck.getChild("head");
        this.jaw = head.getChild("jaw");
        this.mandibleLeft = head.getChild("mandible_left");
        this.mandibleRight = head.getChild("mandible_right");
        this.tailBase = body.getChild("tail_base");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        CubeDeformation none = new CubeDeformation(0.0F);

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(48, 44).addBox(-4.5F, -2.5F, 0.0F, 9.0F, 5.0F, 11.0F, none),
                PartPose.offset(0.0F, 5.5F, 5.0F));

        PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create()
                        .texOffs(0, 49).addBox(-3.5F, -2.5F, -11.0F, 7.0F, 5.0F, 11.0F, none),
                PartPose.offset(0.0F, 1.0F, 0.0F));

        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(48, 25).addBox(-4.5F, -3.5F, -12.0F, 9.0F, 7.0F, 12.0F, none),
                PartPose.offset(0.0F, 0.0F, -11.0F));

        head.addOrReplaceChild("jaw", CubeListBuilder.create()
                        .texOffs(64, 60).addBox(-3.5F, -1.0F, -6.0F, 7.0F, 2.0F, 6.0F, none),
                PartPose.offset(0.0F, 3.5F, -11.0F));

        head.addOrReplaceChild("mandible_left", CubeListBuilder.create()
                        .texOffs(36, 60).addBox(-2.4746F, -0.6488F, -6.0F, 7.0F, 2.0F, 7.0F, none),
                PartPose.offsetAndRotation(-3.5F, -0.5F, -12.0F, 0.0F, 0.0F, -0.829F));

        head.addOrReplaceChild("mandible_right", CubeListBuilder.create()
                        .texOffs(54, 15).addBox(-4.413F, -0.8375F, -6.0F, 7.0F, 2.0F, 7.0F, none),
                PartPose.offsetAndRotation(3.5F, -0.5F, -12.0F, 0.0F, 0.0F, 0.9163F));

        body.addOrReplaceChild("back", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, 0.0359F, -19.4545F, 7.0F, 5.0F, 20.0F, none),
                PartPose.offsetAndRotation(16.5F, 7.5F, 14.0F, 0.0F, 1.5708F, 0.5236F));

        PartDefinition tailBase = body.addOrReplaceChild("tail_base", CubeListBuilder.create(),
                PartPose.offset(16.0F, 9.9F, 14.0F));

        tailBase.addOrReplaceChild("tail", CubeListBuilder.create()
                        .texOffs(0, 25).addBox(-1.0F, -2.0342F, -0.4779F, 4.0F, 4.0F, 20.0F, none),
                PartPose.offsetAndRotation(2.5F, -19.4F, 1.0F, 0.0F, 1.5708F, 1.7017F));

        tailBase.addOrReplaceChild("tail_tip", CubeListBuilder.create()
                        .texOffs(54, 0).addBox(0.5F, -27.4F, -3.0F, 6.0F, 8.0F, 7.0F, none),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(GreatFrostwormEntity worm, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        float deg = Mth.DEG_TO_RAD;
        float walk = Math.min(limbSwingAmount, 1.0F);
        float slither = Mth.sin(limbSwing * 0.5F) * walk;
        float bite = Mth.sin(this.attackTime * Mth.PI);                        // 0 -> 1 -> 0 during a bite
        float rear = worm.getRearAmount(ageInTicks - worm.tickCount);           // 0..1 while casting

        // Body: breathing bob + slithering side to side while moving
        body.y += Mth.sin(ageInTicks * 0.08F) * 0.6F;
        body.yRot += slither * 0.12F;

        // Neck swings the other way (S-shape), rears up when casting an attack
        neck.yRot += -slither * 0.35F + Mth.sin(ageInTicks * 0.05F) * 0.08F;
        neck.xRot += Mth.sin(ageInTicks * 0.08F + 1.0F) * 0.05F - rear * 0.6F;

        // Head looks at its target
        head.yRot += Mth.clamp(netHeadYaw, -60.0F, 60.0F) * deg * 0.6F;
        head.xRot += headPitch * deg * 0.5F + rear * 0.3F;

        // Jaw: slightly open, snaps open when biting, wide open when casting
        jaw.xRot += 0.15F + Mth.sin(ageInTicks * 0.1F) * 0.05F + bite * 0.7F + rear * 0.6F;

        // Mandibles: chatter all the time, pinch when biting / spread when casting
        float pinch = Mth.sin(ageInTicks * 0.25F) * 0.08F + bite * 0.35F + rear * 0.3F;
        mandibleLeft.zRot -= pinch;
        mandibleRight.zRot += pinch;

        // Tail sways slowly, whips back when casting
        tailBase.zRot += Mth.sin(ageInTicks * 0.1F) * 0.12F;
        tailBase.xRot += Mth.cos(ageInTicks * 0.07F) * 0.08F + rear * 0.15F;
    }
}
