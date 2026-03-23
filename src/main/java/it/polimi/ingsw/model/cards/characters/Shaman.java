package it.polimi.ingsw.model.cards.characters;

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
 * Represents a Shaman card, which has a certain number of stars.
 * Instances of this class are intended to be created from JSON data.
 */
public class Shaman extends Character {
    private int stars;

    /**
     * Default constructor for Shaman.
     * The constructor is empty as instances of this class will be populated using JSON deserialization.
     */
    public Shaman() {
        super();
    }

    public Shaman(Age age, int stars){
        this.age = age;
        this.stars= stars;
    }

    /**
     * Gets the number of stars on the card.
     * @return The number of stars.
     */
    public int getStars() {
        return stars;
    }

    @Override
    public void onAddedToPlayer(Player p) {
        p.getStats().incrementCharacter(CharacterEnum.SHAMAN);
        p.getStats().addStars(stars);
    }

    @Override
    public Card copy() {
        return new Shaman(this.age, this.stars);
    }
}
