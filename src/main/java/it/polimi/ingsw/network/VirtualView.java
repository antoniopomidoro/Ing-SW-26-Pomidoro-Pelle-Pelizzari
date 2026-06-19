package it.polimi.ingsw.network;

import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.model.game.GameEvent;
import it.polimi.ingsw.model.game.GameStateObserver;
import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.network.dto.CountdownDTO;
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

    // volatile: scritti dal thread lobby, letti anche dai thread di gioco/transport (ping, log, eventi)
    protected volatile Totem totem;
    protected volatile GameController gameController;
    protected volatile String gameId;
    protected volatile String nickname;
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
                event.getType(),
                event.getCulprit() != null ? event.getCulprit().getId() : null,
                snapshot,
                event.getTriggerKey()
        );

        sendToClient(dto);
    }

    /**
     * Sends the abandonment countdown duration to this view's client. Emitted by
     * ServerManager to the single surviving player when the countdown starts.
     *
     * @param seconds seconds remaining before the auto-win triggers
     */
    public void sendCountdown(long seconds) {
        sendToClient(new CountdownDTO(seconds));
    }

    /**
     * Sends a lobby progress update to the client.
     *
     * @param lobbyState the current lobby state to report
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
    /**
     * Sends a lobby update carrying a full game-state snapshot (used for rejoin).
     *
     * @param lobbyState the current lobby state to report
     */
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

    /**
     * Sends a heartbeat to the client to detect disconnections. Implemented by
     * the concrete transport (socket/RMI).
     */
    protected abstract void ping();

    /**
     * Returns the id of the game this view belongs to.
     *
     * @return the game id, or null if not yet assigned
     */
    public String getGameId() {
        return gameId;
    }

    /**
     * Sets the id of the game this view belongs to.
     *
     * @param gameId the game id
     */
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
