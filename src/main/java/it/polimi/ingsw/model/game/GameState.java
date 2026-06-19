package it.polimi.ingsw.model.game;
import it.polimi.ingsw.controller.GameConfig;
import it.polimi.ingsw.model.board.Board;
import it.polimi.ingsw.model.board.Tile;
import it.polimi.ingsw.model.cards.Decks;
import it.polimi.ingsw.model.game.StatePhases.GamePhaseBehavior;
import it.polimi.ingsw.model.game.StatePhases.IllegalMoveException;
import it.polimi.ingsw.model.game.StatePhases.SetupPhase;
import it.polimi.ingsw.model.player.Player;

import java.io.Serializable;
import java.util.*;
import java.util.List;
import it.polimi.ingsw.model.cards.Building;

/**
 * GameState: The root aggregate of the game model and the Context of the State Pattern.
 * It manages the lifecycle of Players, Board, and Deck, and delegates
 * phase-specific logic to {@link GamePhaseBehavior} implementations.
 */
public class GameState implements Serializable {
    // --- Core game fields ---
    private GameConfig config;
    private Age age;
    private int turn;
    private GamePhaseBehavior currentPhase;
    private List<Player> players;
    private Board board;
    private Decks deck;
    private List<Player> turnOrder;
    private List<Player> orderTileOrder;
    private transient List<GameStateObserver> observers = new ArrayList<>();
    private String gameId;

    // --- State Pattern support fields ---
    private int currentTileIndex;
    private int currentPlayerIndex = 0;
    private int currentPlayerOrderIndex = 0;
    private int extraIndex = 0;

    /** Default constructor. */
    public GameState() {
        this.players = new ArrayList<>();
        this.turnOrder = new ArrayList<>();
        this.orderTileOrder = new ArrayList<>();
        this.observers = new ArrayList<>();
    }

    /**
     * Constructor: Initializes the game environment.
     * @param players The list of players provided from the login handler.
     * @param config The game configuration object.
     * @param board The initial board instance.
     * @param deck The decks manager.
     */
    public GameState(List<Player> players, GameConfig config, Board board, Decks deck, String gameId) {
        this.gameId = gameId;
        this.age = Age.AGE_1;
        this.turn = 1;
        this.currentTileIndex = 0;
        this.config = config;
        this.board = board;
        this.deck = deck;
        this.players = new ArrayList<>(players);
        this.turnOrder = new ArrayList<>();
        this.orderTileOrder = new ArrayList<>(this.players);
        this.observers = new ArrayList<>();
        restorePhase(new SetupPhase());
    }

    /**
     * Restores the phase without calling execute().
     * Used during save/load to avoid side effects.
     */
    public void restorePhase(GamePhaseBehavior phase) {
        this.currentPhase = phase;
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.observers = new ArrayList<>();
    }

    // ==========================================
    //  Getters & Setters
    // ==========================================

    /** @return The current phase behavior (State Pattern). */
    public GamePhaseBehavior getCurrentPhase() {
        return this.currentPhase;
    }

    /** @return The current Age of the game (Age I, II, or III). */
    public Age getAge() {
        return this.age;
    }

    /** @return The current round number. */
    public int getTurn() {
        return this.turn;
    }

    /** @return The list of all players. */
    public List<Player> getPlayers() {
        return this.players;
    }

    /** @return The game board. */
    public Board getBoard() {
        return this.board;
    }

    /** @return The game decks. */
    public Decks getDeck() {
        return this.deck;
    }

    /** @return The list of players sorted by their current turn order. */
    public List<Player> getTurnOrder() {
        return this.turnOrder;
    }

    /** @return The list of players sorted by their current order-tile order. */
    public List<Player> getOrderTileOrder() {
        return this.orderTileOrder;
    }

    /** @return The current player in turn order, or null if unavailable. */
    public Player getCurrentTurnOrderPlayer() {
        if (turnOrder.isEmpty() || currentPlayerIndex >= turnOrder.size()) return null;
        return turnOrder.get(currentPlayerIndex);
    }
    /** @return The current player in order-tile order, or null if unavailable. */
    public Player getCurrentOrderTileOrderPlayer() {
        if (orderTileOrder.isEmpty() || currentPlayerOrderIndex >= orderTileOrder.size()) return null;
        return this.orderTileOrder.get(currentPlayerOrderIndex);
    }

