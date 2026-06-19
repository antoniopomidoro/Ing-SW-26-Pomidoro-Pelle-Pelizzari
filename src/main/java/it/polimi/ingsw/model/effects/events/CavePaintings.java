package it.polimi.ingsw.model.effects.events;

import it.polimi.ingsw.model.cards.characters.*;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;

import java.util.List;

/**
 * Event effect that rewards or penalizes each player based on how many Artist
 * cards they own: players with fewer artists than the current age value pay
 * prestige points, while the others gain prestige points scaled by their
 * artist count.
 */
public class CavePaintings implements EventEffect {
    private CharacterEnum type = CharacterEnum.ARTIST;

    /**
     * Applies the cave-paintings reward/penalty to every player in turn order.
     *
     * @param state the current game state
     * @param age   the age in which the event resolves
     * @return true if applied, false if state, age or the turn order is null
     */
    @Override
    public boolean applyEffect(GameState state, Age age) {
        if (state == null || age == null || state.getOrderTileOrder() == null) {
            return false;
        }
        List<Player> players = state.getOrderTileOrder();
        for (Player p : players) {
            if (p == null) continue;
            int artist = p.getStats().getCharacterCount(type);
            if (artist < age.getValue()) {
                p.payPP(2);
            } else {
                p.addPP(age.getValue() * artist);
            }
        }
        return true;
    }

    /**
     * Returns a human-readable description of the effect.
     *
     * @return a string describing the effect
     */
    public String toString(){
        return "CAVE PAINTINGS";
    }
}