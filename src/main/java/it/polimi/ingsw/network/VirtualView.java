package it.polimi.ingsw.network;

import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.model.game.GameEvent;
import it.polimi.ingsw.model.game.GameStateObserver;
import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.network.dto.GameEventDTO;
import it.polimi.ingsw.network.dto.GameStateDTO;

public abstract class VirtualView implements GameStateObserver {
    protected Totem totem;
    protected GameController gameController;

    public void setTotem(Totem totem) {
        this.totem = totem;
    }

    public void setGameController(GameController controller) {
        this.gameController = controller;
    }

    public Totem getTotem() {
        return totem;
    }

    @Override
    public void onGameEvent(GameEvent event) {
        if (event == null) {
            return;
        }

        GameStateDTO snapshot = null;

        // Per SUCCESSFUL_ACTION: includi lo snapshot completo del GameState
        if (event.getType() == GameEvent.Type.SUCCESSFUL_ACTION && gameController != null) {
            snapshot = GameStateDTO.from(gameController.getGameState());
        }

        // TODO-NET-403 [privacy]: filtrare eventuali dati sensibili/non pubblici prima di inviare al singolo client.

        GameEventDTO dto = new GameEventDTO(
                event.getType() != null ? event.getType().name() : null,
                event.getCulprit() != null ? event.getCulprit().getId() : null,
                event.getMessage(),
                snapshot
        );

        sendToClient(dto);
    }

    public boolean sendGameSnapshot(){
        if (gameController == null) {
            return false;
        }
        GameStateDTO snapshot = GameStateDTO.from(gameController.getGameState());
        GameEventDTO syncDto = new GameEventDTO(
                "SYNC",
                null,
                "Game state synchronization",
                snapshot
        );
        sendToClient(syncDto);
        return true;
    }
    public void sendLobbyUpdate(int currentPlayers, int requiredPlayers) {
        GameEventDTO lobbyDto = new GameEventDTO(
                "LOBBY_UPDATE",
                this.totem,
                "Waiting for players... (" + currentPlayers + "/" + requiredPlayers + ")",
                null
        );

        sendToClient(lobbyDto);
    }
    protected abstract void sendToClient(GameEventDTO dto);

}
