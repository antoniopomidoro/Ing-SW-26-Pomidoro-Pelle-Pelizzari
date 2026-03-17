package it.polimi.ingsw.model.effects.contextual;

import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.cards.characters.*;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.effects.contextual.*;
import it.polimi.ingsw.model.effects.events.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;


public class InventorPair implements ContextualEffect {

    @Override
    public boolean executeEffect(Player p, GameState state) {
        p.addFood(3);
        return true;
    }
}
