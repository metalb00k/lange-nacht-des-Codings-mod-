package net.example.per_aspera_ad_astra.worldgen.biome.surface;

import net.example.per_aspera_ad_astra.Per_aspera_ad_astra;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

public class Modbiomes {
    public static final ResourceKey<Biome> ICE_DESERT = ResourceKey.create(Registries.BIOME, new ResourceLocation(Per_aspera_ad_astra.MOD_ID, "ice_desert"));

}
