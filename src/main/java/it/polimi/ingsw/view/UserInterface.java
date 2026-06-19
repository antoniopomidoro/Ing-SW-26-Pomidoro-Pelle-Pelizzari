package it.polimi.ingsw.view;

import it.polimi.ingsw.network.dto.*;

/**
 * Contract implemented by every client-side view (CLI and GUI). The DTO
 * visitors invoke these callbacks as lobby and game messages arrive, so the
 * concrete view can render the corresponding state without knowing the
 * transport details.
 */
public interface UserInterface {

    /**
     * Re-renders the game from a state-changing event's snapshot.
     *
     * @param state the game event carrying the new snapshot
     * @return true if the update was handled
     */
    boolean update(GameEventDTO state);

    /**
     * Performs the first render when setup completes.
     *
     * @param state the setup event carrying the initial snapshot
     * @return true if the setup was handled
     */
    boolean setUp(GameEventDTO state);

    // --- game events ---

    /**
     * Reports a game error (illegal move) to the user.
     *
     * @param dto the error event
     */
    void onGameError(GameEventDTO dto);

    /**
     * Reacts to the start of a player's turn (e.g. highlighting them).
     *
     * @param dto the turn-started event
     */
    void onPlayerTurnStarted(GameEventDTO dto);

    /**
     * Reacts to a player disconnecting.
     *
     * @param dto the disconnect event
     */
    void onPlayerDisconnected(GameEventDTO dto);

    /**
     * Reacts to the game ending normally (final scores ready).
     *
     * @param dto the end-game event
     */
    void onGameEnded(GameEventDTO dto);

    /**
     * Reacts to an exceptional (technical) win, e.g. by abandonment.
     *
     * @param dto the exceptional-win event
     */
    void onExceptionalWin(GameEventDTO dto);

    /**
     * Reacts to the abandonment countdown. Default is a no-op.
     *
     * @param dto the countdown payload
     */
    default void onCountdown(CountdownDTO dto) {}

    // --- lobby events ---

    /**
     * Reacts to a lobby waiting update (players joining).
     *
     * @param dto the lobby update
     */
    void onLobbyWaiting(LobbyUpdateDTO dto);

    /**
     * Reacts to rejoining an ongoing game.
     *
     * @param dto the lobby update carrying the rejoin snapshot
     */
    void onLobbyRejoin(LobbyUpdateDTO dto);

    /**
     * Reacts to the game starting from the lobby.
     *
     * @param dto the lobby update signalling the start
     */
    void onGameStarting(LobbyUpdateDTO dto);

    // --- other ---

    /**
     * Reacts to a totem-selection update in the lobby.
     *
     * @param dto the totem-selection payload
     */
    void onTotemSelection(TotemSelectionDTO dto);

    /**
     * Reports a lobby error to the user.
     *
     * @param dto the error payload
     */
    void onLobbyError(ErrorDTO dto);

    /**
     * Reacts to a requested totem being unavailable. Defaults to
     * {@link #onLobbyError(ErrorDTO)}.
     *
     * @param dto the error payload
     */
    default void onTotemUnavailable(ErrorDTO dto) { onLobbyError(dto); }

    /**
     * Stops the view and releases its resources.
     */
    void stop();
}
