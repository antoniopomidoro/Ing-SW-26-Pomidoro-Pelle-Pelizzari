package it.polimi.ingsw.network;

import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.model.game.GameEvent;
import it.polimi.ingsw.model.game.GameState;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Totem;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Coordinates lobby creation, player rejoin, and the lifecycle of active games.
 */
public class ServerManager {
    private final Map<String, PendingGame> pendingGames = new ConcurrentHashMap<>();
    private final Map<String, GameController> activeGames = new ConcurrentHashMap<>();
    private final Map<String, Map<Totem, VirtualView>> viewRegistry = new ConcurrentHashMap<>();
    private final Random random = new Random();
    private final NUDEPinger pinger = new NUDEPinger(this);

    public ServerManager(){
        pinger.start();
        loadSavedGames();
    }
    /**
     * Genera un codice a 6 cifre univoco, verificando che non ci siano collisioni.
     */
    private String generateUniqueLobbyId() {
        String newId;
        do {
            newId = String.format("%06d", random.nextInt(1000000));
        } while (activeGames.containsKey(newId) || pendingGames.containsKey(newId));
        return newId;
    }

    /**
     * Creates a brand new game lobby.
     *
     * @param playerName player's nickname (the host)
     * @param requiredPlayers number of players required to start the game
     * @param requestedTotem totem/color requested by the player
     * @param view the player's {@link VirtualView}
     * @return The generated 6-digit gameId so the network layer can send it to the client
     */
    public synchronized LobbyState createGame(String playerName, int requiredPlayers,
                                          Totem requestedTotem, VirtualView view) {
        if (playerName == null || playerName.isBlank() || requestedTotem == null || view == null || requiredPlayers <= 1) {
            throw new IllegalArgumentException("Invalid input for creation");
        }

        String gameId = generateUniqueLobbyId();
        PendingGame pending = new PendingGame(gameId, requiredPlayers);
        pendingGames.put(gameId, pending);

        Totem totem = pending.addPlayer(playerName, requestedTotem);

        viewRegistry.computeIfAbsent(gameId, id -> new ConcurrentHashMap<>()).put(totem, view);
        view.setTotem(totem);
        view.setGameId(gameId);


        // Notifica il creatore che è in attesa
        view.sendLobbyUpdate(pending.getCurrentPlayerCount(), pending.getRequiredPlayers());

        return LobbyState.WAITING;
    }

    /**
     * A player requests to join an existing game (either pending or active for rejoin).
     *
     * @param gameId ID of the room to join
     * @param playerName player's nickname
     * @param requestedTotem totem/color requested by the player
     * @param view the player's {@link VirtualView}
     * @return {@link LobbyState#STARTING_GAME} when the match starts, {@link LobbyState#WAITING}
     * while the lobby is filling, or {@link LobbyState#REJOIN} for a returning player
     * @throws IllegalArgumentException if lobby doesn't exist or input is invalid
     * @throws IllegalStateException if the game is already active and player cannot rejoin
     * @throws IOException if the game initialization fails
     */
    public synchronized LobbyState joinGame(String gameId, String playerName,
                                            Totem requestedTotem, VirtualView view) throws IOException {
        if (gameId == null || gameId.isBlank() || playerName == null || playerName.isBlank()
                || requestedTotem == null || view == null) {
            throw new IllegalArgumentException("Invalid input for join");
        }

        // 1. CASO REJOIN (La partita è già in corso)
        if (activeGames.containsKey(gameId)) {
            GameController controller = activeGames.get(gameId);
            GameState state = controller.getGameState();
            Player returningPlayer = state.getPlayers().stream()
                    .filter(p -> p.getNickname().equals(playerName))
                    .findFirst()
                    .orElse(null);

            if (returningPlayer == null) {
                throw new IllegalStateException("Game already started and you are not a participant: " + gameId);
            }
            if (!returningPlayer.getId().equals(requestedTotem)) {
                throw new IllegalArgumentException("Wrong totem color for rejoin");
            }
            if (returningPlayer.isConnected()) {
                throw new IllegalStateException("Player " + playerName + " is already online");
            }

            state.reintegratePlayer(returningPlayer);
            viewRegistry.get(gameId).put(requestedTotem, view);
            view.setTotem(requestedTotem);
            view.setGameController(controller);
            view.setGameId(gameId);
            state.addObserver(view);
            view.sendGameSnapshot();
            return LobbyState.REJOIN;
        }

        // 2. CASO JOIN NORMALE (La lobby è in attesa)
        if (pendingGames.containsKey(gameId)) {
            PendingGame pending = pendingGames.get(gameId);
            Totem totem = pending.addPlayer(playerName, requestedTotem);

            viewRegistry.computeIfAbsent(gameId, id -> new ConcurrentHashMap<>()).put(totem, view);
            view.setTotem(totem);
            view.setGameId(gameId);

            if (pending.isFull()) {
                List<Player> players = pending.getJoinedPlayers();
                GameController controller = new GameController(players, gameId);

                Map<Totem, VirtualView> views = viewRegistry.get(gameId);
                for (Map.Entry<Totem, VirtualView> entry : views.entrySet()) {
                    VirtualView vv = entry.getValue();
                    vv.setGameController(controller);
                    controller.getGameState().addObserver(vv);
                    vv.sendGameSnapshot();
                }

                activeGames.put(gameId, controller);
                pendingGames.remove(gameId);

                return LobbyState.STARTING_GAME;
            }

            // Notifica tutti gli utenti nella lobby in attesa
            Map<Totem, VirtualView> views = viewRegistry.get(gameId);
            for (Map.Entry<Totem, VirtualView> entry : views.entrySet()) {
                VirtualView vv = entry.getValue();
                vv.sendLobbyUpdate(pending.getCurrentPlayerCount(), pending.getRequiredPlayers());
            }
            return LobbyState.WAITING;
        }

        // 3. CASO ERRORE (La lobby non esiste)
        throw new IllegalArgumentException("Stanza " + gameId + " inesistente!");
    }
    public boolean disconnectPlayer(VirtualView view) {
        if (view == null) return false;
        if (viewRegistry.get(view.gameId) == null) return false;
        VirtualView deadView = viewRegistry.get(view.gameId).remove(view.getTotem());
        if (deadView == null) return false;
        GameController controller = activeGames.get(view.gameId);
        if (controller == null) return false;
        controller.getGameState().removeObserver(deadView);
        controller.disconnectPlayer(view.getTotem());
        return true;
    }

    public boolean loadSavedGames(){
        //TODO: add implementation
        return true;
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