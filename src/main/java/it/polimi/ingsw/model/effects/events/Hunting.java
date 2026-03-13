package it.polimi.ingsw.model.effects.events;

import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.cards.characters.*;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.effects.contextual.*;
import it.polimi.ingsw.model.effects.events.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;


import java.util.List;

public class Hunting implements EventEffect {
    CharacterEnum type = CharacterEnum.HUNTER;
    @Override
    public boolean executeEffect(List<Player> players, GameState state, GamePhase phase, Age age) {
        for(Player p : players){
            int hunter = p.getStats().getCharacterCount(type);
            p.addFood(hunter);
            int pp = hunter*age.getValue();
            p.addPP(pp);
            for (Building b : p.getBuildingsByPhase(phase)){
                b.triggerBuildingEffect(p, state);
            }
        }
        return false;
    }
}
