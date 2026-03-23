package it.polimi.ingsw.model.effects.events;

import it.polimi.ingsw.model.cards.characters.*;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;

import java.util.List;

public class Sustenance implements EventEffect {
    @Override
    public boolean applyEffect(List<Player> players, GameState state, Age age) {
        if (players == null || state == null || age == null) {
            return false;
        }
        for (Player p : players) {
            int neededFood = 0;
            for (CharacterEnum c : CharacterEnum.values()) {
                neededFood += p.getStats().getCharacterCount(c);
            }
            int foodToPay = neededFood - p.getStats().getSustainmentDiscount();
            p.payFoodWithPenalty(foodToPay, age.getValue());
        }
        return true;
    }
}
