package it.polimi.ingsw.network.dto;

import it.polimi.ingsw.model.player.Totem;

import java.io.Serializable;

public class LobbyRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String gameId;
    private String playerName;
    private int requiredPlayers;
    private Totem requestedTotem;

    public LobbyRequest() {
        // Costruttore vuoto richiesto dalla serializzazione.
    }

    public LobbyRequest(String gameId, String playerName, int requiredPlayers, Totem requestedTotem) {
        this.gameId = gameId;
        this.playerName = playerName;
        this.requiredPlayers = requiredPlayers;
        this.requestedTotem = requestedTotem;
    }

    public String getGameId() {
        return gameId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getRequiredPlayers() {
        return requiredPlayers;
    }

    public Totem getRequestedTotem() {
        return requestedTotem;
    }
}
