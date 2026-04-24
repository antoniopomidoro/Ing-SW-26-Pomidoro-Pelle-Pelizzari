package it.polimi.ingsw.network;

import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Totem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Holds lobby state before a match starts, including join order and totem assignments.
 */
public class PendingGame {
    private static final String INVALID_NICKNAME_MESSAGE = "Invalid nickname: null or blank";
    private static final String DUPLICATE_NICKNAME_MESSAGE_PREFIX = "Nickname not available: ";
    private static final String LOBBY_FULL_MESSAGE_PREFIX = "Lobby is full: ";
    private static final String INVALID_TOTEM_MESSAGE = "Invalid totem: null";
    private static final String TOTEM_NOT_AVAILABLE_MESSAGE_PREFIX = "Totem not available: ";
    private static final String PENDING_PLAYER_NOT_FOUND_MESSAGE_PREFIX = "Pending player not found: ";
    private static final String PLAYER_WITH_TOTEM_NOT_FOUND_MESSAGE_PREFIX = "Player with totem not found: ";

    private final String gameId;
    private final int requiredPlayers;
    private final List<Player> joinedPlayers = new ArrayList<>();
    private final Set<String> pendingNicknames = new HashSet<>();

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
        validatePlayerName(playerName);
        if (isFull()) {
            throw new IllegalStateException(LOBBY_FULL_MESSAGE_PREFIX + gameId);
        }
        if (requestedTotem == null) {
            throw new IllegalArgumentException(INVALID_TOTEM_MESSAGE);
        }

        for (Player player : joinedPlayers) {
            if (player.getNickname().equals(playerName)) {
                throw new IllegalArgumentException(DUPLICATE_NICKNAME_MESSAGE_PREFIX + playerName);
            }
            if (player.getId().equals(requestedTotem)) {
                throw new IllegalArgumentException(TOTEM_NOT_AVAILABLE_MESSAGE_PREFIX + requestedTotem);
            }
        }

        joinedPlayers.add(new Player(requestedTotem, playerName));
        pendingNicknames.remove(playerName);
        return requestedTotem;
    }

    /**
     * Registers a nickname as pending totem selection.
     *
     * @param playerName nickname to register
     */
    public synchronized void registerPending(String playerName) {
        validatePlayerName(playerName);

        if (pendingNicknames.contains(playerName)) {
            throw new IllegalArgumentException(DUPLICATE_NICKNAME_MESSAGE_PREFIX + playerName);
        }

        for (Player player : joinedPlayers) {
            if (player.getNickname().equals(playerName)) {
                throw new IllegalArgumentException(DUPLICATE_NICKNAME_MESSAGE_PREFIX + playerName);
            }
        }

        pendingNicknames.add(playerName);
    }

    /**
     * Removes a player nickname waiting for totem confirmation.
     *
     * @param playerName nickname to remove from pending registrations
     * @throws IllegalArgumentException if nickname is null/blank or not currently pending
     */
    public synchronized void removePending(String playerName) {
        validatePlayerName(playerName);

        if (!pendingNicknames.remove(playerName)) {
            throw new IllegalArgumentException(PENDING_PLAYER_NOT_FOUND_MESSAGE_PREFIX + playerName);
        }
    }

    /**
     * Removes a confirmed player by totem and frees that totem for selection again.
     *
     * @param totem totem assigned to the player that must be removed
     * @throws IllegalArgumentException if totem is null or no joined player owns it
     */
    public synchronized void removePlayer(Totem totem) {
        if (totem == null) {
            throw new IllegalArgumentException(INVALID_TOTEM_MESSAGE);
        }

        boolean removed = joinedPlayers.removeIf(player -> player.getId().equals(totem));
        if (!removed) {
            throw new IllegalArgumentException(PLAYER_WITH_TOTEM_NOT_FOUND_MESSAGE_PREFIX + totem);
        }
    }

    public synchronized boolean isFull() {
        return joinedPlayers.size() + pendingNicknames.size()>= requiredPlayers;
    }

    /**
     * Creates the player list following the join order.
     *
     * @return list of {@link Player} with assigned totems and nicknames
     * @throws IllegalStateException if the lobby is not full
     */
    public synchronized List<Player> getJoinedPlayers() {
        if (!isFull()) {
            throw new IllegalStateException("Lobby not full: " + joinedPlayers.size() + "/" + requiredPlayers);
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

    /**
     * Returns all totems that are still selectable in this lobby.
     *
     * @return non-null set of currently available totems
     */
    public synchronized Set<Totem> getAvailableTotems() {
        Set<Totem> availableTotems = EnumSet.allOf(Totem.class);
        for (Player player : joinedPlayers) {
            availableTotems.remove(player.getId());
        }
        return Collections.unmodifiableSet(availableTotems);
    }

    /**
     * Returns selected totems with the nickname that currently owns each one.
     *
     * @return non-null map totem to nickname for joined players
     */
    public synchronized Map<Totem, String> getTakenTotems() {
        Map<Totem, String> takenTotems = new EnumMap<>(Totem.class);
        for (Player player : joinedPlayers) {
            takenTotems.put(player.getId(), player.getNickname());
        }
        return Collections.unmodifiableMap(takenTotems);
    }

    private void validatePlayerName(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            throw new IllegalArgumentException(INVALID_NICKNAME_MESSAGE);
        }
    }

}
