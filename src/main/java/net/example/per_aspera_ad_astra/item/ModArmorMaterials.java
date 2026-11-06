package net.example.per_aspera_ad_astra.item;

import net.example.per_aspera_ad_astra.Per_aspera_ad_astra;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.Supplier;

/**
 * Armor materials of the mod.
 *
 * For comparison, Netherite is: durability multiplier 37, protection (boots/legs/chest/helmet) 3/6/8/3,
 * toughness 3.0, knockback resistance 0.1, enchantability 15.
 *
 * The armor texture (what it looks like when worn) is loaded from
 *   assets/per_aspera_ad_astra/textures/models/armor/<name>_layer_1.png  (helmet, chestplate, boots)
 *   assets/per_aspera_ad_astra/textures/models/armor/<name>_layer_2.png  (leggings)
 */
public enum ModArmorMaterials implements ArmorMaterial {
    CRYO_TITAN("cryo_titan",
            42,                         // durability multiplier
            new int[]{ 4, 7, 9, 4 },    // protection: boots, leggings, chestplate, helmet
            18,                         // enchantability
            SoundEvents.ARMOR_EQUIP_NETHERITE,
            3.5f,                       // toughness
            0.15f,                      // knockback resistance (0.15 = 15% per piece)
            () -> Ingredient.of(ModItems.CRYOTITAN_INGOT.get())),

    // Spacesuit: protection like iron armor (iron = 2/5/6/2), a bit less durable. Repair with iron ingots.
    SPACESUIT("spacesuit",
            13,                         // durability multiplier (iron = 15)
            new int[]{ 2, 5, 6, 2 },    // protection: boots, leggings, chestplate, helmet
            9,                          // enchantability (iron = 9)
            SoundEvents.ARMOR_EQUIP_IRON,
            0.0f,                       // toughness
            0.0f,                       // knockback resistance
            () -> Ingredient.of(Items.IRON_INGOT));

    // Base durability per piece, same as vanilla: boots, leggings, chestplate, helmet
    private static final int[] BASE_DURABILITY = { 13, 15, 16, 11 };

    private final String name;
    private final int durabilityMultiplier;
    private final int[] protectionAmounts;
    private final int enchantmentValue;
    private final SoundEvent equipSound;
    private final float toughness;
    private final float knockbackResistance;
    private final Supplier<Ingredient> repairIngredient;

    ModArmorMaterials(String name, int durabilityMultiplier, int[] protectionAmounts, int enchantmentValue,
                      SoundEvent equipSound, float toughness, float knockbackResistance,
                      Supplier<Ingredient> repairIngredient) {
        this.name = name;
        this.durabilityMultiplier = durabilityMultiplier;
        this.protectionAmounts = protectionAmounts;
        this.enchantmentValue = enchantmentValue;
        this.equipSound = equipSound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repairIngredient = repairIngredient;
    }

    private static int index(ArmorItem.Type type) {
        return switch (type) {
            case BOOTS -> 0;
            case LEGGINGS -> 1;
            case CHESTPLATE -> 2;
            case HELMET -> 3;
        };
    }

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return BASE_DURABILITY[index(type)] * this.durabilityMultiplier;
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return this.protectionAmounts[index(type)];
    }

    @Override
    public int getEnchantmentValue() {
        return this.enchantmentValue;
    }

    @Override
    public SoundEvent getEquipSound() {
        return this.equipSound;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }

    @Override
    public String getName() {
        // "modid:name" so the armor texture is looked up in our mod's folder
        return Per_aspera_ad_astra.MOD_ID + ":" + this.name;
    }

    @Override
    public float getToughness() {
        return this.toughness;
    }

    @Override
    public float getKnockbackResistance() {
        return this.knockbackResistance;
    }
}
