package it.polimi.ingsw.model.effects.contextual;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.cards.characters.*;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.effects.events.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;

/**
 * Contextual effect that grants food whenever the player forms a new pair of
 * matching inventor tools. It tracks the previous pair count and rewards food
 * for each additional pair completed since acquisition.
 */
public class InventorPair implements ContextualEffect {
    @JsonProperty("bonus")
    private int bonus;
    private int oldPairs;

    /**
     * Returns the recorded baseline pair count (for testing).
     *
     * @return the previously recorded inventor pair count
     */
    protected int getOldPairs() {
        return oldPairs;
    }

    /**
     * Returns the per-pair food bonus (for testing).
     *
     * @return the food bonus granted per new pair
     */
    protected int getBonus() {
        return bonus;
    }

    /**
     * Records the player's current inventor pair count as the baseline.
     *
     * @param p the player receiving the card
     * @return true
     */
    @Override
    public boolean onAddedToPlayer(Player p) {
        this.oldPairs = p.getStats().getEqualInventorPair();
        return true;
    }

    /**
     * Grants food for each new inventor pair formed since the recorded baseline
     * and updates the baseline.
     *
     * @param p     the player the effect applies to
     * @param state the current game state
     * @return true if at least one new pair was rewarded, false otherwise
     */
    @Override
    public boolean executeEffect(Player p, GameState state) {
        int newPairs = p.getStats().getEqualInventorPair();
        if (newPairs > oldPairs) {
            p.addFood((newPairs - oldPairs) * bonus);
            oldPairs = newPairs;
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public String toString(){
        return  bonus + " FOOD FOR EACH NEW INVENTOR PAIR";
    }
}