    /** @return The current tile index during tile scanning in TurnPhase. */
    public int getCurrentTileIndex() {
        return this.currentTileIndex;
    }

    /** @return The unique identifier of this game. */
    public String getGameId() {
        return gameId;
    }

    /**
     * Updates the game Age.
     * @param age The new Age to set.
     * @return true if the update was successful and the input was valid.
     */
    public boolean setAge(Age age) {
        if (age == null) return false;
        this.age = age;
        return true;
    }

    /**
     * Updates the current turn count.
     * @param turn The turn number to set.
     * @return true if the turn is valid (positive).
     */
    public boolean setTurn(int turn) {
        if (turn <= 0) return false;
        this.turn = turn;
        return true;
    }

    /**
     * Switches the game to a new phase and auto-starts it.
     *
     * @param newPhase The new phase behavior.
     * @return true if the phase is accepted and its execute(...) completes successfully.
     */
    public boolean setPhase(GamePhaseBehavior newPhase) {
        if (newPhase == null) {
            return false;
        }
        this.currentPhase = newPhase;
        // Autostart the phase as soon as it is set!
        return this.currentPhase.execute(this);
    }

    /**
     * Sets the current tile index for tile scanning in TurnPhase.
     * @param index The tile index.
     * @return true if the operation was successful.
     */
    public boolean setCurrentTileIndex(int index) {
        if (index < 0) {
            return false;
        }
        this.currentTileIndex = index;
        return true;
    }

    /**
     * Advances to the next player in the order-tile sequence.
     * @return true if there are more players in the sequence, false if it looped back to the beginning.
     */
    public boolean nextPlayerInTurnOrderTile() {
        while (true) {
            currentPlayerOrderIndex++;

            if (currentPlayerOrderIndex >= orderTileOrder.size()) {
                currentPlayerOrderIndex = 0;
                return false;
            }

            if (getCurrentOrderTileOrderPlayer().isConnected()) {
                return true;
            }

        }
    }

    /**
     * Advances to the next player in the standard turn order.
     * @return true if there are more players in the turn order, false if it looped back to the beginning.
     */
    public boolean nextPlayerInTurnOrder(){
        currentPlayerIndex++;
        if(currentPlayerIndex >= turnOrder.size()){
            currentPlayerIndex = 0;
            return false;
        }
        return true;
    }

    /**
     * @return The extra index value used for game-specific state tracking.
     */
    public int getExtraIndex() {
        return extraIndex;
    }

    /**
     * Sets the extra index value.
     * @param extraIndex the non-negative index to set.
     * @return true if the update was successful, false if the value was negative.
     */
    public boolean setExtraIndex(int extraIndex) {
        if (extraIndex < 0) {
            return false;
        }
        this.extraIndex = extraIndex;
        return true;
    }

    /**
     * @return The game configuration currently active.
     */
    public GameConfig getConfig() {
        return config;
    }

    /**
     * Updates the turn order
     * @return true if successful.
     */
    public boolean updateTurnOrder(){
        turnOrder.clear();
        for (Tile t : board.getTiles().getTiles()){
            if (t.isOccupied() && t.getOccupier() != null && !turnOrder.contains(t.getOccupier())){
                turnOrder.add(t.getOccupier());
            }
        }
        return true;
    }

    /**
     * Sets a completely new sequence for the order-tile order.
     * @param generatedOrder A list representing the new order.
     * @return true if successful, false if the list is null or empty.
     */
    public boolean setOrderTileOrder(List<Player> generatedOrder) {
        if (generatedOrder == null || generatedOrder.isEmpty()) {
            return false;
        }

        this.orderTileOrder.clear();
        // Null entries are skipped so a gap in the order list never causes an NPE.
        generatedOrder.stream()
                .filter(p -> p != null && p.isConnected())
                .forEach(this.orderTileOrder::add);
        generatedOrder.stream()
                .filter(p -> p != null && !p.isConnected())
                .forEach(this.orderTileOrder::add);

        return true;
    }

