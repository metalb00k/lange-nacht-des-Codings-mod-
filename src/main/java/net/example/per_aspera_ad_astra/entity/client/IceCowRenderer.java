package net.example.per_aspera_ad_astra.entity.client;

import net.example.per_aspera_ad_astra.Per_aspera_ad_astra;
import net.minecraft.client.renderer.entity.CowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Cow;

/** Draws the ice cow: the vanilla cow model + animations, only with our texture. */
public class IceCowRenderer extends CowRenderer {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Per_aspera_ad_astra.MOD_ID, "textures/entity/ice_cow.png");

    public IceCowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(Cow entity) {
        return TEXTURE;
    }
}
