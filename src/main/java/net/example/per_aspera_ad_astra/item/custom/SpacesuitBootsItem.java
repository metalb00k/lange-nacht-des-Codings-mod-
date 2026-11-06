package net.example.per_aspera_ad_astra.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Spacesuit boots. Can get the ice-skates upgrade (boots + 3 iron ingots, see recipe/SpacesuitUpgradeRecipe):
 * faster on ice + Methaneis, and Methaneis doesn't crack under you (see event/IceSkateEvents, MethaneisBlock).
 */
public class SpacesuitBootsItem extends ArmorItem {
    /** NBT tag of the ice-skates upgrade. */
    public static final String ICE_SKATES = "ice_skates";

    public SpacesuitBootsItem(ArmorMaterial material, Properties properties) {
        super(material, Type.BOOTS, properties);
    }

    public static boolean hasSkates(ItemStack stack) {
        return stack.getItem() instanceof SpacesuitBootsItem && stack.hasTag() && stack.getTag().getBoolean(ICE_SKATES);
    }

    /** True if the entity wears spacesuit boots with ice skates. */
    public static boolean wearsSkates(LivingEntity entity) {
        return hasSkates(entity.getItemBySlot(EquipmentSlot.FEET));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return hasSkates(stack) || super.isFoil(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (hasSkates(stack)) {
            tooltip.add(Component.translatable("tooltip.per_aspera_ad_astra.upgrade_ice_skates").withStyle(ChatFormatting.AQUA));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
