package it.polimi.ingsw.model.cards;

import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;


import java.util.List;

/**
 * Represents an Event card.
 * Instances of this class are intended to be created from JSON data.
 */
public class Event extends Card {
    private String id;
    private EventEffect effect;
    private boolean isFinal;
    private GamePhase phase;

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
     * Handles the logic when the event card is discarded (triggered).
     * @param state The current game state.
     * @param players The list of players involved.
     * @return True if the event was executed successfully.
     */
    @Override
    public boolean onDiscard(GameState state, List<Player> players){
        if (effect != null) {
            return effect.executeEffect(players, state, phase, this.getAge());
        }
        return false;
    }
}
