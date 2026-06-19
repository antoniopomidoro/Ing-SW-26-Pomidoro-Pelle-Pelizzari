package it.polimi.ingsw.model.effects.contextual;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.cards.characters.*;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.effects.contextual.*;
import it.polimi.ingsw.model.effects.events.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;

import java.util.Arrays;


/**
 * End-game contextual effect that awards prestige points for each completed
 * card set the player owns.
 */
public class EndCardSet implements ContextualEffect {
    @JsonProperty("pp")
    private int pp;

    /**
     * Awards prestige points proportional to the player's completed card sets.
     *
     * @param p     the player the effect applies to
     * @param state the current game state
     * @return true
     */
    @Override
    public boolean executeEffect(Player p, GameState state) {
        p.addPP(p.getStats().calculateSet() * pp);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString(){
        return  pp +" PP FOR EACH SET";
    }
}
