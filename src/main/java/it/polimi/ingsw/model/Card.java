package it.polimi.ingsw.model;

/**
 * Represents an abstract game card with its basic properties.
 * This class serves as a base for all specific card types in the game.
 * Concrete subclasses of this class are intended to be created from JSON data.
 */
public abstract class Card {
    private Age age;
    private String id;
    private int minPlayers;

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

    /**
     * Gets the unique identifier of the card.
     * @return The ID of the card.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Sets the unique identifier of the card. This method is intended to be used by the JSON deserializer.
     * @param id The ID to set for the card.
     * @return True if the ID was set successfully, false otherwise.
     */
    public boolean setId(String id) {
        if (id == null || id.trim().isEmpty()){
            return false;
        }

        this.id = id;
        return true;
    }

    /**
     * Gets the minimum number of players required to use this card.
     * @return The minimum number of players.
     */
    public int getMinPlayers() {
        return this.minPlayers;
    }

    /**
     * Sets the minimum number of players required to use this card. This method is intended to be used by the JSON deserializer.
     * @param minPlayers The minimum number of players.
     * @return True if the minimum number of players was set successfully, false otherwise.
     */
    public boolean setMinPlayers(int minPlayers) {
        if (minPlayers <=1 || minPlayers>5){
            return false;
        }

        this.minPlayers = minPlayers;
        return true;
    }
}
