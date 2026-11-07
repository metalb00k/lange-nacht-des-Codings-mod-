package net.example.per_aspera_ad_astra.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Jetpack: does nothing on its own. Its only use is the upgrade recipe
 * (spacesuit chestplate + jetpack -> chestplate with the "jetpack" upgrade, keeps the oxygen).
 * The flying itself: client/ModKeyBindings (thrust) + network/JetpackPacket (server side).
 */
public class JetpackItem extends Item {
    public JetpackItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.per_aspera_ad_astra.jetpack_item").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
