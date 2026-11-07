package net.example.per_aspera_ad_astra.entity.client;

import net.example.per_aspera_ad_astra.Per_aspera_ad_astra;
import net.minecraft.client.renderer.entity.BeeRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Bee;

/** Draws the ice bee: the vanilla bee model + wing/stinger animations, only with our texture. */
public class IceBeeRenderer extends BeeRenderer {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Per_aspera_ad_astra.MOD_ID, "textures/entity/ice_bee.png");

    public IceBeeRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(Bee entity) {
        return TEXTURE;
    }
}
