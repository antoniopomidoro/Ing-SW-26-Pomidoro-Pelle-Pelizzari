package it.polimi.ingsw.model.effects.contextual;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;

import java.util.InvalidPropertiesFormatException;

public class AddStars implements ContextualEffect {
    @JsonProperty("stars")
    private int stars;

    @Override
    public boolean onAddedToPlayer(Player p) {
        if (p == null) return false;
        p.getStats().addStars(stars);
        return true;
    }
    @Override
    public String toString(){
        return "ADD " + stars +" STARS";
    }
}
