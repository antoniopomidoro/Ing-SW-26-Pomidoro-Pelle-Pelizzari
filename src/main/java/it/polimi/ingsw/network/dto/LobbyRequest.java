package it.polimi.ingsw.network.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import it.polimi.ingsw.model.player.Totem;

import java.io.Serializable;

/**
 * Serializable lobby request sent by clients.
 * Handles both the creation of a new lobby and joining an existing one.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LobbyRequest implements DTO {
    private static final long serialVersionUID = 1L;

    public enum Type {
        CREATE,
        JOIN
    }

    private Type type;
    private String gameId;          // Used ONLY if type == JOIN
    private String playerName;
    private int requiredPlayers;    // Used ONLY if type == CREATE
    private Totem requestedTotem;

    /**
     * Empty constructor required by serialization frameworks (Jackson / RMI).
     */
    public LobbyRequest() {
    }

    /**
     * Private constructor to force the use of Factory Methods.
     */
    private LobbyRequest(Type type, String gameId, String playerName, int requiredPlayers, Totem requestedTotem) {
        this.type = type;
        this.gameId = gameId;
        this.playerName = playerName;
        this.requiredPlayers = requiredPlayers;
        this.requestedTotem = requestedTotem;
    }

    // --- STATIC FACTORY METHODS ---

    /**
     * Creates a request to form a brand-new lobby.
     * The gameId is deliberately null because the Server will generate it.
     */
    public static LobbyRequest createNewLobby(String playerName, int requiredPlayers, Totem requestedTotem) {
        return new LobbyRequest(Type.CREATE, null, playerName, requiredPlayers, requestedTotem);
    }

    /**
     * Creates a request to join an existing lobby.
     * The requiredPlayers is deliberately 0 because the room size is already set.
     */
    public static LobbyRequest joinExistingLobby(String gameId, String playerName, Totem requestedTotem) {
        return new LobbyRequest(Type.JOIN, gameId, playerName, 0, requestedTotem);
    }

    // --- GETTERS ---

    public Type getType() { return type; }
    public String getGameId() { return gameId; }
    public String getPlayerName() { return playerName; }
    public int getRequiredPlayers() { return requiredPlayers; }
    public Totem getRequestedTotem() { return requestedTotem; }
}
