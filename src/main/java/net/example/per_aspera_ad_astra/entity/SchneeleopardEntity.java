package net.example.per_aspera_ad_astra.entity;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Snow leopard ("Schneeleopard").
 *
 * It extends the vanilla Wolf, so it has the wild wolf AI:
 *  - wanders around, hunts sheep, rabbits and foxes, attacks skeletons
 *  - neutral: if you hit it, it attacks you and nearby snow leopards help it
 *  - runs away from llamas
 *
 * But it can NOT be tamed: bones do nothing (see mobInteract).
 * It also makes cat sounds instead of barking.
 */
public class SchneeleopardEntity extends Wolf {

    public SchneeleopardEntity(EntityType<? extends Wolf> type, Level level) {
        super(type, level);
    }

    /** Wolf stats, but tougher (wild wolf = 8 health, 2 damage). */
    public static AttributeSupplier.Builder createAttributes() {
        return Wolf.createAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D);
    }

    /** Taming a wolf happens when you right-click it with a bone. We simply ignore bones. */
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(Items.BONE)) {
            return InteractionResult.PASS;
        }
        return super.mobInteract(player, hand);
    }

    // ---- Sounds: cat sounds instead of wolf barking ----

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isAngry() ? SoundEvents.CAT_HISS : SoundEvents.CAT_STRAY_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.CAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.CAT_DEATH;
    }
}
