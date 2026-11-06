package net.example.per_aspera_ad_astra.item;

import net.example.per_aspera_ad_astra.Per_aspera_ad_astra;
import net.example.per_aspera_ad_astra.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * The mod's own creative tab. Add new items/blocks inside displayItems with pOutput.accept(...).
 * The tab's display name is set in resources/assets/per_aspera_ad_astra/lang/en_us.json ("creativetab.per_aspera_tab").
 */
public class ModCreativeModTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Per_aspera_ad_astra.MOD_ID);

    public static final RegistryObject<CreativeModeTab> PER_ASPERA_TAB = CREATIVE_MODE_TABS.register("per_aspera_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.STAR_FRAGMENT.get()))
                    .title(Component.translatable("creativetab.per_aspera_tab"))
                    .displayItems((pParameters, pOutput) -> {
                        // Items
                        pOutput.accept(ModItems.STAR_FRAGMENT.get());
                        pOutput.accept(Items.DIAMOND);
                        pOutput.accept(ModItems.CRYOORE.get());
                        pOutput.accept(ModItems.CRYOTITAN_INGOT.get());
                        pOutput.accept(ModItems.EISWASSER_EIMER.get());
                        pOutput.accept(ModItems.FLAMING_HEART.get());
                        pOutput.accept(ModItems.HEATER.get());
                        // Cryo-Titan weapons, tools & armor
                        pOutput.accept(ModItems.CRYO_TITAN_SWORD.get());
                        pOutput.accept(ModItems.CRYO_TITAN_PICKAXE.get());
                        pOutput.accept(ModItems.CRYO_TITAN_AXE.get());
                        pOutput.accept(ModItems.CRYO_TITAN_SHOVEL.get());
                        pOutput.accept(ModItems.CRYO_TITAN_HOE.get());
                        pOutput.accept(ModItems.CRYO_TITAN_HELMET.get());
                        pOutput.accept(ModItems.CRYO_TITAN_CHESTPLATE.get());
                        pOutput.accept(ModItems.CRYO_TITAN_LEGGINGS.get());
                        pOutput.accept(ModItems.CRYO_TITAN_BOOTS.get());
                        // Spacesuit + oxygen (the creative-tab chestplate comes with a full tank)
                        pOutput.accept(ModItems.SPACESUIT_HELMET.get());
                        ItemStack fullTank = new ItemStack(ModItems.SPACESUIT_CHESTPLATE.get());
                        net.example.per_aspera_ad_astra.item.custom.SpacesuitChestplateItem.setOxygen(fullTank,
                                net.example.per_aspera_ad_astra.item.custom.SpacesuitChestplateItem.MAX_OXYGEN);
                        pOutput.accept(fullTank);
                        pOutput.accept(ModItems.SPACESUIT_LEGGINGS.get());
                        pOutput.accept(ModItems.SPACESUIT_BOOTS.get());
                        pOutput.accept(ModItems.OXYGEN_CANISTER.get());
                        // Mobs
                        pOutput.accept(ModItems.SCHNEELEOPARD_SPAWN_EGG.get());
                        // Block
                        pOutput.accept(ModBlocks.MOD_PORTAL.get());
                        pOutput.accept(ModBlocks.EISHOLZ.get());
                        pOutput.accept(ModBlocks.EISBLAETTER.get());
                        pOutput.accept(ModBlocks.EISZAPFEN.get());
                        pOutput.accept(ModBlocks.CRYOTITAN.get());
                        pOutput.accept(ModBlocks.FROSTKRISTALL.get());
                        pOutput.accept(ModBlocks.EISERDE.get());
                        pOutput.accept(ModBlocks.EISHOLZBRETT.get());
                        pOutput.accept(ModBlocks.ERZ_BLOCK.get());
                        pOutput.accept(ModBlocks.EIS_STEIN.get());
                        pOutput.accept(ModBlocks.EISZIEGELSTEIN.get());
                        pOutput.accept(ModBlocks.EISZIEGELSTEINSTUFEN.get());
                        pOutput.accept(ModBlocks.EISBLOCK.get());

                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
