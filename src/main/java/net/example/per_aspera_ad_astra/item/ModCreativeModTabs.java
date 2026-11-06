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

                        // Block
                        pOutput.accept(ModBlocks.MOD_PORTAL.get());
                        pOutput.accept(ModBlocks.EISHOLZ.get());
                        pOutput.accept(ModBlocks.EISBLAETTER.get());
                        pOutput.accept(ModBlocks.EISZAPFEN.get());
                        pOutput.accept(ModBlocks.CRYOTITAN.get());
                        pOutput.accept(ModBlocks.FROSTKRISTALL.get());
                        pOutput.accept(ModBlocks.EISERDE.get());

                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
