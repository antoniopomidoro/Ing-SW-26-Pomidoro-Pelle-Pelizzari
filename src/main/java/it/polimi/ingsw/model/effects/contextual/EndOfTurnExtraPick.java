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


public class EndOfTurnExtraPick implements ContextualEffect {
    @JsonProperty("upperPick")
    private int upperPick;

    @Override
    public boolean onAddedToPlayer(Player p) {
        p.getStats().setExtraUpperPick(upperPick);
        return true;
    }

    @Override

    public String toString(){
        return "EXTRA UPPER PICK";
    }
}
