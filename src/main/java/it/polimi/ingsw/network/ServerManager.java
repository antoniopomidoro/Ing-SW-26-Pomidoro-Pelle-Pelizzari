package it.polimi.ingsw.network;

import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.model.game.GameEvent;
import it.polimi.ingsw.model.game.GameState;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.network.socket.SocketServer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private final NUDEqueue NUDEqueue;

    public ServerManager(){
        this(true);
    }

    ServerManager(boolean startBackgroundThreads){
        NUDEqueue = new NUDEqueue(this);
        if (startBackgroundThreads) {
            NUDEPinger pinger = new NUDEPinger(this);
            Thread pingerThread = new Thread(pinger, "Pinger");
            Thread NUDEqueueThread = new Thread(NUDEqueue, "NUDEqueue");
            NUDEqueueThread.start();
            pingerThread.start();
        }
        loadSavedGames();
    }
    /**
     * Generates a unique 6-digit code, verifying that there are no collisions.
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


        // Notify the creator that they are waiting
        view.sendLobbyUpdate(LobbyState.WAITING ,pending.getCurrentPlayerCount(), pending.getRequiredPlayers());
        System.out.println("Lobby created with ID: " + gameId);
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

        // 1. REJOIN CASE (The game is already in progress)
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
            view.sendLobbyUpdate(LobbyState.REJOIN);
            return LobbyState.REJOIN;
        }

        // 2. NORMAL JOIN CASE (The lobby is waiting)
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
                    vv.sendLobbyUpdate(LobbyState.STARTING_GAME);
                }

                activeGames.put(gameId, controller);
                pendingGames.remove(gameId);

                return LobbyState.STARTING_GAME;
            }

            // Notify all users in the waiting lobby
            Map<Totem, VirtualView> views = viewRegistry.get(gameId);
            for (Map.Entry<Totem, VirtualView> entry : views.entrySet()) {
                VirtualView vv = entry.getValue();
                vv.sendLobbyUpdate(LobbyState.WAITING ,pending.getCurrentPlayerCount(), pending.getRequiredPlayers());
            }
            return LobbyState.WAITING;
        }

        // 3. ERROR CASE (The lobby does not exist)
        throw new IllegalArgumentException("Room " + gameId + " does not exist!");
    }
    public synchronized boolean disconnectPlayer(VirtualView view) {
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

    public synchronized boolean loadSavedGames(){
        Path savesDir = Paths.get("saves");

        if (Files.notExists(savesDir)) {
            try {
                Files.createDirectories(savesDir);
                System.out.println("saves/ directory not found: created. No games to load.");
            } catch (IOException e) {
                System.err.println("Unable to create saves/ directory: " + e.getMessage());
                return false;
            }
            return true;
        }

        if (!Files.isDirectory(savesDir)) {
            System.err.println("The saves/ path exists but is not a directory.");
            return false;
        }

        File[] saveFiles = savesDir.toFile().listFiles((dir, name) -> name != null && name.endsWith(".json"));
        if (saveFiles == null) {
            System.out.println("Loaded 0 games successfully, 0 failed.");
            return true;
        }

        int loaded = 0;
        int failed = 0;

        for (File saveFile : saveFiles) {
            try {
                String fileName = saveFile.getName();
                String gameId = fileName.substring(0, fileName.length() - ".json".length());

                if (gameId.isBlank()) {
                    throw new IllegalStateException("Invalid save file name: " + fileName);
                }
                if (activeGames.containsKey(gameId)) {
                    throw new IllegalStateException("Duplicate GameId found in saves: " + gameId);
                }

                GameState restoredState = GameStatePersistence.load(gameId);

                GameController restoredController = new GameController(restoredState);
                activeGames.put(gameId, restoredController);
                viewRegistry.putIfAbsent(gameId, new ConcurrentHashMap<>());
                loaded++;
            } catch (Exception e) {
                failed++;
                System.err.println("Error loading save " + saveFile.getName() + ": " + e.getMessage());
            }
        }

        System.out.println("Loaded " + loaded + " games successfully, " + failed + " failed.");
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

    public NUDEqueue getQueue(){
        return NUDEqueue;
    }

}
