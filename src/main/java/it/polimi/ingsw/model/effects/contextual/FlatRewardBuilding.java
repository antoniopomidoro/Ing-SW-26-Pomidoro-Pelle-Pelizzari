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


public class FlatRewardBuilding implements ContextualEffect {
    @JsonProperty("stars")
    private int stars;
    @JsonProperty("protect")
    private int protect;
    @JsonProperty("bonus")
    private int bonus;

    @Override
    public void executeEffect(Player p, GameState state) {
        p.getStats().addStars(stars);
        p.getStats().setRitualLossMultiplier(protect);
        p.getStats().setRitualWinBoost(bonus);
    }
}
