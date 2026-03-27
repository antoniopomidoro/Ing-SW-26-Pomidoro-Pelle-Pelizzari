package it.polimi.ingsw.network;

import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Totem;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PendingGame {
    private final String gameId;
    private final int requiredPlayers;
    private final List<String> joinOrder = new ArrayList<>();
    private final Map<String, Totem> totemByPlayer = new LinkedHashMap<>();
    private final EnumMap<Totem, String> playerByTotem = new EnumMap<>(Totem.class);

    public PendingGame(String gameId, int requiredPlayers) {
        this.gameId = gameId;
        this.requiredPlayers = requiredPlayers;
    }

    /**
     * Aggiunge un giocatore alla lobby assegnandogli il Totem richiesto.
     * @param playerName il nickname del giocatore
     * @param requestedTotem il colore/totem richiesto dal giocatore
     * @return il Totem assegnato
     * @throws IllegalArgumentException se il nickname e' null/blank o gia' presente
     *                                  oppure se il totem richiesto e' null/occupato
     * @throws IllegalStateException se la lobby e' piena
     */
    public synchronized Totem addPlayer(String playerName, Totem requestedTotem) {
        if (playerName == null || playerName.isBlank()) {
            throw new IllegalArgumentException("Nickname non valido: null o blank");
        }
        if (totemByPlayer.containsKey(playerName)) {
            throw new IllegalArgumentException("Nickname gia' in uso: " + playerName);
        }
        if (isFull()) {
            throw new IllegalStateException("La stanza e' piena: " + gameId);
        }
        if (requestedTotem == null) {
            throw new IllegalArgumentException("Totem richiesto non valido: null");
        }
        if (playerByTotem.containsKey(requestedTotem)) {
            throw new IllegalArgumentException("Totem gia' occupato: " + requestedTotem);
        }

        joinOrder.add(playerName);
        totemByPlayer.put(playerName, requestedTotem);
        playerByTotem.put(requestedTotem, playerName);
        return requestedTotem;
    }

    public synchronized boolean isFull() {
        return joinOrder.size() >= requiredPlayers;
    }

    /**
     * Crea la lista di Player dall'ordine di join.
     * @return lista di Player con Totem e nickname assegnati
     * @throws IllegalStateException se la lobby non e' piena
     */
    public synchronized List<Player> createPlayers() {
        if (!isFull()) {
            throw new IllegalStateException("Lobby non piena: " + joinOrder.size() + "/" + requiredPlayers);
        }
        return joinOrder.stream()
                .map(name -> new Player(totemByPlayer.get(name), name))
                .toList();
    }

    public String getGameId() {
        return gameId;
    }

    public int getRequiredPlayers() {
        return requiredPlayers;
    }

    public synchronized int getCurrentPlayerCount() {
        return joinOrder.size();
    }

    public synchronized Map<String, Totem> getAssignedTotems() {
        return new LinkedHashMap<>(totemByPlayer);
    }

    public synchronized Map<Totem, String> getPlayersByTotem() {
        return new EnumMap<>(playerByTotem);
    }
}
