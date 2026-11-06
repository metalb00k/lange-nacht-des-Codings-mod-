package net.example.per_aspera_ad_astra.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Right-click while wearing the spacesuit chestplate to refill its oxygen tank.
 */
public class OxygenCanisterItem extends Item {
    /** How much oxygen one canister adds: 3000 ticks = 2.5 minutes (half a tank). */
    public static final int OXYGEN_PER_CANISTER = 3000;

    public OxygenCanisterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack canister = player.getItemInHand(hand);
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);

        if (!(chest.getItem() instanceof SpacesuitChestplateItem)) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("message.per_aspera_ad_astra.no_tank")
                        .withStyle(ChatFormatting.RED), true);
            }
            return InteractionResultHolder.fail(canister);
        }

        int oxygen = SpacesuitChestplateItem.getOxygen(chest);
        if (oxygen >= SpacesuitChestplateItem.MAX_OXYGEN) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("message.per_aspera_ad_astra.tank_full")
                        .withStyle(ChatFormatting.YELLOW), true);
            }
            return InteractionResultHolder.fail(canister);
        }

        if (!level.isClientSide) {
            SpacesuitChestplateItem.setOxygen(chest, oxygen + OXYGEN_PER_CANISTER);
            if (!player.getAbilities().instabuild) {
                canister.shrink(1);
            }
            player.displayClientMessage(Component.translatable("message.per_aspera_ad_astra.refilled",
                    SpacesuitChestplateItem.getOxygenPercent(chest)).withStyle(ChatFormatting.AQUA), true);
        }
        level.playSound(player, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.5f, 1.6f);
        player.getCooldowns().addCooldown(this, 10);
        return InteractionResultHolder.sidedSuccess(canister, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.per_aspera_ad_astra.canister").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
