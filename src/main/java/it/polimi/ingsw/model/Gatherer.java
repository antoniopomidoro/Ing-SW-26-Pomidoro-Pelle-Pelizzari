package it.polimi.ingsw.model;

/**
 * Represents a Gatherer card.
 * This class serves as a marker to identify cards of the Gatherer type.
 * It has no additional properties or methods beyond the base Card class.
 */
public class Gatherer extends Card {

    /**
     * Default constructor for Gatherer.
     * The constructor is empty as instances of this class will be populated using JSON deserialization.
     */
    public Gatherer() {
        super();
    }
}
