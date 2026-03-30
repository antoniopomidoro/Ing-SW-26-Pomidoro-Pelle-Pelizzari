package it.polimi.ingsw.network;

import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Totem;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds lobby state before a match starts, including join order and totem assignments.
 */
public class PendingGame {
    private final String gameId;
    private final int requiredPlayers;
    private final List<Player> joinedPlayers = new ArrayList<>();

    public PendingGame(String gameId, int requiredPlayers) {
        this.gameId = gameId;
        this.requiredPlayers = requiredPlayers;
    }

    /**
     * Adds a player to the lobby and assigns the requested totem.
     *
     * @param playerName the player's nickname
     * @param requestedTotem the totem/color requested by the player
     * @return the assigned totem
     * @throws IllegalArgumentException if the nickname is null/blank or already used,
     *                                  or if the requested totem is null/occupied
     * @throws IllegalStateException if the lobby is full
     */
    public synchronized Totem addPlayer(String playerName, Totem requestedTotem) {
        if (playerName == null || playerName.isBlank()) {
            throw new IllegalArgumentException("Invalid nickname: null o blank");
        }
        if (isFull()) {
            throw new IllegalStateException("Lobby is full: " + gameId);
        }
        if (requestedTotem == null) {
            throw new IllegalArgumentException("Invalid totem: null");
        }

        for (Player player : joinedPlayers) {
            if (player.getNickname().equals(playerName)) {
                throw new IllegalArgumentException("Nickname not available: " + playerName);
            }
            if (player.getId().equals(requestedTotem)) {
                throw new IllegalArgumentException("Totem not available: " + requestedTotem);
            }
        }

        joinedPlayers.add(new Player(requestedTotem, playerName));
        return requestedTotem;
    }

    public synchronized boolean isFull() {
        return joinedPlayers.size() >= requiredPlayers;
    }

    /**
     * Creates the player list following the join order.
     *
     * @return list of {@link Player} with assigned totems and nicknames
     * @throws IllegalStateException if the lobby is not full
     */
    public synchronized List<Player> getJoinedPlayers() {
        if (!isFull()) {
            throw new IllegalStateException("Lobby non piena: " + joinedPlayers.size() + "/" + requiredPlayers);
        }
        return new ArrayList<>(joinedPlayers);
    }

    public String getGameId() {
        return gameId;
    }

    public int getRequiredPlayers() {
        return requiredPlayers;
    }

    public synchronized int getCurrentPlayerCount() {
        return joinedPlayers.size();
    }

}
