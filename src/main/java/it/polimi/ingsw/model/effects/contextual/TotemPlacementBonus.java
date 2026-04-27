package it.polimi.ingsw.model.effects.contextual;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.model.effects.ContextualEffect;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;


public class TotemPlacementBonus implements ContextualEffect {
    @JsonProperty("bonus")
    private int bonus;
    @Override
    public boolean onAddedToPlayer(Player p) {
        p.getStats().setTotemPlacementBonusFood(bonus);
        return true;
    }

    public String toString(){
        return "+" + bonus + " FOOD FROM TOTEM";
    }
}