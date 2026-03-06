package it.polimi.ingsw.model;

/**
 * Represents an Event card.
 * This class serves as a marker to identify cards of the Event type.
 * It has no additional properties or methods beyond the base Card class.
 */
public class Event extends Card {

    /**
     * Default constructor for Event.
     * The constructor is empty as instances of this class will be populated using JSON deserialization.
     */
    public Event() {
        super();
    }
}
