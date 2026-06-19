package it.polimi.ingsw.model.game;

import it.polimi.ingsw.model.player.Player;

/**
 * Represents a domain event emitted by the {@link GameState} when a significant
 * action (valid or invalid) occurs, requiring notification to observers (e.g., View/Controller).
 * <p>
 * This class encapsulates what happened and who caused it, allowing
 * decoupled communication within the system.
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
        INVALID_ID(false),
        INVALID_CARD(false),
        /** A move was attempted while fewer than the minimum players are connected. */
        INSUFFICIENT_PLAYERS(false),

        // --- VALID ACTIONS ---
        SUCCESSFUL_ACTION(true),
        UPPER_CARD(true),
        BOTTOM_CARD(true),
        UPPER_BUILDING(true),
        BOTTOM_BUILDING(true),
        BOARD_UPDATE(true),
        AGE_CHANGED(true),
        PLAYER_DISCONNECTED(true),
        /** An event card was discarded and its effect resolved. */
        EVENT_CARD_TRIGGERED(true),
        /** At least one building effect was activated by a trigger. */
        BUILDING_ACTIVATED(false),
        PLAYER_RECONNECTED(true),

        // --- PHASE TRANSITIONS ---
        /** Setup completed: initial board and food ready. */
        SETUP_COMPLETED(true),
        /** StartTurn completed: cards reloaded, food distributed. */
        START_TURN_COMPLETED(true),
        /** A player has placed their totem on a tile. */
        TILE_OCCUPIED(true),
        /** All players have completed their picks in the turn. */
        TURN_COMPLETED(true),
        /** Start of a player's turn: the View can show the pick UI. */
        PLAYER_TURN_STARTED(true),
        /** End turn: cards discarded, tiles freed, counter incremented. */
        END_TURN_COMPLETED(true),
        START_TURN_STARTED(true),
        STARTING_END_GAME(true),
        /** End game: final scores calculated. */
        END_GAME_COMPLETED(true),
        /** A player won by exceptional/technical means (abandonment or easter egg). */
        EXCEPTIONAL_WIN(true);

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
    private final TriggerKey triggerKey;

    /**
     * Constructs a new broadcastable game event.
     *
     * @param type    The semantic type of the event. Must not be null.
     * @param culprit The {@link Player} associated with the event. Can be null if the event is not specific to a single player.
     */
    public GameEvent(Type type, Player culprit) {
        this(type, culprit, null);
    }

    /**
     * Constructs a game event carrying the trigger key that caused it (e.g. EVENT_CARD_TRIGGERED).
     *
     * @param type       The semantic type of the event.
     * @param culprit    The player associated with the event, or null.
     * @param triggerKey The trigger key that originated the event, or null.
     */
    public GameEvent(Type type, Player culprit, TriggerKey triggerKey) {
        this.type = type;
        this.culprit = culprit;
        this.triggerKey = triggerKey;
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
     * Retrieves the trigger key associated with this event, if any.
     *
     * @return The {@link TriggerKey}, or null if not applicable.
     */
    public TriggerKey getTriggerKey() {
        return triggerKey;
    }
}
