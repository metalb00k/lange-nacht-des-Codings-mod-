package net.example.per_aspera_ad_astra.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.Level;

/**
 * Ice bee ("Eisbiene").
 *
 * It extends the vanilla Bee and uses the vanilla bee model (only the texture is new:
 * textures/entity/ice_bee.png). So it flies around, stings when angry, pollinates and makes honey.
 *
 *  - Lives in Ice Beehives (blue beehive, block/ModBeehives) - vanilla beehives work too
 *  - Pollinates Eisblatt (ice leaves) because Astra has no flowers
 *    (data/minecraft/tags/blocks/flowers.json), and is bred / lured with Eisblatt too
 *  - Babies are ice bees
 *  - Spawns in the Eiswald and around the beehives on alien village plazas
 */
public class IceBeeEntity extends Bee {

    public IceBeeEntity(EntityType<? extends Bee> type, Level level) {
        super(type, level);
    }

    /** Same stats as a normal bee. */
    public static AttributeSupplier.Builder createAttributes() {
        return Bee.createAttributes();
    }

    /** Babies are ice bees (the vanilla bee would make a normal bee). */
    @Override
    public Bee getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return ModEntities.ICE_BEE.get().create(level);
    }
}
