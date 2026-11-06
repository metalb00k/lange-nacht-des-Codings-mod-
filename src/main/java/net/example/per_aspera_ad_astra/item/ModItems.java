package net.example.per_aspera_ad_astra.item;

import net.example.per_aspera_ad_astra.Per_aspera_ad_astra;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * All custom items of the mod live here.
 *
 * To add a new item:
 *  1. Add a RegistryObject line below (name must be lowercase, e.g. "moon_rock").
 *  2. Add its display name in   resources/assets/per_aspera_ad_astra/lang/en_us.json
 *  3. Add a model in            resources/assets/per_aspera_ad_astra/models/item/<name>.json
 *  4. Add a 16x16 texture in    resources/assets/per_aspera_ad_astra/textures/item/<name>.png
 *  5. (Optional) put it in a creative tab in Per_aspera_ad_astra#addCreative
 */
public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Per_aspera_ad_astra.MOD_ID);

    // ---- Items ----

    public static final RegistryObject<Item> STAR_FRAGMENT = ITEMS.register("star_fragment",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CRYOORE = ITEMS.register("Cryo-Titan Erz",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> CRYOORE = ITEMS.register("Cryo-Titan Erz",
            () -> new Item(new Item.Properties()));

    // ---- End of items ----

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
