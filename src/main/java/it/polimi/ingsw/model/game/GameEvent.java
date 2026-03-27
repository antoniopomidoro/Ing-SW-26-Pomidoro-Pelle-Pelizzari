package it.polimi.ingsw.model.game;

import it.polimi.ingsw.model.player.Player;

/**
 * Represents a domain event emitted by the {@link GameState} when a significant
 * action (valid or invalid) occurs, requiring notification to observers (e.g., View/Controller).
 * <p>
 * This class encapsulates the details of what happened, who caused it, and
 * a descriptive message, allowing decoupled communication within the system.
 */
public class GameEvent {

    /**
     * Enumeration of the semantic types of events that can be broadcasted.
     * These represent the minimum set of event types handled by the error broadcasting flow.
     */
    public enum Type {
        /**
         * Indicates that a player attempted to perform an action when it was not their turn.
         */
        WRONG_TURN,
        /**
         * Indicates that a player attempted an action requiring food but did not have enough.
         */
        INSUFFICIENT_FOOD,
        /**
         * Indicates that an invalid index (e.g., for a card or board position) was provided.
         */
        INVALID_INDEX,
        /**
         * Indicates that an action was attempted in a game phase where it is not permitted.
         */
        INVALID_PHASE,
        INSUFFICIENT_PICKS,
        SUCCESSFUL_ACTION
    }

    private final Type type;
    private final Player culprit;
    private final String message;

    /**
     * Constructs a new broadcastable game event.
     *
     * @param type    The semantic type of the event. Must not be null.
     * @param culprit The {@link Player} associated with the event. Can be null if the event is not specific to a single player.
     * @param message A descriptive message suitable for logging or displaying in the UI.
     */
    public GameEvent(Type type, Player culprit, String message) {
        this.type = type;
        this.culprit = culprit;
        this.message = message;
    }

    /**
     * Retrieves the semantic type of the event.
     *
     * @return The {@link Type} of the event.
     */
    public Type getType() {
        return type;
    }

    /**
     * Retrieves the player who triggered or is associated with the event.
     *
     * @return The associated {@link Player}, or null if not applicable.
     */
    public Player getCulprit() {
        return culprit;
    }

    /**
     * Retrieves the descriptive message associated with the event.
     *
     * @return The descriptive message string.
     */
    public String getMessage() {
        return message;
    }
}
