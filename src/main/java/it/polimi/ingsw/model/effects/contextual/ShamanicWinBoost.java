package it.polimi.ingsw.model.effects.contextual;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;

public class ShamanicWinBoost implements ContextualEffect {
    @JsonProperty("bonus")
    private int bonus;

    @Override
    public void executeEffect(Player p, GameState state) {
        p.getStats().setRitualWinBoost(bonus);
    }
}
