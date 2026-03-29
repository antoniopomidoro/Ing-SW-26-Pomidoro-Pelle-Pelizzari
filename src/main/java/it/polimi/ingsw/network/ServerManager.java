package it.polimi.ingsw.network;

import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.model.game.GameEvent;
import it.polimi.ingsw.model.game.GameState;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Totem;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerManager {
    private final Map<String, PendingGame> pendingGames = new ConcurrentHashMap<>();
    private final Map<String, GameController> activeGames = new ConcurrentHashMap<>();
    private final Map<String, Map<Totem, VirtualView>> viewRegistry = new ConcurrentHashMap<>();

    /**
     * Un giocatore chiede di creare o unirsi a una partita.
     * Se la lobby si riempie, crea il GameController e registra le VirtualView come observer.
     *
     * @param gameId          ID della stanza
     * @param playerName      nickname del giocatore
     * @param requiredPlayers quanti giocatori servono (usato solo alla creazione)
     * @param requestedTotem  totem/colore richiesto dal giocatore
     * @param view            la VirtualView del giocatore (Socket o RMI)
     * @return il GameController se la partita e' partita, null se e' ancora in attesa
     * @throws IllegalStateException se la partita e' gia' attiva o input non valido
     */
    public synchronized LobbyState joinGame(String gameId, String playerName, int requiredPlayers,
                                                Totem requestedTotem, VirtualView view) throws IOException {
        if (gameId == null || gameId.isBlank() || playerName == null || playerName.isBlank()
                || requestedTotem == null || view == null) {
            throw new IllegalArgumentException("invalid input");
        }


        if (activeGames.containsKey(gameId)) {
            GameController controller = activeGames.get(gameId);
            GameState state = controller.getGameState();
            Player returningPlayer = state.getPlayers().stream()
                    .filter(p -> p.getNickname().equals(playerName))
                    .findFirst()
                    .orElse(null);
            if (returningPlayer == null) {
                throw new IllegalStateException("Game already started " + gameId);
            }
            if (!returningPlayer.getId().equals(requestedTotem)) {
                throw new IllegalArgumentException("Wrong totem color");
            }
            if (returningPlayer.isConnected()) {
                throw new IllegalStateException("Player " + playerName + " already online");
            }
            state.reintegratePlayer(returningPlayer);
            viewRegistry.get(gameId).put(requestedTotem, view);
            view.setTotem(requestedTotem);
            view.setGameController(controller);
            state.addObserver(view);
            view.sendGameSnapshot();
            return LobbyState.REJOIN;
        }
        PendingGame pending = pendingGames.computeIfAbsent(gameId,
                id -> new PendingGame(id, requiredPlayers));

        Totem totem = pending.addPlayer(playerName, requestedTotem);

        viewRegistry.computeIfAbsent(gameId, id -> new ConcurrentHashMap<>()).put(totem, view);
        view.setTotem(totem);

        if (pending.isFull()) {
            List<Player> players = pending.createPlayers();
            GameController controller = new GameController(players);

            Map<Totem, VirtualView> views = viewRegistry.get(gameId);
            for (Map.Entry<Totem, VirtualView> entry : views.entrySet()) {
                VirtualView vv = entry.getValue();
                vv.setGameController(controller);
                controller.getGameState().addObserver(vv);
                vv.sendGameSnapshot();
            }

            activeGames.put(gameId, controller);
            pendingGames.remove(gameId);

            // TODO: gestire il caso in cui un client si disconnette durante la fase di attesa

            return LobbyState.STARTING_GAME;
        }
        Map<Totem, VirtualView> views = viewRegistry.get(gameId);
        for (Map.Entry<Totem, VirtualView> entry : views.entrySet()) {
            VirtualView vv = entry.getValue();
            vv.sendLobbyUpdate(pending.getCurrentPlayerCount(), pending.getRequiredPlayers());
        }
        return LobbyState.WAITING;
    }

    public GameController getGame(String gameId) {
        return activeGames.get(gameId);
    }

    public Map<String, PendingGame> getPendingGames() {
        return pendingGames;
    }

    public Map<String, GameController> getActiveGames() {
        return activeGames;
    }

    public Map<String, Map<Totem, VirtualView>> getViewRegistry() {
        return viewRegistry;
    }
}
