package it.polimi.ingsw.model.effects.contextual;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;

public class AddStars implements ContextualEffect {
    @JsonProperty("stars")
    private int stars;

    @Override
    public void executeEffect(Player p, GameState state) {
        p.getStats().addStars(stars);
    }
}
