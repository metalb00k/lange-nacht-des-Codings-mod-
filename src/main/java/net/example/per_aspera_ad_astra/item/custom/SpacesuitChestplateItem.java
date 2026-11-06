package net.example.per_aspera_ad_astra.item.custom;

import net.example.per_aspera_ad_astra.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Spacesuit chestplate = the oxygen tank. The oxygen is stored on the item itself (NBT tag "Oxygen"),
 * so it stays when you take the chestplate off.
 *
 * Oxygen is counted in ticks (20 ticks = 1 second).
 */
public class SpacesuitChestplateItem extends ArmorItem {
    /** Full tank: 6000 ticks = 5 minutes of air. */
    public static final int MAX_OXYGEN = 6000;
    private static final String OXYGEN_TAG = "Oxygen";

    public SpacesuitChestplateItem(ArmorMaterial material, Properties properties) {
        super(material, Type.CHESTPLATE, properties);
    }

    public static int getOxygen(ItemStack stack) {
        return stack.hasTag() ? stack.getTag().getInt(OXYGEN_TAG) : 0;
    }

    public static void setOxygen(ItemStack stack, int oxygen) {
        stack.getOrCreateTag().putInt(OXYGEN_TAG, Math.max(0, Math.min(MAX_OXYGEN, oxygen)));
    }

    /** True if the player wears all 4 spacesuit pieces. */
    public static boolean hasFullSuit(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.SPACESUIT_HELMET.get())
                && player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof SpacesuitChestplateItem
                && player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.SPACESUIT_LEGGINGS.get())
                && player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.SPACESUIT_BOOTS.get());
    }

    /** Oxygen in percent (0-100), for display. */
    public static int getOxygenPercent(ItemStack stack) {
        return getOxygen(stack) * 100 / MAX_OXYGEN;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int seconds = getOxygen(stack) / 20;
        tooltip.add(Component.translatable("tooltip.per_aspera_ad_astra.oxygen",
                        getOxygenPercent(stack), seconds / 60, String.format("%02d", seconds % 60))
                .withStyle(ChatFormatting.AQUA));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
