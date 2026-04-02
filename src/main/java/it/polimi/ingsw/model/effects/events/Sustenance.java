package it.polimi.ingsw.model.effects.events;

import it.polimi.ingsw.model.cards.characters.*;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;

import java.util.List;

public class Sustenance implements EventEffect {
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
}
