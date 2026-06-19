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
 * Contextual effect that grants the player a sustainment discount scaled by the
 * number of cards of a given character type, applied on acquisition.
 */
public class SustainmentBoost implements ContextualEffect {
    @JsonProperty("type")
    private CharacterEnum type;
    @JsonProperty("gain")
    private int gain;

    /**
     * Sets the target character type (for testing).
     *
     * @param type the character type the boost is keyed on
     */
    public void setType(CharacterEnum type) {
        this.type = type;
    }

    /**
     * Sets the per-character gain (for testing).
     *
     * @param gain the sustainment discount granted per matching character
     */
    public void setGain(int gain) {
        this.gain = gain;
    }

    /**
     * Returns the target character type (for testing).
     *
     * @return the character type the boost is keyed on
     */
    protected CharacterEnum getType() {
        return type;
    }

    /**
     * Returns the per-character gain (for testing).
     *
     * @return the sustainment discount granted per matching character
     */
    protected int getGain() {
        return gain;
    }

    /**
     * Registers the sustainment boost on the player's stats on acquisition.
     *
     * @param p the player receiving the card
     * @return true
     */
    @Override
    public boolean onAddedToPlayer(Player p) {

        p.getStats().addSustainmentBoost(type, gain);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public String toString(){
        return "GAIN " + gain + "DISCOUNT FOR EVERY" + type;
    }
}
