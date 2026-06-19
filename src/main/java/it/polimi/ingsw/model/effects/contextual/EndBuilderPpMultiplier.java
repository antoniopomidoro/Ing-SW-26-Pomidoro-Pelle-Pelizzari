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


/**
 * End-game contextual effect that multiplies the player's accumulated builder
 * prestige points by a fixed factor.
 */
public class EndBuilderPpMultiplier implements ContextualEffect {
    @JsonProperty("mult")
    private int mult;

    /**
     * Multiplies the player's builder prestige points by this effect's factor.
     *
     * @param p     the player the effect applies to
     * @param state the current game state
     * @return true
     */
    @Override
    public boolean executeEffect(Player p, GameState state) {
        p.getStats().setBuilderPp(mult * p.getStats().getBuilderPp());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString(){
        return "MULT BUILDER PP BY " + mult;
    }
}
