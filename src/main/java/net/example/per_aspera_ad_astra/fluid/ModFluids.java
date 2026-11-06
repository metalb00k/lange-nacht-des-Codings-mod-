package net.example.per_aspera_ad_astra.fluid;

import net.example.per_aspera_ad_astra.Per_aspera_ad_astra;
import net.example.per_aspera_ad_astra.block.ModBlocks;
import net.example.per_aspera_ad_astra.item.ModItems;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Every fluid needs two versions: the "source" (full block, what the bucket places)
 * and the "flowing" one (the water that spreads out).
 */
public class ModFluids {
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(ForgeRegistries.FLUIDS, Per_aspera_ad_astra.MOD_ID);

    public static final RegistryObject<FlowingFluid> SOURCE_EISWASSER = FLUIDS.register("eiswasser",
            () -> new ForgeFlowingFluid.Source(ModFluids.EISWASSER_PROPERTIES));
    public static final RegistryObject<FlowingFluid> FLOWING_EISWASSER = FLUIDS.register("flowing_eiswasser",
            () -> new ForgeFlowingFluid.Flowing(ModFluids.EISWASSER_PROPERTIES));

    public static final ForgeFlowingFluid.Properties EISWASSER_PROPERTIES = new ForgeFlowingFluid.Properties(
            ModFluidTypes.EISWASSER_TYPE, SOURCE_EISWASSER, FLOWING_EISWASSER)
            .slopeFindDistance(4)
            .levelDecreasePerBlock(1)
            .block(() -> ModBlocks.EISWASSER_BLOCK.get())
            .bucket(() -> ModItems.EISWASSER_EIMER.get());

    public static void register(IEventBus eventBus) {
        FLUIDS.register(eventBus);
    }
}
