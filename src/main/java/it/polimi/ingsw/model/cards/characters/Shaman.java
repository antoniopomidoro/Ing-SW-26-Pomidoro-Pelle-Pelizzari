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
        setId(CharacterEnum.SHAMAN);
    }

    /**
     * Constructs a Shaman for the given age with a number of stars.
     *
     * @param age   the age the card belongs to
     * @param stars the number of stars granted by the card
     */
    public Shaman(Age age, int stars){
        this.age = age;
        this.stars= stars;
        setId(CharacterEnum.SHAMAN);
    }

    /**
     * Test-only constructor that sets the stars without an age.
     *
     * @param stars the number of stars granted by the card
     */
    protected Shaman(int stars) {
        this.stars = stars;
        setId(CharacterEnum.SHAMAN);
    }

    /**
     * Gets the number of stars on the card.
     * @return The number of stars.
     */
    public int getStars() {
        return stars;
    }

    /**
     * Updates the player's stats when this Shaman is added to their hand,
     * incrementing the character count and adding its stars.
     *
     * @param p the player receiving the card
     * @return true on success, false if the player is null
     */
    @Override
    public boolean onAddedToPlayer(Player p) {
        if (p == null) {
            return false;
        }
        return p.getStats().incrementCharacter(getId())
                && p.getStats().addStars(stars);
    }

    /**
     * Returns a human-readable representation of this Shaman card.
     *
     * @return a string describing age and stars
     */
    @Override
    public String toString(){
        return "SHAMAN, " + super.age + ", STARS: " + stars + "\n";
    }
}
