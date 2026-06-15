package it.polimi.ingsw.model.game.StatePhases;

import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.Player;

import java.io.Serializable;
import java.util.Optional;

/**
 * Represents the behavior of a specific game phase within the State design pattern.
 * <p>
 * This interface defines the set of actions that can be performed during any game phase.
 * Each concrete implementation of this interface will provide the logic for a specific
 * phase (e.g., {@link SetupPhase}, {@link PlayerTurnPhase}).
 * <p>
 * By default, all methods emit a {@link GameEvent.Type#INVALID_PHASE} event and throw
 * {@link IllegalMoveException}. Concrete states
 * must override the methods corresponding to the actions allowed in that phase.
 * This ensures that an action invoked in an incorrect phase results in a clear error.
 */
public interface GamePhaseBehavior extends Serializable {

    /**
     * Executes the primary logic associated with this game phase.
     * This method is typically called once when the game transitions into this state.
     *
     * @param context The {@link GameState} which serves as the context for the state pattern.
     * @return {@code true} if the execution was successful, {@code false} otherwise.
     * @throws IllegalMoveException if this action is not applicable to the current phase.
     */
    default boolean execute(GameState context) {
        if (context != null) {
            context.raiseEvent(new GameEvent(GameEvent.Type.INVALID_PHASE, null));
        }
        throw new IllegalMoveException("Action not allowed in the current phase");
    }

    /**
     * Transitions the game to the next logical phase.
     * The implementation is responsible for instantiating and setting the next phase on the context.
     *
     * @param context The {@link GameState} which serves as the context for the state pattern.
     * @return {@code true} if the transition was successful, {@code false} otherwise.
     * @throws IllegalMoveException if this action is not applicable to the current phase.
     */
    default boolean nextPhase(GameState context) {
        if (context != null) {
            context.raiseEvent(new GameEvent(GameEvent.Type.INVALID_PHASE, null));
        }
        throw new IllegalMoveException("Action not allowed in the current phase");
    }

    /**
     * Handles the action of a player picking the top card from a deck.
     *
     * @param context The current game state.
     * @param index   The index of the deck from which to pick the card.
     * @param player  The player performing the action.
     * @param cardInstanceId The expected UUID of the card being picked.
     * @return {@code true} if the action was successful, {@code false} otherwise.
     * @throws IllegalMoveException if this action is not allowed in the current phase.
     */
    default boolean pickTopCard(GameState context, int index, Player player, String cardInstanceId) {
        if (context != null) {
            context.raiseEvent(new GameEvent(GameEvent.Type.INVALID_PHASE, player));
        }
        throw new IllegalMoveException("Action not allowed in the current phase");
    }

    /**
     * Handles the action of a player picking the bottom card from a deck.
     *
     * @param context The current game state.
     * @param index   The index of the deck from which to pick the card.
     * @param player  The player performing the action.
     * @param cardInstanceId The expected UUID of the card being picked.
     * @return {@code true} if the action was successful, {@code false} otherwise.
     * @throws IllegalMoveException if this action is not allowed in the current phase.
     */
    default boolean pickBottomCard(GameState context, int index, Player player, String cardInstanceId) {
        if (context != null) {
            context.raiseEvent(new GameEvent(GameEvent.Type.INVALID_PHASE, player));
        }
        throw new IllegalMoveException("Action not allowed in the current phase");
    }

    /**
     * Handles the action of a player picking the top building from a stack.
     *
     * @param context The current game state.
     * @param index   The index of the building stack.
     * @param player  The player performing the action.
     * @param cardInstanceId The expected UUID of the building being picked.
     * @return {@code true} if the action was successful, {@code false} otherwise.
     * @throws IllegalMoveException if this action is not allowed in the current phase.
     */
    default boolean pickTopBuilding(GameState context, int index, Player player, String cardInstanceId) {
        if (context != null) {
            context.raiseEvent(new GameEvent(GameEvent.Type.INVALID_PHASE, player));
        }
        throw new IllegalMoveException("Action not allowed in the current phase");
    }

