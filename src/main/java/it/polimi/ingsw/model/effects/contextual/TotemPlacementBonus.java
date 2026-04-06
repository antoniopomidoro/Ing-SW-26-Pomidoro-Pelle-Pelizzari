package it.polimi.ingsw.model.effects.contextual;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.model.effects.ContextualEffect;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;

import java.util.List;

public class TotemPlacementBonus implements ContextualEffect {
    @JsonProperty("bonus")
    private int bonus;
    @Override
    public boolean executeEffect(Player p, GameState state) {
        p.getStats().setTotemPlacementBonusFood(1);
        return true;
    }
}