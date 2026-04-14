package it.polimi.ingsw.network;

import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.model.game.GameEvent;
import it.polimi.ingsw.model.game.GameStateObserver;
import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.network.dto.GameEventDTO;
import it.polimi.ingsw.network.dto.GameStateDTO;

/**
 * Base class for network-facing views that observe the game state and relay events to clients.
 */
public abstract class VirtualView implements GameStateObserver {
    protected Totem totem;
    protected GameController gameController;
    protected String gameId;
    /**
     * Assigns the totem associated with this view.
     *
     * @param totem the player's totem
     */
    public void setTotem(Totem totem) {
        this.totem = totem;
    }

    /**
     * Attaches the controller used to access the game state.
     *
     * @param controller the active game controller
     */
    public void setGameController(GameController controller) {
        this.gameController = controller;
    }

    /**
     * Returns the totem associated with this view.
     *
     * @return the player's totem
     */
    public Totem getTotem() {
        return totem;
    }

    /**
     * Receives a game event and forwards a serialized representation to the client.
     *
     * @param event event emitted by the game state
     */
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

    /**
     * Sends a full game-state snapshot to the client.
     *
     * @return {@code true} if the snapshot was sent, {@code false} if no controller is attached
     */
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
    /**
     * Sends a lobby progress update to the client.
     *
     * @param currentPlayers current number of connected players
     * @param requiredPlayers total players required to start the match
     */
    public void sendLobbyUpdate(int currentPlayers, int requiredPlayers) {
        GameEventDTO lobbyDto = new GameEventDTO(
                "LOBBY_UPDATE",
                this.totem,
                "Waiting for players... (" + currentPlayers + "/" + requiredPlayers + ")",
                null
        );

        sendToClient(lobbyDto);
    }

    /**
     * Transmits a DTO to the concrete client implementation.
     *
     * @param dto event payload to send
     */
    protected abstract void sendToClient(GameEventDTO dto);

    protected abstract void ping();

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }
}
