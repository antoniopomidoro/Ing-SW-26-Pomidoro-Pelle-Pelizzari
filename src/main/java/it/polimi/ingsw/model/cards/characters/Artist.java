package it.polimi.ingsw.model.cards.characters;

import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.cards.Character;
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
        setId(CharacterEnum.ARTIST);
    }

    /**
     * Constructs an Artist for the given age.
     *
     * @param age the age the card belongs to
     */
    public Artist(Age age){
        this.age = age;
        setId(CharacterEnum.ARTIST);
    }


    /**
     * Updates the player's stats when this Artist is added to their hand,
     * incrementing the Artist character count.
     *
     * @param p the player receiving the card
     * @return true on success, false if the player is null
     */
    @Override
    public boolean onAddedToPlayer(Player p) {
        if (p == null) {
            return false;
        }
        return p.getStats().incrementCharacter(getId());
    }

    /**
     * Returns a human-readable representation of this Artist card.
     *
     * @return a string describing the card type and age
     */
    @Override
    public String toString() {
        return "ARTIST, AGE: " + super.age +"\n" ;
    }
}
