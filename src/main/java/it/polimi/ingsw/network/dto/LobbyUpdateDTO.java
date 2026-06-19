package it.polimi.ingsw.network.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.network.LobbyState;
import it.polimi.ingsw.view.visitorDTO.DTOVisitor;

/**
 * DTO carrying a lobby progress update: the current lobby state, the game id,
 * an optional game-state snapshot (for rejoin) and the player counts.
 */
public class LobbyUpdateDTO implements DTO {
    private LobbyState lobbyState;
    private String idGame;
    private GameStateDTO snapshot;
    private int currentPlayers;
    private int requiredPlayers;

    /**
     * Full constructor used by Jackson deserialization.
     *
     * @param lobbyState      the current lobby state
     * @param idGame          the game identifier
     * @param snapshot        the game-state snapshot, or null
     * @param currentPlayers  the current number of players
     * @param requiredPlayers the number of players required to start
     */
    @JsonCreator
    public LobbyUpdateDTO(
            @JsonProperty("lobbyState") LobbyState lobbyState,
            @JsonProperty("idGame") String idGame,
            @JsonProperty("snapshot") GameStateDTO snapshot,
            @JsonProperty("currentPlayers") int currentPlayers,
            @JsonProperty("requiredPlayers") int requiredPlayers
    ) {
        this.lobbyState = lobbyState;
        this.idGame = idGame;
        this.snapshot = snapshot;
        this.currentPlayers = currentPlayers;
        this.requiredPlayers = requiredPlayers;
    }

    /**
     * Builds a lobby update carrying only the player counts (no snapshot).
     *
     * @param lobbyState      the current lobby state
     * @param idGame          the game identifier
     * @param currentPlayers  the current number of players
     * @param requiredPlayers the number of players required to start
     */
    public LobbyUpdateDTO(
            LobbyState lobbyState,
            String idGame,
            int currentPlayers,
            int requiredPlayers
    ) {
        this.lobbyState = lobbyState;
        this.idGame = idGame;
        this.currentPlayers = currentPlayers;
        this.requiredPlayers = requiredPlayers;
    }

    /**
     * Builds a lobby update carrying a game-state snapshot (used for rejoin).
     *
     * @param lobbyState the current lobby state
     * @param idGame     the game identifier
     * @param snapshot   the game-state snapshot
     */
    public LobbyUpdateDTO(
            LobbyState lobbyState,
            String idGame,
            GameStateDTO snapshot
    ) {
        this.lobbyState = lobbyState;
        this.idGame = idGame;
        this.snapshot = snapshot;
    }

    /** @return the current lobby state. */
    public LobbyState getLobbyState() {
        return lobbyState;
    }

    /** @return the game identifier. */
    public String getIdGame() {
        return idGame;
    }

    /** @return the game-state snapshot, or null if none was attached. */
    public GameStateDTO getSnapshot() {
        return snapshot;
    }

    /** @return the current number of players in the lobby. */
    public int getCurrentPlayers() {
        return currentPlayers;
    }

    /** @return the number of players required to start. */
    public int getRequiredPlayers() {
        return requiredPlayers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(DTOVisitor visitor) {
        visitor.visit(this);
    }
}
