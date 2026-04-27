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


public class EndCardSet implements ContextualEffect {
    @JsonProperty("pp")
    private int pp;

    @Override
    public boolean executeEffect(Player p, GameState state) {
        p.addPP(p.getStats().calculateSet() * pp);
        return true;
    }

    @Override
    public String toString(){
        return  pp +" PP FOR EACH SET";
    }
}
