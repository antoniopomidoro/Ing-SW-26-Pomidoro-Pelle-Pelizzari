package it.polimi.ingsw.model.effects.contextual;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;

import java.util.InvalidPropertiesFormatException;

/**
 * Contextual effect that grants a fixed number of stars to the player when the
 * owning card is acquired.
 */
public class AddStars implements ContextualEffect {
    @JsonProperty("stars")
    private int stars;

    /**
     * Adds this effect's stars to the player's stats on acquisition.
     *
     * @param p the player receiving the card
     * @return true on success, false if the player is null
     */
    @Override
    public boolean onAddedToPlayer(Player p) {
        if (p == null) return false;
        p.getStats().addStars(stars);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString(){
        return "ADD " + stars +" STARS";
    }
}
