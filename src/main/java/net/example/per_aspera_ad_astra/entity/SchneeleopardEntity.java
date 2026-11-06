package net.example.per_aspera_ad_astra.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.level.Level;

/**
 * Snow leopard ("Schneeleopard").
 *
 * It extends the vanilla Cat, so it behaves exactly like a cat:
 *  - tame it with raw cod / raw salmon
 *  - right-click when tamed: sit / stand up
 *  - follows its owner, sleeps on the owner's bed, sits on chests/beds/furnaces
 *  - creepers and phantoms are scared of it
 *  - breed two tamed snow leopards with fish -> baby snow leopard
 */
public class SchneeleopardEntity extends Cat {

    public SchneeleopardEntity(EntityType<? extends Cat> type, Level level) {
        super(type, level);
    }

    /** Cat stats, but a bit tougher (snow leopards are bigger than cats). */
    public static AttributeSupplier.Builder createAttributes() {
        return Cat.createAttributes()
                .add(Attributes.MAX_HEALTH, 16.0D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D);
    }

    /** Only breed with other snow leopards (not with normal cats). */
    @Override
    public boolean canMate(Animal other) {
        return other instanceof SchneeleopardEntity && super.canMate(other);
    }

    /** Babies are snow leopards and belong to the same owner. */
    @Override
    public SchneeleopardEntity getBreedOffspring(ServerLevel level, AgeableMob partner) {
        SchneeleopardEntity baby = ModEntities.SCHNEELEOPARD.get().create(level);
        if (baby != null && this.isTame()) {
            baby.setOwnerUUID(this.getOwnerUUID());
            baby.setTame(true);
        }
        return baby;
    }
}
