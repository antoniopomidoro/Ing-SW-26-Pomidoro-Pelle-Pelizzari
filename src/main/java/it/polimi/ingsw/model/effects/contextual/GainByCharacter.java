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


/**
 * Contextual effect that grants prestige points and/or food proportional to the
 * number of cards of a given character type owned by the player.
 */
public class GainByCharacter implements ContextualEffect {
    @JsonProperty("type")
    private CharacterEnum type;
    @JsonProperty("ppGain")
    private int ppGain;
    @JsonProperty("foodGain")
    private int foodGain;

    /**
     * Grants prestige points and food scaled by how many cards of the target
     * character type the player owns.
     *
     * @param p     the player the effect applies to
     * @param state the current game state
     * @return true if the player owns at least one card of that type
     */
    @Override
    public boolean executeEffect(Player p, GameState state) {
        int charCount = p.getStats().getCharacterCount(type);
        p.addPP(ppGain * charCount);
        p.addFood(foodGain * charCount);
        return charCount > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString(){
        if(foodGain == 0){
            return "AT THE END GIVE " + ppGain + " PP FOR EACH " + type;
        }else if(ppGain == 0){
            return "CAVE PAINTINGS GAIN " + foodGain +" FOOD FOR EACH " + type;

        }else{
            return "HUNTING GIVE " + ppGain + "PP AND " +foodGain + "FOOD FOR EACH " + type;
        }
    }
}
