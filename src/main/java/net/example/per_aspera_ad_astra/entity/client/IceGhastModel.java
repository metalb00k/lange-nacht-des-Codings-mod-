package net.example.per_aspera_ad_astra.entity.client;

import net.example.per_aspera_ad_astra.Per_aspera_ad_astra;
import net.example.per_aspera_ad_astra.entity.IceGhastEntity;
import net.minecraft.client.model.GhastModel;
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
 * Ice ghast model, exported from Blockbench (iceghast.java).
 *
 * It extends the vanilla GhastModel, so the tentacles wave like a ghast's.
 * Changes to the export:
 *  - tentacle1..tentacle9 renamed to tentacle0..tentacle8 (vanilla names)
 *  - everything moved down by 13.6 pixels, to the same height as the vanilla ghast
 *    (otherwise it floats above its hitbox once it's scaled up 4.5x)
 */
public class IceGhastModel extends GhastModel<IceGhastEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            new ResourceLocation(Per_aspera_ad_astra.MOD_ID, "ice_ghast"), "main");

    /** How far everything was moved down compared to the Blockbench export. */
    private static final float Y_SHIFT = 13.6F;

    public IceGhastModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition root = meshdefinition.getRoot();

        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 4.0F + Y_SHIFT, 0.0F));

        tentacle(root, 0, -1.0F, 8.0F, -3.7F, -5.0F);
        tentacle(root, 1, 4.0F, 9.0F, 1.3F, -5.0F);
        tentacle(root, 2, -6.0F, 13.0F, 6.3F, -5.0F);
        tentacle(root, 3, -1.0F, 11.0F, -6.3F, 0.0F);
        tentacle(root, 4, -1.0F, 11.0F, -1.3F, 0.0F);
        tentacle(root, 5, -1.0F, 10.0F, 3.7F, 0.0F);
        tentacle(root, 6, -1.0F, 12.0F, -3.7F, 5.0F);
        tentacle(root, 7, 4.0F, 12.0F, 1.3F, 5.0F);
        tentacle(root, 8, -6.0F, 9.0F, 6.3F, 5.0F);

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    /** One tentacle: boxX = x start of the cube, length = how long it is, x/z = where it hangs. */
    private static void tentacle(PartDefinition root, int index, float boxX, float length, float x, float z) {
        root.addOrReplaceChild("tentacle" + index, CubeListBuilder.create()
                .texOffs(0, 0).addBox(boxX, 0.0F, -1.0F, 2.0F, length, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(x, 11.0F + Y_SHIFT, z));
    }
}
