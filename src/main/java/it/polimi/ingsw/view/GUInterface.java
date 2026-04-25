package it.polimi.ingsw.view;

import it.polimi.ingsw.network.dto.ErrorDTO;
import it.polimi.ingsw.network.dto.GameEventDTO;
import it.polimi.ingsw.network.dto.LobbyUpdateDTO;
import it.polimi.ingsw.network.dto.TotemSelectionDTO;

public class GUInterface implements UserInterface {

    @Override
    public boolean update(GameEventDTO state) {
        return false;
    }

    @Override
    public boolean setUp(GameEventDTO state) {
        return false;
    }

    @Override
    public void onGameError(GameEventDTO dto) {}

    @Override
    public void onPlayerTurnStarted(GameEventDTO dto) {}

    @Override
    public void onPlayerDisconnected(GameEventDTO dto) {}

    @Override
    public void onGameEnded(GameEventDTO dto) {}

    @Override
    public void onLobbyWaiting(LobbyUpdateDTO dto) {}

    @Override
    public void onLobbyRejoin(LobbyUpdateDTO dto) {}

    @Override
    public void onGameStarting(LobbyUpdateDTO dto) {}

    @Override
    public void onTotemSelection(TotemSelectionDTO dto) {}

    @Override
    public void onLobbyError(ErrorDTO dto) {}
}
