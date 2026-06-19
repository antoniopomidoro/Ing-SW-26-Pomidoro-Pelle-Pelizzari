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
 * Contextual effect that grants the player extra top-row picks at end of turn,
 * applied when the owning card is acquired.
 */
public class EndOfTurnExtraPick implements ContextualEffect {
    @JsonProperty("upperPick")
    private int upperPick;

    /**
     * Registers the extra top-row picks on the player's stats on acquisition.
     *
     * @param p the player receiving the card
     * @return true
     */
    @Override
    public boolean onAddedToPlayer(Player p) {
        p.getStats().setExtraUpperPick(upperPick);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString(){
        return "EXTRA UPPER PICK";
    }
}
