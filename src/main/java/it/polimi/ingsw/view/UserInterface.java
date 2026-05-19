package it.polimi.ingsw.view;

import it.polimi.ingsw.network.dto.ErrorDTO;
import it.polimi.ingsw.network.dto.GameEventDTO;
import it.polimi.ingsw.network.dto.LobbyUpdateDTO;
import it.polimi.ingsw.network.dto.TotemSelectionDTO;

public interface UserInterface {

    boolean update(GameEventDTO state);
    boolean setUp(GameEventDTO state);

    // --- game events ---
    void onGameError(GameEventDTO dto);
    void onPlayerTurnStarted(GameEventDTO dto);
    void onPlayerDisconnected(GameEventDTO dto);
    void onGameEnded(GameEventDTO dto);

    // --- lobby events ---
    void onLobbyWaiting(LobbyUpdateDTO dto);
    void onLobbyRejoin(LobbyUpdateDTO dto);
    void onGameStarting(LobbyUpdateDTO dto);

    // --- other ---
    void onTotemSelection(TotemSelectionDTO dto);
    void onLobbyError(ErrorDTO dto);
    void stop();
}
