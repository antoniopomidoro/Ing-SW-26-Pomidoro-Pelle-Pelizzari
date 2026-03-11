package it.polimi.ingsw.model;

/**
 * Represents an abstract game card with its basic properties.
 * This class serves as a base for all specific card types in the game.
 * Concrete subclasses of this class are intended to be created from JSON data.
 */
public abstract class Card {
    private Age age;

    /**
     * Default constructor for Card.
     * The constructor is empty as instances of its subclasses will be populated using JSON deserialization.
     */
    public Card() {

    }

    /**
     * Gets the age (era) of the card.
     * @return The age of the card.
     */
    public Age getAge() {
        return this.age;
    }

    /**
     * Sets the age (era) of the card. This method is intended to be used by the JSON deserializer.
     * @param age The age to set for the card.
     * @return True if the age was set successfully, false otherwise.
     */
    public boolean setAge(Age age) {
        if (age == null) {
            return false;
        }

        this.age = age;
        return true;
    }
}
