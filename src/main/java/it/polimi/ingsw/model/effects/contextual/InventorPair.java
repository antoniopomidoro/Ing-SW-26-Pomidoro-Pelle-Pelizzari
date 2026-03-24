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
    private int oldPairs = 0;

    /* The method is activated when the player buys the building */
    public void countOldPairs(Player p) {
        for(Tool t : Tool.values()) {
            int count = p.getStats().getToolCount(t);
            if(count >= 2) {
                this.oldPairs += count / 2;
            }
        }
    }

    @Override
    public boolean executeEffect(Player p, GameState state) {
        int newPairs = 0;
        for(Tool t : Tool.values()) {
            int count = p.getStats().getToolCount(t);
            if(count >= 2) {
                newPairs += count / 2;
            }
        }
        if(newPairs > this.oldPairs) {
            p.addFood(bonus * (newPairs - oldPairs));
            this.oldPairs = newPairs;
        }
        return true;
    }
}