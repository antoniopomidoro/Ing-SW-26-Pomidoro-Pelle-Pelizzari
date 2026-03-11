package it.polimi.ingsw.model;

import java.util.List;

/**
 * Represents an Event card.
 * Instances of this class are intended to be created from JSON data.
 */
public class Event extends Card {
    private String id;
    private EventEffect effect;

    /**
     * Default constructor for Event.
     * The constructor is empty as instances of this class will be populated using JSON deserialization.
     */
    public Event() {
        super();
    }

    /**
     * Gets the ID of the event.
     * @return The event ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the ID of the event.
     * @param id The event ID.
     * @return True if successful.
     */
    public boolean setId(String id) {
        if (id == null) return false;
        this.id = id;
        return true;
    }

    /**
     * Triggers the event effect for all players.
     * @param players The list of players.
     * @param state The game state.
     * @return True if successful.
     */
    public boolean triggerEvent(List<Player> players, GameState state) {
        return false;
    }
}
