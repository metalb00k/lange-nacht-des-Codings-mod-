package net.example.per_aspera_ad_astra.item;

import net.example.per_aspera_ad_astra.Per_aspera_ad_astra;
import net.example.per_aspera_ad_astra.item.custom.OxygenCanisterItem;
import net.example.per_aspera_ad_astra.item.custom.SpacesuitChestplateItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Items;
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
    public static final RegistryObject<Item> CRYOORE = ITEMS.register("cryo_titan_erz",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CRYOTITAN_INGOT = ITEMS.register("cryo_titan_ingot",
            () -> new Item(new Item.Properties()));

    // ---- Cryo-Titan weapons & tools (stats: see ModToolTiers) ----
    // Numbers = extra damage and attack speed, same as the vanilla Netherite versions.
    public static final RegistryObject<Item> CRYO_TITAN_SWORD = ITEMS.register("cryo_titan_sword",
            () -> new SwordItem(ModToolTiers.CRYO_TITAN, 3, -2.4f, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> CRYO_TITAN_PICKAXE = ITEMS.register("cryo_titan_pickaxe",
            () -> new PickaxeItem(ModToolTiers.CRYO_TITAN, 1, -2.8f, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> CRYO_TITAN_AXE = ITEMS.register("cryo_titan_axe",
            () -> new AxeItem(ModToolTiers.CRYO_TITAN, 5.0f, -3.0f, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> CRYO_TITAN_SHOVEL = ITEMS.register("cryo_titan_shovel",
            () -> new ShovelItem(ModToolTiers.CRYO_TITAN, 1.5f, -3.0f, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> CRYO_TITAN_HOE = ITEMS.register("cryo_titan_hoe",
            () -> new HoeItem(ModToolTiers.CRYO_TITAN, -4, 0.0f, new Item.Properties().fireResistant()));

    // ---- Cryo-Titan armor (stats: see ModArmorMaterials) ----
    public static final RegistryObject<Item> CRYO_TITAN_HELMET = ITEMS.register("cryo_titan_helmet",
            () -> new ArmorItem(ModArmorMaterials.CRYO_TITAN, ArmorItem.Type.HELMET, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> CRYO_TITAN_CHESTPLATE = ITEMS.register("cryo_titan_chestplate",
            () -> new ArmorItem(ModArmorMaterials.CRYO_TITAN, ArmorItem.Type.CHESTPLATE, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> CRYO_TITAN_LEGGINGS = ITEMS.register("cryo_titan_leggings",
            () -> new ArmorItem(ModArmorMaterials.CRYO_TITAN, ArmorItem.Type.LEGGINGS, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> CRYO_TITAN_BOOTS = ITEMS.register("cryo_titan_boots",
            () -> new ArmorItem(ModArmorMaterials.CRYO_TITAN, ArmorItem.Type.BOOTS, new Item.Properties().fireResistant()));

    // ---- Spacesuit (needed to breathe in Astra, see event/OxygenEvents) ----
    public static final RegistryObject<Item> SPACESUIT_HELMET = ITEMS.register("spacesuit_helmet",
            () -> new ArmorItem(ModArmorMaterials.SPACESUIT, ArmorItem.Type.HELMET, new Item.Properties()));
    // The chestplate is the oxygen tank
    public static final RegistryObject<Item> SPACESUIT_CHESTPLATE = ITEMS.register("spacesuit_chestplate",
            () -> new SpacesuitChestplateItem(ModArmorMaterials.SPACESUIT, new Item.Properties()));
    public static final RegistryObject<Item> SPACESUIT_LEGGINGS = ITEMS.register("spacesuit_leggings",
            () -> new ArmorItem(ModArmorMaterials.SPACESUIT, ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<Item> SPACESUIT_BOOTS = ITEMS.register("spacesuit_boots",
            () -> new ArmorItem(ModArmorMaterials.SPACESUIT, ArmorItem.Type.BOOTS, new Item.Properties()));
    // Right-click to refill the spacesuit's oxygen tank
    public static final RegistryObject<Item> OXYGEN_CANISTER = ITEMS.register("oxygen_canister",
            () -> new OxygenCanisterItem(new Item.Properties().stacksTo(16)));

    // ---- End of items ----

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
