package it.polimi.ingsw.model.effects.contextual;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.cards.characters.*;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.effects.events.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;

public class InventorPair implements ContextualEffect {
    @JsonProperty("bonus")
    private int bonus;
    private int oldPairs;

    protected int getOldPairs() {
        return oldPairs;
    }

    protected int getBonus() {
        return bonus;
    }

    @Override
    public boolean onAddedToPlayer(Player p) {
        this.oldPairs = p.getStats().getEqualInventorPair();
        return true;
    }

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
    public String toString(){
        return "GIVE " + bonus + " FOOD FOR EACH NEW INVENTOR PAIR COMPLETED";
    }
}