    /**
     * Clears the current turn order list.
     * @return true to indicate success.
     */
    public boolean clearTurnOrder() {
        this.turnOrder.clear();
        return true;
    }

    // ==========================================
    //  Public accessors for save/load
    // ==========================================

    /** @return The current player index in turnOrder. */
    public int getCurrentPlayerIndex() { return currentPlayerIndex; }

    /** @return The current player index in orderTileOrder. */
    public int getCurrentPlayerOrderIndex() { return currentPlayerOrderIndex; }

    /** Restores the configuration (save/load). @param config the configuration to set. */
    public void setConfig(GameConfig config) { this.config = config; }
    /** Restores the player list (save/load). @param players the players to set. */
    public void setPlayers(List<Player> players) { this.players = new ArrayList<>(players); }
    /** Restores the board (save/load). @param board the board to set. */
    public void setBoard(Board board) { this.board = board; }
    /** Restores the decks (save/load). @param deck the decks to set. */
    public void setDeck(Decks deck) { this.deck = deck; }
    /** Restores the turn order (save/load). @param turnOrder the turn order to set. */
    public void setTurnOrder(List<Player> turnOrder) { this.turnOrder = new ArrayList<>(turnOrder); }
    /** Restores orderTileOrder directly, without connected-first sorting. @param order the order to set. */
    public void restoreOrderTileOrder(List<Player> order) { this.orderTileOrder = new ArrayList<>(order); }
    /** Restores the game id (save/load). @param gameId the game id to set. */
    public void setGameId(String gameId) { this.gameId = gameId; }
    /** Restores the current turn-order player index (save/load). @param idx the index to set. */
    public void setCurrentPlayerIndex(int idx) { this.currentPlayerIndex = idx; }
    /** Restores the current order-tile player index (save/load). @param idx the index to set. */
    public void setCurrentPlayerOrderIndex(int idx) { this.currentPlayerOrderIndex = idx; }

    /**
     * Registers an observer for {@link GameEvent} notifications.
     *
     * @param observer observer callback; null values are ignored
     */
    public void addObserver(GameStateObserver observer) {
        if (observer != null) observers.add(observer);
    }

    /**
     * Publishes an event to all registered observers.
     *
     * @param event event to notify; null is ignored
     */
    public void raiseEvent(GameEvent event) {
        if (event == null) return;
        observers.forEach(o -> o.onGameEvent(event));
    }

    // ==========================================
    //  Phase Delegation API
    // ==========================================

    /**
     * Checks if a phase behavior is currently active.
     * @return true if a phase is assigned, false otherwise.
     */
    private boolean hasCurrentPhase() {
        return currentPhase != null;
    }

    /** Minimum number of connected players required for a move to be legal. */
    private static final int MIN_PLAYERS_TO_PLAY = 2;

    /**
     * Ensures the game has enough connected players to allow a move: a single
     * connected player cannot play alone. When violated, raises
     * {@link GameEvent.Type#INSUFFICIENT_PLAYERS} and throws, so the controller
     * rejects the move (returns {@code false}).
     *
     * @param culprit the player attempting the move
     * @throws IllegalMoveException if fewer than {@value #MIN_PLAYERS_TO_PLAY}
     *                              players are connected
     */
    private void requireEnoughConnectedPlayers(Player culprit) {
        long connected = players.stream().filter(Player::isConnected).count();
        if (connected < MIN_PLAYERS_TO_PLAY) {
            raiseEvent(new GameEvent(GameEvent.Type.INSUFFICIENT_PLAYERS, culprit));
            throw new IllegalMoveException(
                    "At least " + MIN_PLAYERS_TO_PLAY + " connected players are required to play.");
        }
    }

    /**
     * Delegates the top card picking action to the current phase.
     * @param index the position of the card.
     * @param player the player making the action.
     * @param cardInstanceId the expected UUID of the card being picked.
     * @return true if successful, false otherwise.
     */
    public boolean pickTopCard(int index, Player player, String cardInstanceId) {
        requireEnoughConnectedPlayers(player);
        if (!hasCurrentPhase()) return false;
        return currentPhase.pickTopCard(this, index, player, cardInstanceId);
    }

