package it.polimi.ingsw.network.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.view.visitorDTO.DTOVisitor;

/**
 * Serializable lobby request sent by clients.
 * Handles both the creation of a new lobby and joining an existing one.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LobbyRequest implements DTO {
    private static final long serialVersionUID = 1L;

    public enum Type {
        CREATE,
        ENTER_LOBBY,
        SELECT_TOTEM
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
     * Creates a request to enter an existing lobby before totem selection.
     */
    public static LobbyRequest enterLobby(String gameId, String playerName) {
        return new LobbyRequest(Type.ENTER_LOBBY, gameId, playerName, 0, null);
    }

    /**
     * Creates a request to confirm totem selection in a lobby the player already entered.
     */
    public static LobbyRequest selectTotem(String gameId, String playerName, Totem requestedTotem) {
        return new LobbyRequest(Type.SELECT_TOTEM, gameId, playerName, 0, requestedTotem);
    }

    // --- GETTERS ---

    public Type getType() { return type; }
    public String getGameId() { return gameId; }
    public String getPlayerName() { return playerName; }
    public int getRequiredPlayers() { return requiredPlayers; }
    public Totem getRequestedTotem() { return requestedTotem; }

    @Override
    public void accept(DTOVisitor visitor) {
        return;
    }
}
