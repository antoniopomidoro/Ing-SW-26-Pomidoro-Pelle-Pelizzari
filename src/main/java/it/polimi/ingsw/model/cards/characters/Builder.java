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
 * Represents a Builder card, a specific type of card that provides discounts and prestige points.
 * Instances of this class are intended to be created from JSON data.
 */
public class Builder extends Character {
    private int discount;
    private int pp;

    /**
     * Default constructor for Builder.
     * The constructor is empty as instances of this class will be populated using JSON deserialization.
     */
    public Builder() {
        setId(CharacterEnum.BUILDER);
    }
    /**
     * Constructs a Builder for the given age with its prestige points and discount.
     *
     * @param age      the age the card belongs to
     * @param pp       the prestige points granted by the card
     * @param discount the building discount granted by the card
     */
    public Builder(Age age, int pp , int discount){
        this.age = age;
        this.discount =discount;
        this.pp=pp;
        setId(CharacterEnum.BUILDER);
    }

    /**
     * Test-only constructor that sets discount and prestige points without an age.
     *
     * @param discount the building discount granted by the card
     * @param pp       the prestige points granted by the card
     */
    protected Builder(int discount, int pp) {
        this.discount = discount;
        this.pp = pp;
        setId(CharacterEnum.BUILDER);
    }

    /**
     * Gets the discount value provided by the card.
     * @return The discount value.
     */
    public int getDiscount() {
        return this.discount;
    }

    /**
     * Gets the prestige points (PP) provided by the card.
     * @return The prestige points.
     */
    public int getPP() {
        return this.pp;
    }

    /**
     * Updates the player's stats when this Builder is added to their hand,
     * incrementing the character count and applying its prestige points and discount.
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
                && p.getStats().addBuilderPp(pp)
                && p.getStats().addBuildingDiscount(discount);
    }


    /**
     * Returns a human-readable representation of this Builder card.
     *
     * @return a string describing age, discount and prestige points
     */
    @Override
    public String toString(){
        return "BUILDER, AGE: " + super.age +", DISCOUNT: " + discount + ", PP: " + pp + "\n" ;
    }
}
