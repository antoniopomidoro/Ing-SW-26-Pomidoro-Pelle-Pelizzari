package it.polimi.ingsw.model.effects.contextual;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;

/**
 * Contextual effect that reduces the penalty a player suffers when losing a
 * shamanic ritual, by setting the player's ritual loss multiplier.
 */
public class ShamanicLossProtection implements ContextualEffect {
    @JsonProperty("protect")
    private int protect;

    /**
     * Sets the player's ritual loss multiplier on acquisition.
     *
     * @param p the player receiving the card
     * @return true
     */
    @Override
    public boolean onAddedToPlayer(Player p) {

        p.getStats().setRitualLossMultiplier(protect);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public String toString(){
        return "SHAMANIC LOSS PROTECTION";
    }
}
