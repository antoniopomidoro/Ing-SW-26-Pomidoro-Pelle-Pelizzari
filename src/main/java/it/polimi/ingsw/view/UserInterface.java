package it.polimi.ingsw.view;

import it.polimi.ingsw.network.dto.*;

public interface UserInterface {

    boolean update(GameEventDTO state);
    boolean setUp(GameEventDTO state);

    // --- game events ---
    void onGameError(GameEventDTO dto);
    void onPlayerTurnStarted(GameEventDTO dto);
    void onPlayerDisconnected(GameEventDTO dto);
    void onGameEnded(GameEventDTO dto);
    void onExceptionalWin(GameEventDTO dto);
    default void onCountdown(CountdownDTO dto) {}

    // --- lobby events ---
    void onLobbyWaiting(LobbyUpdateDTO dto);
    void onLobbyRejoin(LobbyUpdateDTO dto);
    void onGameStarting(LobbyUpdateDTO dto);

    // --- other ---
    void onTotemSelection(TotemSelectionDTO dto);
    void onLobbyError(ErrorDTO dto);
    default void onTotemUnavailable(ErrorDTO dto) { onLobbyError(dto); }
    void stop();
}
