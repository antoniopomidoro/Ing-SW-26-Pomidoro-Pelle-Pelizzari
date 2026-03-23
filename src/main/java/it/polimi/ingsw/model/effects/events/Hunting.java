package it.polimi.ingsw.model.effects.events;

import it.polimi.ingsw.model.cards.characters.*;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;

import java.util.List;

public class Hunting implements EventEffect {
    CharacterEnum type = CharacterEnum.HUNTER;

    @Override
    public boolean applyEffect(List<Player> players, GameState state, Age age) {
        if (players == null || state == null || age == null) {
            return false;
        }
        for (Player p : players) {
            int hunter = p.getStats().getCharacterCount(type);
            p.addFood(hunter);
            int pp = hunter * age.getValue();
            p.addPP(pp);
        }
        return true;
    }
}
