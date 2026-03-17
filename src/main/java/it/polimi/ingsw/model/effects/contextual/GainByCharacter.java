package it.polimi.ingsw.model.effects.contextual;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.cards.characters.*;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.effects.contextual.*;
import it.polimi.ingsw.model.effects.events.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;


public class GainByCharacter implements ContextualEffect {
    @JsonProperty("type")
    private CharacterEnum type;
    @JsonProperty("ppGain")
    private int ppGain;
    @JsonProperty("foodGain")
    private int foodGain;

    @Override
    public boolean executeEffect(Player p, GameState state) {
        int charCount = p.getStats().getCharacterCount(type);
        p.addPP(ppGain * charCount);
        p.addFood(foodGain * charCount);
        return true;
    }
}
