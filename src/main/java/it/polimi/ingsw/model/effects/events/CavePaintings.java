package it.polimi.ingsw.model.effects.events;

import it.polimi.ingsw.model.cards.characters.*;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;

import java.util.List;

public class CavePaintings implements EventEffect {
    private CharacterEnum type = CharacterEnum.ARTIST;

    @Override
    public boolean applyEffect(List<Player> players, GameState state, Age age) {
        if (players == null || state == null || age == null) {
            return false;
        }
        for (Player p : players) {
            int artist = p.getStats().getCharacterCount(type);
            if (artist < age.getValue()) {
                p.payPP(2);
            } else {
                p.addPP(age.getValue() * artist);
            }
        }
        return true;
    }
}
