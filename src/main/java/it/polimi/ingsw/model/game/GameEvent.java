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
     * Each event knows independently whether it represents a state-altering action
     * that requires saving the game to disk.
     */
    public enum Type {
        // --- ERRORS ---
        WRONG_TURN(false),
        INSUFFICIENT_FOOD(false),
        INVALID_INDEX(false),
        INVALID_PHASE(false),
        INSUFFICIENT_PICKS(false),
        INVALID_ACTION(false),
        OCCUPIED_TILE(false),

        // --- VALID ACTIONS ---
        SUCCESSFUL_ACTION(true),
        UPPER_CARD(true),
        BOTTOM_CARD(true),
        UPPER_BUILDING(true),
        BOTTOM_BUILDING(true),
        BOARD_UPDATE(true),
        AGE_CHANGED(true);

        private final boolean requiresSave;

        /**
         * Enum constructor.
         * @param requiresSave true if this event should trigger a disk save.
         */
        Type(boolean requiresSave) {
            this.requiresSave = requiresSave;
        }

        /**
         * Indicates whether this event modified the game state.
         * @return true if persistence is required.
         */
        public boolean requiresSave() {
            return requiresSave;
        }
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