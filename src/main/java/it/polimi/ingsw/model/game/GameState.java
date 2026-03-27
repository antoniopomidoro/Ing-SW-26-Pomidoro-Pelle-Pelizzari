package it.polimi.ingsw.model.game;
import it.polimi.ingsw.controller.GameConfig;
import it.polimi.ingsw.model.board.Board;
import it.polimi.ingsw.model.cards.Decks;
import it.polimi.ingsw.model.player.Player;

import java.util.*;
import java.util.List;
import it.polimi.ingsw.model.cards.Building;

/**
 * GameState: The root aggregate of the game model and the Context of the State Pattern.
 * It manages the lifecycle of Players, Board, and Deck, and delegates
 * phase-specific logic to {@link GamePhaseBehavior} implementations.
 */
public class GameState {
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

    // --- State Pattern support fields ---
    private int currentTileIndex;
    private int currentPlayerIndex = 0;
    private int currentPlayerOrderIndex = 0;
    private int extraIndex = 0;

    /**
     * Constructor: Initializes the game environment.
     * @param players The list of players provided from the login handler.
     * @param config The game configuration object.
     * @param board The initial board instance.
     * @param deck The decks manager.
     */
    public GameState(List<Player> players, GameConfig config, Board board, Decks deck) {
        this.age = Age.AGE_1;
        this.turn = 1;
        this.currentTileIndex = 0;
        this.config = config;
        this.board = board;
        this.deck = deck;
        this.players = new ArrayList<>(players);
        this.turnOrder = new ArrayList<>();
        this.orderTileOrder = new ArrayList<>(this.players);
        setPhase(new SetupPhase());
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
        return this.turnOrder.get(currentPlayerIndex);
    }

    /** @return The current player in order-tile order, or null if unavailable. */
    public Player getCurrentOrderTileOrderPlayer() {
        return this.orderTileOrder.get(currentPlayerOrderIndex);
    }

    /** @return The current tile index during tile scanning in TurnPhase. */
    public int getCurrentTileIndex() {
        return this.currentTileIndex;
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
        // Autostart della fase non appena viene settata!
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
    public boolean nextPlayerInTurnOrderTile(){
        currentPlayerOrderIndex++;
        if(currentPlayerOrderIndex >= orderTileOrder.size()){
            currentPlayerOrderIndex = 0;
            return false;
        }
        return true;
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
     * Updates the turn order with a new player.
     * @param p The player to add to the turn order.
     * @return true if successful, false if the player is null or already present.
     */
    public boolean updateTurnOrder(Player p){
        if (p == null || turnOrder.contains(p)) return false;
        this.turnOrder.add(p);
        return true;
    }

    /**
     * Sets a completely new sequence for the order-tile order.
     * @param orderTileOrder A list representing the new order.
     * @return true if successful, false if the list is null or empty.
     */
    public boolean setOrderTileOrder(List<Player> orderTileOrder) {
        if (orderTileOrder == null || orderTileOrder.isEmpty())return false;
        this.orderTileOrder.clear();
        this.orderTileOrder.addAll(orderTileOrder);
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

    /**
     * Registra un osservatore per le notifiche {@link GameEvent}.
     *
     * @param observer callback osservatore; valori null vengono ignorati
     */
    public void addObserver(GameStateObserver observer) {
        if (observer != null) observers.add(observer);
    }

    /**
     * Pubblica un evento a tutti gli osservatori registrati.
     *
     * @param event evento da notificare; null viene ignorato
     */
    public void raiseEvent(GameEvent event) {
        if (event == null) return;
        observers.forEach(o -> o.onGameEvent(event));
    }

    /**
     * Emette un evento di azione riuscita per aggiornare le view osservatrici.
     */
    private void raiseSuccessfulAction(Player player, String actionName) {
        raiseEvent(new GameEvent(GameEvent.Type.SUCCESSFUL_ACTION, player, actionName));
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

    /**
     * Delegates the top card picking action to the current phase.
     * @param index the position of the card.
     * @param player the player making the action.
     * @return true if successful, false otherwise.
     */
    public boolean pickTopCard(int index, Player player) {
        if (!hasCurrentPhase()) return false;
        boolean success = currentPhase.pickTopCard(this, index, player);
        if (success) {
            raiseSuccessfulAction(player, "pickTopCard(index=" + index + ")");
        }
        return success;
    }

    /**
     * Delegates the bottom card picking action to the current phase.
     * @param index the position of the card.
     * @param player the player making the action.
     * @return true if successful, false otherwise.
     */
    public boolean pickBottomCard(int index, Player player) {
        if (!hasCurrentPhase()) return false;
        boolean success = currentPhase.pickBottomCard(this, index, player);
        if (success) {
            raiseSuccessfulAction(player, "pickBottomCard(index=" + index + ")");
        }
        return success;
    }

    /**
     * Delegates the top building picking action to the current phase.
     * @param index the position of the building.
     * @param player the player making the action.
     * @return true if successful, false otherwise.
     */
    public boolean pickTopBuilding(int index, Player player) {
        if (!hasCurrentPhase()) return false;
        boolean success = currentPhase.pickTopBuilding(this, index, player);
        if (success) {
            raiseSuccessfulAction(player, "pickTopBuilding(index=" + index + ")");
        }
        return success;
    }

    /**
     * Delegates the bottom building picking action to the current phase.
     * @param index the position of the building.
     * @param player the player making the action.
     * @return true if successful, false otherwise.
     */
    public boolean pickBottomBuilding(int index, Player player) {
        if (!hasCurrentPhase()) return false;
        boolean success = currentPhase.pickBottomBuilding(this, index, player);
        if (success) {
            raiseSuccessfulAction(player, "pickBottomBuilding(index=" + index + ")");
        }
        return success;
    }

    /**
     * Delegates the occupation of an offer trail tile to the current phase.
     * @param index the index of the offer trail tile.
     * @param player the player taking the action.
     * @return true if successful, false otherwise.
     */
    public boolean occupyOfferTrailTile(int index, Player player) {
        if (!hasCurrentPhase()) return false;
        boolean success = currentPhase.occupyOfferTrailTile(this, index, player);
        if (success) {
            raiseSuccessfulAction(player, "occupyOfferTrailTile(index=" + index + ")");
        }
        return success;
    }

    // ==========================================
    //  Trigger System
    // ==========================================

    /**
     * Publishes a trigger: for each active player (orderTileOrder),
     * activates all buildings that match the given trigger key.
     * Questo meccanismo e distinto dal broadcast error/event via {@link #raiseEvent(GameEvent)}.
     *
     * @param key The trigger key (e.g. END_TURN, HUNTER_EVENT, etc.)
     * @return true se il trigger viene iterato correttamente, false su input non valido
     */
    public boolean publishTrigger(TriggerKey key) {
        if (key == null || orderTileOrder == null) {
            return false;
        }
        for (Player p : orderTileOrder) {
            if (p == null) {
                continue;
            }
            List<Building> triggered = p.getBuildingsByTrigger(key);
            for (Building b : triggered) {
                b.triggerBuildingEffect(p, this);
            }
        }
        return true;
    }
}
