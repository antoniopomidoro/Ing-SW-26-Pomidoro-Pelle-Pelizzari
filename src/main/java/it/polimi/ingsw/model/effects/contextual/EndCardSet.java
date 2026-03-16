package it.polimi.ingsw.model.effects.contextual;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.cards.characters.*;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.effects.contextual.*;
import it.polimi.ingsw.model.effects.events.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;

import java.util.Arrays;


public class EndCardSet implements ContextualEffect {
    @JsonProperty("pp")
    private int pp;

    @Override
    public void executeEffect(Player p, GameState state) {
        int set = Arrays.stream(CharacterEnum.values())
                .mapToInt(c -> p.getStats().getCharacterCount(c))
                .min()
                .orElse(0);
        p.addPP(set * pp);
    }
}
