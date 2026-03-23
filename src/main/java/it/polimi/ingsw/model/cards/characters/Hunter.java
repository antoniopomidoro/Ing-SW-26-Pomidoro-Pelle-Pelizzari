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
    }

    public Hunter(Age age, boolean hasFood){
        this.age = age;
        this.hasFood = hasFood;
    }

    @Override
    public void onAddedToPlayer(Player p) {
        p.getStats().incrementCharacter(CharacterEnum.HUNTER);
        int food = p.getStats().getCharacterCount(CharacterEnum.HUNTER)*(hasFood ? 1 : 0);
        p.addFood(food);
    }

    public boolean isHasFood() {
        return hasFood;
    }

    public void setHasFood(boolean hasFood) {
        this.hasFood = hasFood;
    }

    @Override
    public Card copy() {
        return new Hunter(this.age, this.hasFood);
    }
}
