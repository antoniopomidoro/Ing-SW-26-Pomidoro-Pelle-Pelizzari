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


public class SustainmentBoost implements ContextualEffect {
    @JsonProperty("type")
    private CharacterEnum type;
    @JsonProperty("gain")
    private int gain;

    // Setters for testing purposes
    public void setType(CharacterEnum type) {
        this.type = type;
    }

    public void setGain(int gain) {
        this.gain = gain;
    }

    protected CharacterEnum getType() {
        return type;
    }

    protected int getGain() {
        return gain;
    }

    @Override
    public boolean onAddedToPlayer(Player p) {

        p.getStats().addSustainmentBoost(type, gain);
        return true;
    }
    public String toString(){
        return "GAIN " + gain + "DISCOUNT FOR EVERY" + type;
    }
}