    /**
     * Delegates the bottom card picking action to the current phase.
     * @param index the position of the card.
     * @param player the player making the action.
     * @param cardInstanceId the expected UUID of the card being picked.
     * @return true if successful, false otherwise.
     */
    public boolean pickBottomCard(int index, Player player, String cardInstanceId) {
        requireEnoughConnectedPlayers(player);
        if (!hasCurrentPhase()) return false;
        return currentPhase.pickBottomCard(this, index, player, cardInstanceId);

    }

    /**
     * Delegates the top building picking action to the current phase.
     * @param index the position of the building.
     * @param player the player making the action.
     * @param cardInstanceId the expected UUID of the building being picked.
     * @return true if successful, false otherwise.
     */
    public boolean pickTopBuilding(int index, Player player, String cardInstanceId) {
        requireEnoughConnectedPlayers(player);
        if (!hasCurrentPhase()) return false;
        return currentPhase.pickTopBuilding(this, index, player, cardInstanceId);
    }

    /**
     * Delegates the bottom building picking action to the current phase.
     * @param index the position of the building.
     * @param player the player making the action.
     * @param cardInstanceId the expected UUID of the building being picked.
     * @return true if successful, false otherwise.
     */
    public boolean pickBottomBuilding(int index, Player player, String cardInstanceId) {
        requireEnoughConnectedPlayers(player);
        if (!hasCurrentPhase()) return false;
        return currentPhase.pickBottomBuilding(this, index, player, cardInstanceId);
    }

    /**
     * Delegates the occupation of an offer trail tile to the current phase.
     * @param index the index of the offer trail tile.
     * @param player the player taking the action.
     * @return true if successful, false otherwise.
     */
    public boolean occupyOfferTrailTile(int index, Player player) {
        requireEnoughConnectedPlayers(player);
        if (!hasCurrentPhase()) return false;
        return currentPhase.occupyOfferTrailTile(this, index, player);
    }

    // ==========================================
    //  Trigger System
    // ==========================================

    /**
     * Publishes a trigger: for each active player (orderTileOrder),
     * activates all buildings that match the given trigger key.
     * This mechanism is distinct from error/event broadcast via {@link #raiseEvent(GameEvent)}.
     *
     * @param key The trigger key (e.g. END_TURN, HUNTER_EVENT, etc.)
     * @return true if the trigger is iterated correctly, false on invalid input
     */
    public boolean publishTrigger(TriggerKey key) {
        if (key == null || orderTileOrder == null) {
            return false;
        }
        boolean anyActivated = false;
        for (Player p : orderTileOrder) {
            if (p == null) {
                continue;
            }
            List<Building> triggered = p.getBuildingsByTrigger(key);
            for (Building b : triggered) {
                if (b.triggerBuildingEffect(p, this)) {
                    anyActivated = true;
                }
            }
        }
        if (anyActivated) {
            raiseEvent(new GameEvent(GameEvent.Type.BUILDING_ACTIVATED, null));
        }
        return true;
    }
    /**
     * Marks a player as disconnected. Does NOT raise {@code PLAYER_DISCONNECTED}:
     * the controller raises it after the current phase has skipped the player's
     * turn, so the snapshot carried to clients reflects the post-skip state.
     *
     * @param player the player leaving
     * @return {@code true} if the player belonged to this game and was marked disconnected
     */
    public boolean disconnectPlayer(Player player) {
        if (player == null) return false;
        if (!players.contains(player)) {
            return false;
        }
        player.setConnected(false);
        return true;
    }
    /**
     * Unregisters an observer from {@link GameEvent} notifications.
     *
     * @param observer the observer to remove
     * @return true if removed, false if the observer is null
     */
    public boolean removeObserver(GameStateObserver observer) {
        if (observer == null) return false;
        observers.remove(observer);
        return true;
    }

    /**
     * Marks a previously disconnected player as connected again.
     *
     * @param player the player to reintegrate
     * @return true if the player belongs to this game and was reconnected,
     *         false if the player is null, unknown, or already connected
     */
    public boolean reintegratePlayer(Player player) {
        if (player == null) return false;
        if (!players.contains(player)) {
            return false;
        }
        if (player.isConnected()) {
            return false;
        }
        player.setConnected(true);
        return true;
    }

}
