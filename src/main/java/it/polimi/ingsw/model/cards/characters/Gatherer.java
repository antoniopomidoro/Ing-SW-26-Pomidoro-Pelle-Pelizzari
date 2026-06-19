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
 * Represents a Gatherer card.
 * This class serves as a marker to identify cards of the Gatherer type.
 * It has no additional properties or methods beyond the base Character class.
 */
public class Gatherer extends Character {
    private int sustDisc;
    /**
     * Default constructor for Gatherer.
     * The constructor is empty as instances of this class will be populated using JSON deserialization.
     */
    public Gatherer() {
        super();
        setId(CharacterEnum.GATHERER);
    }

    /**
     * Constructs a Gatherer for the given age with a sustainment discount.
     *
     * @param age      the age the card belongs to
     * @param sustDisc the sustainment discount granted by the card
     */
    public Gatherer(Age age, int sustDisc){
        this.sustDisc = sustDisc;
        this.age = age;
        setId(CharacterEnum.GATHERER);
    }

    /**
     * Test-only setter for the sustainment discount.
     *
     * @param sustDisc the sustainment discount granted by the card
     */
    protected void setSustDisc(int sustDisc) {
        this.sustDisc = sustDisc;
        setId(CharacterEnum.GATHERER);
    }

    /**
     * Updates the player's stats when this Gatherer is added to their hand,
     * incrementing the character count and applying its sustainment discount.
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
                && p.getStats().addSustainmentDiscount(sustDisc);
    }

    /**
     * Gets the sustainment discount granted by this card.
     *
     * @return the sustainment discount
     */
    public int getSustDisc() {
        return sustDisc;
    }

    /**
     * Returns a human-readable representation of this Gatherer card.
     *
     * @return a string describing age and sustainment discount
     */
    @Override
    public  String toString(){
        return "GATHERER, AGE " + super.age + " SUSTAINMENT DISCOUNT: " + sustDisc + "\n";
    }

}
