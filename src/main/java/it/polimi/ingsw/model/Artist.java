package it.polimi.ingsw.model;

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
}
