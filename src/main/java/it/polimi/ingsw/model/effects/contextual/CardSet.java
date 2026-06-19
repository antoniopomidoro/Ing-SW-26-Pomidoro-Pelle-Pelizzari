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
 * Contextual effect that grants food whenever the player completes a new card
 * set. It records the player's set count on acquisition and, on execution,
 * rewards food for each additional set completed since then.
 */
public class CardSet implements ContextualEffect {
    @JsonProperty("food")
    private int food;
    private int baseSet;

    /**
     * Records the player's current set count as the baseline on acquisition.
     *
     * @param p the player receiving the card
     * @return true
     */
    @Override
    public boolean onAddedToPlayer(Player p) {
        baseSet = p.getStats().calculateSet();
        return true;
    }

    /**
     * Grants food for each new set completed since the recorded baseline and
     * updates the baseline.
     *
     * @param p     the player the effect applies to
     * @param state the current game state
     * @return true if at least one new set was rewarded, false otherwise
     */
    @Override
    public boolean executeEffect(Player p, GameState state) {
        int newSet = p.getStats().calculateSet();
        if (newSet > baseSet) {
            p.addFood((newSet - baseSet) * food);
            baseSet = newSet;
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString(){
        return "FOOD EVERY 6 CARDS SET";
    }
}
