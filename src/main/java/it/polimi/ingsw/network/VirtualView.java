package it.polimi.ingsw.network;

import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.model.game.GameEvent;
import it.polimi.ingsw.model.game.GameStateObserver;
import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.network.dto.DTO;
import it.polimi.ingsw.network.dto.ErrorDTO;
import it.polimi.ingsw.network.dto.GameEventDTO;
import it.polimi.ingsw.network.dto.GameStateDTO;
import it.polimi.ingsw.network.dto.LobbyUpdateDTO;
import it.polimi.ingsw.network.dto.TotemSelectionDTO;

/**
 * Base class for network-facing views that observe the game state and relay events to clients.
 */
public abstract class VirtualView implements GameStateObserver {
    private static final String EMPTY_NICKNAME = "";

    protected Totem totem;
    protected GameController gameController;
    protected String gameId;
    protected String nickname;
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

        // For each event that modifies the state: include the full GameState snapshot
        if (event.getType().requiresSave() && gameController != null) {
            snapshot = GameStateDTO.from(gameController.getGameState());
        }
        GameEventDTO dto = new GameEventDTO(
                event.getType() != null ? event.getType().name() : null,
                event.getCulprit() != null ? event.getCulprit().getId() : null,
                snapshot
        );

        sendToClient(dto);
    }
    /**
     * Sends a lobby progress update to the client.
     *
     * @param currentPlayers current number of connected players
     * @param requiredPlayers total players required to start the match
     */
    public void sendLobbyUpdate(LobbyState lobbyState, int currentPlayers, int requiredPlayers) {
        LobbyUpdateDTO lobbyDto = new LobbyUpdateDTO(
                lobbyState,
                gameId,
                currentPlayers,
                requiredPlayers
        );

        sendToClient(lobbyDto);
    }
    public void sendLobbyUpdate(LobbyState lobbyState) {
        GameStateDTO snapshot = GameStateDTO.from(gameController.getGameState());
        LobbyUpdateDTO lobbyDto = new LobbyUpdateDTO(
                lobbyState,
                gameId,
                snapshot
        );

        sendToClient(lobbyDto);
    }

    /**
     * Sends a structured error payload to the client.
     *
     * @param error error dto to deliver
     */
    public void sendError(ErrorDTO error) {
        sendToClient(error);
    }

    /**
     * Sends current totem-selection status to the client.
     *
     * @param totemSelection totem-selection payload
     */
    public void sendTotemSelection(TotemSelectionDTO totemSelection) {
        sendToClient(totemSelection);
    }

    /**
     * Transmits a DTO to the concrete client implementation.
     *
     * @param dto event payload to send
     */
    protected abstract void sendToClient(DTO dto);

    protected abstract void ping();

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    /**
     * Stores the nickname associated with this network view.
     *
     * @param nickname player's nickname
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * Returns the nickname associated with this view.
     *
     * @return non-null nickname, or empty string when unset
     */
    public String getNickname() {
        return nickname == null ? EMPTY_NICKNAME : nickname;
    }
}
