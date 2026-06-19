package it.polimi.ingsw.model.effects.events;

import it.polimi.ingsw.model.cards.characters.*;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;

import java.util.List;

/**
 * Event effect that grants each player food and prestige points proportional to
 * the number of Hunter cards they own (prestige scaled by the current age value).
 */
public class Hunting implements EventEffect {
    CharacterEnum type = CharacterEnum.HUNTER;

    /**
     * Applies the hunting reward to every player in turn order.
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
            int hunter = p.getStats().getCharacterCount(type);
            p.addFood(hunter);
            int pp = hunter * age.getValue();
            p.addPP(pp);
        }
        return true;
    }

    /**
     * Returns a human-readable description of the effect.
     *
     * @return a string describing the effect
     */
    public String toString(){
        return "HUNTING";
    }
}
