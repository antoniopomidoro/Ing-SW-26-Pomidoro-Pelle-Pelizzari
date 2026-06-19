package it.polimi.ingsw.model.effects.contextual;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.model.effects.ContextualEffect;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;


/**
 * Contextual effect that grants the player extra food whenever they place a
 * totem, applied on acquisition.
 */
public class TotemPlacementBonus implements ContextualEffect {
    @JsonProperty("bonus")
    private int bonus;

    /**
     * Registers the totem placement food bonus on the player's stats.
     *
     * @param p the player receiving the card
     * @return true
     */
    @Override
    public boolean onAddedToPlayer(Player p) {
        p.getStats().setTotemPlacementBonusFood(bonus);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public String toString(){
        return "+" + bonus + " FOOD FROM TOTEM";
    }
}