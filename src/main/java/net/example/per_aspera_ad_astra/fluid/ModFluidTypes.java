package net.example.per_aspera_ad_astra.fluid;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import net.example.per_aspera_ad_astra.Per_aspera_ad_astra;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.joml.Vector3f;

import java.util.function.Consumer;

/**
 * Fluid types = how a fluid behaves (swimming, drowning, sounds) and how it looks.
 * Eiswasser uses the vanilla water textures, just tinted green.
 */
public class ModFluidTypes {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, Per_aspera_ad_astra.MOD_ID);

    // Colour of the water (ARGB: alpha, red, green, blue). Change this to change the green.
    public static final int EISWASSER_COLOR = 0xCC2FD158;

    public static final RegistryObject<FluidType> EISWASSER_TYPE = FLUID_TYPES.register("eiswasser",
            () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("block.per_aspera_ad_astra.eiswasser")
                    .canSwim(true)
                    .canDrown(true)
                    .canExtinguish(true)
                    .canHydrate(true)
                    .supportsBoating(true)
                    .fallDistanceModifier(0f)
                    .temperature(250)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)) {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        @Override
                        public ResourceLocation getStillTexture() {
                            return new ResourceLocation("block/water_still");
                        }

                        @Override
                        public ResourceLocation getFlowingTexture() {
                            return new ResourceLocation("block/water_flow");
                        }

                        @Override
                        public ResourceLocation getOverlayTexture() {
                            return new ResourceLocation("block/water_overlay");
                        }

                        @Override
                        public int getTintColor() {
                            return EISWASSER_COLOR;
                        }

                        // Green fog when your head is under the water
                        @Override
                        public Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level,
                                                       int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor) {
                            return new Vector3f(0.18f, 0.6f, 0.3f);
                        }

                        @Override
                        public void modifyFogRender(Camera camera, FogRenderer.FogMode mode, float renderDistance,
                                                    float partialTick, float nearDistance, float farDistance, FogShape shape) {
                            RenderSystem.setShaderFogStart(0.5f);
                            RenderSystem.setShaderFogEnd(12f);
                        }
                    });
                }
            });

    public static void register(IEventBus eventBus) {
        FLUID_TYPES.register(eventBus);
    }
}
