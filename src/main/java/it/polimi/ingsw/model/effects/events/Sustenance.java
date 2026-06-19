package it.polimi.ingsw.model.effects.events;

import it.polimi.ingsw.model.cards.characters.*;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;

import java.util.List;

/**
 * Event effect that makes every player pay food to sustain their characters.
 * The food owed equals the player's character count reduced by their
 * sustainment discount; any shortfall is paid with a prestige-point penalty
 * scaled by the current age value.
 */
public class Sustenance implements EventEffect {
    /**
     * Charges each player in turn order the food needed to sustain their
     * characters, applying a penalty for any shortfall.
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
            int neededFood = 0;
            for (CharacterEnum c : CharacterEnum.values()) {
                neededFood += p.getStats().getCharacterCount(c);
            }
            int foodToPay = Math.max(0,neededFood-p.getStats().getSustainmentDiscount());
            p.payFoodWithPenalty(foodToPay, age.getValue());
        }
        return true;
    }

    /**
     * Returns a human-readable description of the effect.
     *
     * @return a string describing the effect
     */
    public String toString(){
        return "SUSTAINMENT";
    }
}
