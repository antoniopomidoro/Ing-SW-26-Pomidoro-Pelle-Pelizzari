package it.polimi.ingsw.model.cards.characters;

import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.Character;
import it.polimi.ingsw.model.game.Age;
import it.polimi.ingsw.model.player.*;


/**
 * Represents a specific type of Hunter character (Type 1).
 * Instances of this class are intended to be created from JSON data.
 */
public class Hunter extends Character {
    private boolean hasFood;
    /**
     * Default constructor for Hunter1.
     * The constructor is empty as instances of this class will be populated using JSON deserialization.
     */
    public Hunter() {
        super();
        setId(CharacterEnum.HUNTER);
    }

    /**
     * Constructs a Hunter for the given age.
     *
     * @param age     the age the card belongs to
     * @param hasFood whether this hunter grants food when acquired
     */
    public Hunter(Age age, boolean hasFood){
        this.age = age;
        this.hasFood = hasFood;
        setId(CharacterEnum.HUNTER);
    }

    /**
     * Test-only constructor that sets the food flag without an age.
     *
     * @param hasFood whether this hunter grants food when acquired
     */
    protected Hunter(boolean hasFood) {
        this.hasFood = hasFood;
        setId(CharacterEnum.HUNTER);
    }

    /**
     * Updates the player's stats when this Hunter is added to their hand,
     * incrementing the character count and granting food proportional to the
     * number of hunters owned (when this hunter provides food).
     *
     * @param p the player receiving the card
     * @return true on success, false if the player is null or the update fails
     */
    @Override
    public boolean onAddedToPlayer(Player p) {
        if (p == null) {
            return false;
        }
        if (!p.getStats().incrementCharacter(getId())) {
            return false;
        }
        int food = p.getStats().getCharacterCount(getId())*(hasFood ? 1 : 0);
        return p.addFood(food);
    }

    /**
     * Returns a human-readable representation of this Hunter card.
     *
     * @return a string describing age and whether it grants food
     */
    @Override
    public String toString(){
        if(hasFood){
            return "HUNTER, " +super.age + ", HAS FOOD \n";
        }else{
            return "HUNTER, " +super.age + ", HASN'T FOOD \n";
        }
    }
}
