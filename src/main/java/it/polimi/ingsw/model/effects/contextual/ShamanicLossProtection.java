package it.polimi.ingsw.model.effects.contextual;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;

public class ShamanicLossProtection implements ContextualEffect {
    @JsonProperty("protect")
    private int protect;

    @Override
    public boolean onAddedToPlayer(Player p) {

        p.getStats().setRitualLossMultiplier(protect);
        return true;
    }
    public String toString(){
        return "PROTECT PP FROM SHAMANIC EVENT LOSS";
    }
}
