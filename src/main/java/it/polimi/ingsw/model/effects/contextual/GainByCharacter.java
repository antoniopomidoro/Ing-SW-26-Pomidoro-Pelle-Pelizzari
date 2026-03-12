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

    /**
     * Default constructor for JSON deserialization.
     */
    public GainByCharacter() {}

    @JsonCreator
    public GainByCharacter(
            @JsonProperty("type") CharacterEnum type,
            @JsonProperty("ppGain") int ppGain,
            @JsonProperty("foodGain") int foodGain) {
        this.type = type;
        this.ppGain = ppGain;
        this.foodGain = foodGain;
    }

    @Override
    public void executeEffect(Player p, GameState state) {
        int charCount = p.getStats().getCharacterCount(type);
        p.addPP(ppGain * charCount);
        p.addFood(foodGain * charCount);
    }
}
