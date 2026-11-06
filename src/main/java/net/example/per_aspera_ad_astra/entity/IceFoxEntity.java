package net.example.per_aspera_ad_astra.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.level.Level;

/**
 * Ice fox ("Eisfuchs").
 *
 * It extends the vanilla Fox, so it has the normal fox AI:
 *  - runs away from players (unless it trusts them), sleeps during the day
 *  - hunts chickens, rabbits and fish, pounces on its prey
 *  - picks up items and carries them in its mouth
 *  - breed two ice foxes with sweet berries -> baby ice fox that trusts you
 *
 * No spawn egg and no natural spawning: use /summon per_aspera_ad_astra:ice_fox
 */
public class IceFoxEntity extends Fox {

    public IceFoxEntity(EntityType<? extends Fox> type, Level level) {
        super(type, level);
    }

    /** Same stats as a normal fox. */
    public static AttributeSupplier.Builder createAttributes() {
        return Fox.createAttributes();
    }

    /** Babies are ice foxes (the vanilla fox would make a normal fox). */
    @Override
    public Fox getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return ModEntities.ICE_FOX.get().create(level);
    }
}
