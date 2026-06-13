package it.polimi.ingsw.network;

import it.polimi.ingsw.controller.Actions.Executor;
import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.model.game.GameEvent;
import it.polimi.ingsw.model.game.GameState;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.network.dto.ErrorDTO;
import it.polimi.ingsw.network.dto.TotemSelectionDTO;
import it.polimi.ingsw.network.socket.SocketServer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Coordinates lobby creation, player rejoin, and the lifecycle of active games.
 *
 * <p><b>Threading model</b>: lobby state ({@code pendingGames}, {@code pendingViews},
 * {@code viewRegistry}) is mutated only on the single lobby thread (via
 * {@link #submitToLobby}); each game's state is mutated only on its own queue
 * (via {@link GameSession#trySubmit}). No {@code synchronized} is needed: mutual
 * exclusion comes from the queues, and the concurrent maps only serve the
 * pinger's read-only traversal.</p>
 */
public class ServerManager {
    private static final String INVALID_INPUT_ENTER = "Invalid input for lobby entry";
    private static final String INVALID_INPUT_SELECT = "Invalid input for totem selection";
    private static final String INVALID_INPUT_CREATION = "Invalid input for creation";
    private static final String GAME_SAVE_EXTENSION = ".ser";

    private final Map<String, PendingGame> pendingGames = new ConcurrentHashMap<>();
    private final Map<String, GameSession> activeGames = new ConcurrentHashMap<>();
    private final Map<String, Map<Totem, VirtualView>> viewRegistry = new ConcurrentHashMap<>();
    private final Map<String, Map<String, VirtualView>> pendingViews = new ConcurrentHashMap<>();
    private final Set<VirtualView> rawClients = ConcurrentHashMap.newKeySet();
    private final Random random = new Random();
    private final ExecutorService lobbyExecutor =
            Executors.newSingleThreadExecutor(Thread.ofVirtual().name("lobby").factory());

    public ServerManager() {
        NUDEPinger pinger = new NUDEPinger(this);
        Thread pingerThread = new Thread(pinger, "Pinger");
        pingerThread.start();
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
     * Enqueues a lobby operation on the single lobby thread. Exceptions are
     * logged instead of silently disappearing inside the executor's Future.
     *
     * @param task the lobby operation to run
     */
    public void submitToLobby(Runnable task) {
        Objects.requireNonNull(task, "task");
        lobbyExecutor.submit(() -> {
            try {
                task.run();
            } catch (RuntimeException e) {
                System.err.println("[ServerManager] lobby command failed: " + e.getMessage());
            }
        });
    }

    /**
     * Routes a game command onto the queue of its own game, then executes it
     * there resolving the acting player from the totem.
     *
     * @param command the game command; carries its own gameId and totem
     * @param view    the sender's view, used to report routing errors
     */
    public void submitToGame(Executor command, VirtualView view) {
        Objects.requireNonNull(command, "command");
        Objects.requireNonNull(view, "view");
        String gameId = command.getIdGame();
        if (gameId == null || gameId.isBlank()) {
            view.sendError(new ErrorDTO(ErrorDTO.ErrorCode.INVALID_INPUT));
            return;
        }
        GameSession session = activeGames.get(gameId);
        if (session == null) {
            view.sendError(new ErrorDTO(ErrorDTO.ErrorCode.LOBBY_NOT_FOUND));
            return;
        }
        // closeGame can run between the activeGames.get above and this submit:
        // a closed game is indistinguishable from a missing one for the client
        if (!session.trySubmit(() -> runGameCommand(session, command))) {
            view.sendError(new ErrorDTO(ErrorDTO.ErrorCode.LOBBY_NOT_FOUND));
        }
    }

    private void runGameCommand(GameSession session, Executor command) {
        try {
            GameController controller = session.controller();
            Optional<Player> player = controller.getGameState().getPlayers().stream()
                    .filter(p -> p.getId().equals(command.getIdPlayer()))
                    .findFirst();
            if (player.isEmpty()) {
                System.err.println("[ServerManager] no player with totem "
                        + command.getIdPlayer() + " in game " + command.getIdGame());
                return;
            }
            command.execute(player.get(), controller);
        } catch (RuntimeException e) {
            System.err.println("[ServerManager] game command failed in game "
                    + command.getIdGame() + ": " + e.getMessage());
        }
    }

    /**
     * Removes a game from the active map, shuts down its command queue and
     * deletes its save file. Deliberately does NOT touch viewRegistry or the
     * clients: views stay registered (and pinged) so players can linger on the
     * end-game screen; their entries are cleaned up by disconnectPlayer when
     * they leave.
     *
     * @param gameId the game to close
     */
    public void closeGame(String gameId) {
        GameSession session = activeGames.remove(gameId);
        if (session == null) {
            return;
        }
        session.close();
        try {
            GameStatePersistence.delete(gameId);
        } catch (IllegalStateException e) {
            System.err.println("[ServerManager] could not delete save for game "
                    + gameId + ": " + e.getMessage());
        }
    }

    /**
     * Registers the observer that closes the game when it reaches its terminal
     * event. Must be registered AFTER the controller exists (so SaveObserver,
     * added in the GameController constructor, performs its last save BEFORE
     * this observer deletes the file — observers fire in registration order).
     *
     * @param controller the game controller
     * @param gameId     game identifier
     */
    private void registerEndGameCleanup(GameController controller, String gameId) {
        controller.getGameState().addObserver(event -> {
            if (event != null && event.getType() == GameEvent.Type.END_GAME_COMPLETED) {
                closeGame(gameId);
            }
        });
    }

    /**
     * Registers a player in a pending lobby before totem confirmation.
     *
     * @param gameId target lobby identifier
     * @param playerName player's nickname
     * @param view player's virtual view
     * @throws IllegalArgumentException when {@code view} is {@code null} and no client can be notified
     */
    public void enterLobby(String gameId, String playerName, VirtualView view) {
        if (view == null) {
            throw new IllegalArgumentException(INVALID_INPUT_ENTER);
        }
        if (gameId == null || gameId.isBlank() || playerName == null || playerName.isBlank()) {
            view.sendError(new ErrorDTO(ErrorDTO.ErrorCode.INVALID_INPUT));
            return;
        }

        GameSession session = activeGames.get(gameId);
        if (session != null) {
            GameController activeGame = session.controller();
            GameState state = activeGame.getGameState();
            Optional<Player> returningPlayer = state.getPlayers().stream()
                    .filter(player -> playerName.equals(player.getNickname()))
                    .findFirst();

            if (returningPlayer.isEmpty()) {
                view.sendError(new ErrorDTO(ErrorDTO.ErrorCode.NOT_A_PARTICIPANT));
                return;
            }

            Player player = returningPlayer.get();
            if (player.isConnected()) {
                view.sendError(new ErrorDTO(ErrorDTO.ErrorCode.ALREADY_CONNECTED));
                return;
            }

            viewRegistry.computeIfAbsent(gameId, id -> new ConcurrentHashMap<>()).put(player.getId(), view);
            view.setTotem(player.getId());
            view.setGameController(activeGame);
            view.setGameId(gameId);
            view.setNickname(playerName);
            // REJOIN snapshot + mutazione dello stato: entrambi sul thread della partita.
            // sendLobbyUpdate(REJOIN) legge il GameState (GameStateDTO.from) e non può
            // farlo dal thread lobby mentre la coda esegue comandi.
            boolean accepted = session.trySubmit(() -> {
                view.sendLobbyUpdate(LobbyState.REJOIN);
                if (!activeGame.reconnectPlayer(player, view)) {
                    // il rollback del registry torna sul thread lobby: l'invariante
                    // "lobby maps solo sul thread lobby" vale anche nel path di errore
                    submitToLobby(() -> viewRegistry.getOrDefault(gameId, Collections.emptyMap())
                            .remove(player.getId(), view));
                    view.sendError(new ErrorDTO(ErrorDTO.ErrorCode.RECONNECTION_FAILED));
                }
            });
            if (!accepted) {
                viewRegistry.getOrDefault(gameId, Collections.emptyMap()).remove(player.getId(), view);
                view.sendError(new ErrorDTO(ErrorDTO.ErrorCode.LOBBY_NOT_FOUND));
            }
            return;
        }

        PendingGame pending = pendingGames.get(gameId);
        if (pending == null) {
            view.sendError(new ErrorDTO(ErrorDTO.ErrorCode.LOBBY_NOT_FOUND));
            return;
        }

        if (pending.isFull()) {
            view.sendError(new ErrorDTO(ErrorDTO.ErrorCode.LOBBY_FULL));
            return;
        }

        try {
            pending.registerPending(playerName);
        } catch (IllegalArgumentException e) {
            view.sendError(new ErrorDTO(ErrorDTO.ErrorCode.NICKNAME_TAKEN));
            return;
        }

        pendingViews.computeIfAbsent(gameId, id -> new ConcurrentHashMap<>()).put(playerName, view);
        view.setGameId(gameId);
        view.setNickname(playerName);

        broadcastWaiting(gameId, pending);
        broadcastTotemSelection(gameId, pending);
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
    public LobbyState createGame(String playerName, int requiredPlayers,
                                          Totem requestedTotem, VirtualView view) {
        if (view == null) {
            throw new IllegalArgumentException(INVALID_INPUT_CREATION);
        }
        if (playerName == null || playerName.isBlank() || requestedTotem == null || requiredPlayers <= 1 || requiredPlayers > 5) {
            view.sendError(new ErrorDTO(ErrorDTO.ErrorCode.INVALID_INPUT));
            return LobbyState.WAITING;
        }

        String gameId = generateUniqueLobbyId();
        PendingGame pending = new PendingGame(gameId, requiredPlayers);
        pendingGames.put(gameId, pending);

        Totem totem = pending.addPlayer(playerName, requestedTotem);

        viewRegistry.computeIfAbsent(gameId, id -> new ConcurrentHashMap<>()).put(totem, view);
        view.setTotem(totem);
        view.setGameId(gameId);
        view.setNickname(playerName);

        broadcastTotemSelection(gameId, pending);
        broadcastWaiting(gameId, pending);
        System.out.println("Lobby created with ID: " + gameId);
        return LobbyState.WAITING;
    }

    /**
     * Confirms a player's totem selection in a pending lobby.
     *
     * @param gameId target lobby identifier
     * @param playerName player's nickname
     * @param requestedTotem requested totem
     * @param view player's virtual view
     * @throws IllegalArgumentException when {@code view} is {@code null} and no client can be notified
     */
    public void selectTotem(String gameId, String playerName,
                                         Totem requestedTotem, VirtualView view) {
        if (view == null) {
            throw new IllegalArgumentException(INVALID_INPUT_SELECT);
        }
        if (gameId == null || gameId.isBlank() || playerName == null || playerName.isBlank()
                || requestedTotem == null) {
            view.sendError(new ErrorDTO(ErrorDTO.ErrorCode.INVALID_INPUT));
            return;
        }

        PendingGame pending = pendingGames.get(gameId);
        if (pending == null) {
            view.sendError(new ErrorDTO(ErrorDTO.ErrorCode.LOBBY_NOT_FOUND));
            return;
        }

        if (pending.isFull() && !pending.isPending(playerName)) {
            view.sendError(new ErrorDTO(ErrorDTO.ErrorCode.LOBBY_FULL));
            return;
        }

        try {
            pending.addPlayer(playerName, requestedTotem);
        } catch (IllegalArgumentException e) {
            view.sendError(new ErrorDTO(ErrorDTO.ErrorCode.TOTEM_NOT_AVAILABLE));
            return;
        }

        VirtualView confirmedView = takePendingView(gameId, playerName, view);
        viewRegistry.computeIfAbsent(gameId, id -> new ConcurrentHashMap<>()).put(requestedTotem, confirmedView);
        confirmedView.setTotem(requestedTotem);
        confirmedView.setGameId(gameId);
        confirmedView.setNickname(playerName);

        broadcastTotemSelection(gameId, pending);

        if (!pending.isFull()) {
            broadcastWaiting(gameId, pending);
        }

        if (pending.isFull()) {
            List<Player> players = pending.getJoinedPlayers();
            try {
                GameController controller = new GameController(players, gameId);

                Map<Totem, VirtualView> views = viewRegistry.get(gameId);
                for (Map.Entry<Totem, VirtualView> entry : views.entrySet()) {
                    VirtualView vv = entry.getValue();
                    vv.setGameController(controller);
                    controller.getGameState().addObserver(vv);
                }

                for (VirtualView vv : views.values()) {
                    vv.sendLobbyUpdate(LobbyState.STARTING_GAME);
                }

                controller.start();

                activeGames.put(gameId, GameSession.create(controller, gameId));
                registerEndGameCleanup(controller, gameId);
                pendingGames.remove(gameId);
                pendingViews.remove(gameId);
            } catch (IllegalStateException e) {
                ErrorDTO gameStartFailed = new ErrorDTO(ErrorDTO.ErrorCode.GAME_START_FAILED);
                broadcastError(gameId, gameStartFailed);
                pendingGames.remove(gameId);
                closeGame(gameId);
                viewRegistry.remove(gameId);
                pendingViews.remove(gameId);
                return;
            }
        }
    }

    private VirtualView takePendingView(String gameId, String playerName, VirtualView fallbackView) {
        Map<String, VirtualView> gamePendingViews = pendingViews.get(gameId);
        if (gamePendingViews == null) {
            return fallbackView;
        }

        VirtualView pendingView = gamePendingViews.remove(playerName);
        if (gamePendingViews.isEmpty()) {
            pendingViews.remove(gameId);
        }

        return pendingView == null ? fallbackView : pendingView;
    }

    private void broadcastWaiting(String gameId, PendingGame pending) {
        int current = pending.getCurrentPlayerCount();
        int required = pending.getRequiredPlayers();
        viewRegistry.getOrDefault(gameId, Collections.emptyMap())
                .values().forEach(v -> v.sendLobbyUpdate(LobbyState.WAITING, current, required));
        pendingViews.getOrDefault(gameId, Collections.emptyMap())
                .values().forEach(v -> v.sendLobbyUpdate(LobbyState.WAITING, current, required));
    }

    private void broadcastTotemSelection(String gameId, PendingGame pending) {
        TotemSelectionDTO totemSelectionDTO = new TotemSelectionDTO(
                gameId,
                pending.getAvailableTotems(),
                pending.getTakenTotems()
        );

        Map<Totem, VirtualView> confirmedViews = viewRegistry.getOrDefault(gameId, Collections.emptyMap());
        for (VirtualView lobbyView : confirmedViews.values()) {
            lobbyView.sendTotemSelection(totemSelectionDTO);
        }

        Map<String, VirtualView> waitingViews = pendingViews.getOrDefault(gameId, Collections.emptyMap());
        for (VirtualView lobbyView : waitingViews.values()) {
            lobbyView.sendTotemSelection(totemSelectionDTO);
        }
    }

    private void broadcastError(String gameId, ErrorDTO error) {
        viewRegistry.getOrDefault(gameId, Collections.emptyMap())
                .values().forEach(v -> v.sendError(error));
        pendingViews.getOrDefault(gameId, Collections.emptyMap())
                .values().forEach(v -> v.sendError(error));
    }

    /**
     * Disconnects a player view. Asynchronous: the lobby-side cleanup runs on
     * the lobby thread, the game-state mutation on the game's own queue.
     *
     * @param view virtual view to disconnect
     */
    public void disconnectPlayer(VirtualView view) {
        if (view == null) {
            return;
        }
        rawClients.remove(view);               // set concorrente: sicuro sul thread chiamante
        submitToLobby(() -> doDisconnect(view));
    }

    /** Lobby-thread half of the disconnect. Idempotent: a second pass finds the maps already clean. */
    private void doDisconnect(VirtualView view) {
        String gameId = view.getGameId();
        if (gameId == null || gameId.isBlank()) {
            return;
        }

        // CASE A: player is in pendingViews (entered lobby but no totem yet)
        Map<String, VirtualView> waiting = pendingViews.get(gameId);
        if (waiting != null && waiting.containsValue(view)) {
            String playerName = view.getNickname();
            waiting.remove(playerName);
            if (waiting.isEmpty()) {
                pendingViews.remove(gameId);
            }

            PendingGame pending = pendingGames.get(gameId);
            if (pending == null) {
                return;
            }

            try {
                pending.removePending(playerName);
            } catch (IllegalArgumentException e) {
                return;
            }

            cleanupLobbyIfEmpty(gameId);
            return;
        }

        // CASE B: player confirmed totem in pending lobby
        GameSession session = activeGames.get(gameId);
        if (session == null) {
            Map<Totem, VirtualView> registry = viewRegistry.get(gameId);
            if (registry == null) {
                return;
            }

            Totem totem = view.getTotem();
            if (totem == null) {
                return;
            }

            registry.remove(totem);
            if (registry.isEmpty()) {
                viewRegistry.remove(gameId);
            }

            PendingGame pending = pendingGames.get(gameId);
            if (pending == null) {
                return;
            }

            try {
                pending.removePlayer(totem);
            } catch (IllegalArgumentException e) {
                return;
            }

            cleanupLobbyIfEmpty(gameId);
            if (pendingGames.containsKey(gameId)) {
                broadcastTotemSelection(gameId, pending);
            }
            return;
        }

        // CASE C: player is in an active game
        Map<Totem, VirtualView> registry = viewRegistry.get(gameId);
        if (registry == null) {
            return;
        }

        Totem totem = view.getTotem();
        if (totem == null) {
            return;
        }

        registry.remove(totem);
        // removeObserver E disconnectPlayer girano sul thread della partita:
        // la lista observer e il GameState si toccano solo da lì
        session.trySubmit(() -> {
            session.controller().getGameState().removeObserver(view);
            session.controller().disconnectPlayer(totem);
        });
    }

    private void cleanupLobbyIfEmpty(String gameId) {
        boolean noConfirmed = viewRegistry.getOrDefault(gameId, Collections.emptyMap()).isEmpty();
        boolean noWaiting = pendingViews.getOrDefault(gameId, Collections.emptyMap()).isEmpty();
        if (noConfirmed && noWaiting) {
            pendingGames.remove(gameId);
            viewRegistry.remove(gameId);
            pendingViews.remove(gameId);
        }
    }

    public boolean loadSavedGames(){
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

        File[] saveFiles = savesDir.toFile().listFiles((dir, name) -> name != null && name.endsWith(GAME_SAVE_EXTENSION));
        if (saveFiles == null) {
            System.out.println("Loaded 0 games successfully, 0 failed.");
            return true;
        }

        int loaded = 0;
        int failed = 0;
        List<String> loadedGameIds = new ArrayList<>();

        for (File saveFile : saveFiles) {
            try {
                String fileName = saveFile.getName();
                String gameId = fileName.substring(0, fileName.length() - GAME_SAVE_EXTENSION.length());

                if (gameId.isBlank()) {
                    throw new IllegalStateException("Invalid save file name: " + fileName);
                }
                if (activeGames.containsKey(gameId)) {
                    throw new IllegalStateException("Duplicate GameId found in saves: " + gameId);
                }

                GameState restoredState = GameStatePersistence.load(gameId);

                GameController restoredController = new GameController(restoredState);
                activeGames.put(gameId, GameSession.create(restoredController, gameId));
                registerEndGameCleanup(restoredController, gameId);
                viewRegistry.putIfAbsent(gameId, new ConcurrentHashMap<>());
                loaded++;
                loadedGameIds.add(gameId);
            } catch (Exception e) {
                failed++;
                System.err.println("Error loading save " + saveFile.getName() + ": " + e.getMessage());
            }
        }

        if (loadedGameIds.isEmpty()) {
            System.out.println("Loaded game IDs: none");
        } else {
            System.out.println("Loaded game IDs: " + loadedGameIds);
        }

        System.out.println("Loaded " + loaded + " games successfully, " + failed + " failed.");
        return true;
    }

    /**
     * Looks up an active game by id.
     *
     * @param gameId game identifier
     * @return the controller, or empty if no active game has that id
     */
    public Optional<GameController> getGame(String gameId) {
        return Optional.ofNullable(activeGames.get(gameId)).map(GameSession::controller);
    }

    public Map<String, PendingGame> getPendingGames() {
        return pendingGames;
    }

    public Map<String, Map<Totem, VirtualView>> getViewRegistry() {
        return viewRegistry;
    }

    /**
     * Pings all registered virtual views — both confirmed (viewRegistry) and pending (pendingViews).
     * Invoked by {@link NUDEPinger} to detect silent disconnections in every lobby phase.
     */
    public void registerRawClient(VirtualView view) {
        rawClients.add(view);
    }

    public void pingAllViews() {
        viewRegistry.values().forEach(gameViews -> gameViews.values().forEach(VirtualView::ping));
        pendingViews.values().forEach(gameViews -> gameViews.values().forEach(VirtualView::ping));
        rawClients.forEach(VirtualView::ping);
    }

}
