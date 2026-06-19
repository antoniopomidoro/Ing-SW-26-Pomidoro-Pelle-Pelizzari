package it.polimi.ingsw.model.effects.contextual;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;

/**
 * Contextual effect that increases the reward a player gains when winning a
 * shamanic ritual, by setting the player's ritual win boost.
 */
public class ShamanicWinBoost implements ContextualEffect {
    @JsonProperty("bonus")
    private int bonus;

    /**
     * Sets the player's ritual win boost on acquisition.
     *
     * @param p the player receiving the card
     * @return true
     */
    @Override
    public boolean onAddedToPlayer(Player p) {

        p.getStats().setRitualWinBoost(bonus);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public String toString(){
        return "SHAMANIC EVENT WIN X " + bonus;
    }
}
