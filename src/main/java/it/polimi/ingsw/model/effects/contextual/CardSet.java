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


public class CardSet implements ContextualEffect {
    @JsonProperty("food")
    private int food;
    private int baseSet;

    @Override
    public boolean onAddedToPlayer(Player p) {
        baseSet = p.getStats().calculateSet();
        return true;
    }

    @Override
    public boolean executeEffect(Player p, GameState state) {
        int newSet = p.getStats().calculateSet();
        if (newSet > baseSet) {
            p.addFood((newSet - baseSet) * food);
            baseSet = newSet; // Update baseSet to the new set value
            return true;
        }
        return true;
    }
    @Override
    public String toString(){
        return " GIVE FOOD EVERY 6 CARDS SET COMPLETED ";
    }
}
