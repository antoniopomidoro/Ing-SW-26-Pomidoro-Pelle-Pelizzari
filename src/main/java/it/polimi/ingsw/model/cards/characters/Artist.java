package it.polimi.ingsw.model.cards.characters;

import it.polimi.ingsw.controller.JsonFactory;
import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.cards.Character;
import it.polimi.ingsw.model.cards.characters.*;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.effects.contextual.*;
import it.polimi.ingsw.model.effects.events.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;


/**
 * Represents an Artist card.
 * This class serves as a marker to identify cards of the Artist type.
 * It has no additional properties or methods beyond the base Character class.
 */
public class Artist extends Character {

    /**
     * Default constructor for Artist.
     * The constructor is empty as instances of this class will be populated using JSON deserialization.
     */
    public Artist() {
        super();
    }

    public Artist(Age age){
        this.age = age;
    }


    @Override
    public boolean onAddedToPlayer(Player p) {
        if (p == null) {
            return false;
        }
        return p.getStats().incrementCharacter(CharacterEnum.ARTIST);
    }

    @Override
    public String toString() {
        return "ARTIST, AGE: " + super.age +"\n" ;
    }
}