    /**
     * Handles the action of a player picking the bottom building from a stack.
     *
     * @param context The current game state.
     * @param index   The index of the building stack.
     * @param player  The player performing the action.
     * @param cardInstanceId The expected UUID of the building being picked.
     * @return {@code true} if the action was successful, {@code false} otherwise.
     * @throws IllegalMoveException if this action is not allowed in the current phase.
     */
    default boolean pickBottomBuilding(GameState context, int index, Player player, String cardInstanceId) {
        if (context != null) {
            context.raiseEvent(new GameEvent(GameEvent.Type.INVALID_PHASE, player));
        }
        throw new IllegalMoveException("Action not allowed in the current phase");
    }

    /**
     * Handles the action of a player occupying a tile on the offer trail.
     *
     * @param context The current game state.
     * @param index   The index of the tile on the offer trail.
     * @param player  The player performing the action.
     * @return {@code true} if the action was successful, {@code false} otherwise.
     * @throws IllegalMoveException if this action is not allowed in the current phase.
     */
    default boolean occupyOfferTrailTile(GameState context, int index, Player player) {
        if (context != null) {
            context.raiseEvent(new GameEvent(GameEvent.Type.INVALID_PHASE, player));
        }
        throw new IllegalMoveException("Action not allowed in the current phase");
    }

    /**
     * Transitions the game to the next age.
     *
     * @param context The {@link GameState} which serves as the context for the state pattern.
     * @return {@code true} if the transition was successful, {@code false} otherwise.
     * @throws IllegalMoveException if this action is not applicable to the current phase.
     */
    /**
     * Remaining upper-row picks for the active player in the current phase.
     * Phases that do not allow picks return 0 (default).
     *
     * @return number of remaining upper picks
     */
    default int getRemainingUpperPicks() {
        return 0;
    }

    /**
     * Remaining bottom-row picks for the active player in the current phase.
     * Phases that do not allow picks return 0 (default).
     *
     * @return number of remaining bottom picks
     */
    default int getRemainingBottomPicks() {
        return 0;
    }

    default boolean nextAge(GameState context) {
        if (context != null) {
            context.raiseEvent(new GameEvent(GameEvent.Type.INVALID_PHASE, null));
        }
        throw new IllegalMoveException("Action not allowed in the current phase");
    }

    /**
     * Reacts to a player disconnecting while this phase is active.
     * <p>
     * The player has already been marked disconnected on the model when this is
     * called. The default implementation does nothing: most phases have no notion
     * of a "current" player whose turn must be skipped (they are transient or do
     * not await a specific player's input). Only the phases that actively wait for
     * a single player — {@link StartTurnPhase} (tile occupation) and
     * {@link PlayerTurnPhase} (card/building drafting) — override this to advance
     * past the player who left. This is a lifecycle hook, not a player action, so
     * it never throws {@link IllegalMoveException}.
     *
     * @param context the {@link GameState} context for the state pattern
     * @param player  the player who just disconnected
     * @return {@code true} if this phase advanced as a result, {@code false} otherwise
     */
    default boolean handlePlayerDisconnect(GameState context, Player player) {
        return false;
    }

    /**
     * Resolves the player this phase is currently awaiting an action from.
     * <p>
     * This is the authoritative source for "whose turn it is" in the public
     * snapshot, replacing index-based lookups (e.g. {@code turnOrder.get(index)})
     * that break when a disconnected player occupies an earlier tile: such lookups
     * count connected and disconnected occupiers inconsistently and end up pointing
     * at the wrong player. The default returns {@link Optional#empty()} — most
     * phases are transient or await no specific player. {@link PlayerTurnPhase} and
     * {@link StartTurnPhase} override this with their own active player.
     *
     * @param context the {@link GameState} context for the state pattern
     * @return the player currently expected to act, or empty if none
     */
    default Optional<Player> resolveActivePlayer(GameState context) {
        return Optional.empty();
    }
}
