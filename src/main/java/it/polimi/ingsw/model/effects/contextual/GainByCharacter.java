package it.polimi.ingsw.model.effects.contextual;

import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.cards.characters.*;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.effects.contextual.*;
import it.polimi.ingsw.model.effects.events.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;


public class GainByCharacter implements ContextualEffect {
    private CharacterEnum type;
    private int ppGain;
    private int foodGain;

    public GainByCharacter(CharacterEnum type, int ppGain, int foodGain){
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
