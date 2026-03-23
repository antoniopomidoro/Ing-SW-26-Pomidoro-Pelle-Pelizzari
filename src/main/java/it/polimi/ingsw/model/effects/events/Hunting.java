package it.polimi.ingsw.model.effects.events;

import it.polimi.ingsw.model.cards.characters.*;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;

import java.util.List;

public class Hunting implements EventEffect {
    CharacterEnum type = CharacterEnum.HUNTER;

    @Override
    public boolean applyEffect(GameState state, Age age) {
        if (state == null || age == null || state.getOrderTileOrder() == null) {
            return false;
        }
        List<Player> players = state.getOrderTileOrder();
        for (Player p : players) {
            if (p == null) continue;
            int hunter = p.getStats().getCharacterCount(type);
            p.addFood(hunter);
            int pp = hunter * age.getValue();
            p.addPP(pp);
        }
        return true;
    }
}
