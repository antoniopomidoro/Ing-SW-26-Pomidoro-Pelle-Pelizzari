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

public class Sustenance implements EventEffect {
    @Override
    public boolean executeEffect(List<Player> players, GameState state, GamePhase phase, Age age) {
        for(Player p : players){
            int neededFood = 0;
            for (CharacterEnum c : CharacterEnum.values()){
                neededFood += p.getStats().getCharacterCount(c);
            }
            int foodToPay = neededFood - p.getStats().getSustainmentDiscount();
            p.payFoodWithPenalty(foodToPay, age.getValue());
        }
        return false;
    }
}
