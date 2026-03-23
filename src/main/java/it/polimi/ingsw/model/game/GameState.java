package it.polimi.ingsw.model.game;
import it.polimi.ingsw.controller.GameConfig;
import it.polimi.ingsw.model.board.Board;
import it.polimi.ingsw.model.cards.Decks;
import it.polimi.ingsw.model.player.Player;

import java.util.*;
import java.util.List;

/**
 * GameState: The root aggregate of the game model and the Context of the State Pattern.
 * It manages the lifecycle of Players, Board, and Deck, and delegates
 * phase-specific logic to {@link GamePhaseBehavior} implementations
 * associated with each {@link GamePhase}.
 */
public class GameState {
    // --- Core game fields ---
    private GameConfig config;
    private Age age;
    private int turn;
    private GamePhase phase;
    private GamePhaseBehavior gamePhase;
    private List<Player> players;
    private Board board;
    private Decks deck;
    private List<Player> turnOrder;
    private List<Player> orderTileOrder;

    // --- State Pattern support fields ---
    private int currentTileIndex;
    private int currentPlayerIndex = 0;
    private int currentPlayerOrderIndex = 0;
    private int extraIndex = 0;

    /**
     * Constructor: Initializes the game environment.
     * @param nicknames The list of names provided from the UI.
     */
    public GameState(List<String> nicknames, GameConfig config) {
        this.age = Age.AGE_1;
        this.turn = 1;
        this.currentTileIndex = 0;
        this.config = config;

        this.board = new Board();
        this.players = new ArrayList<>();

        for (int i = 0; i < nicknames.size(); i++) {
            String name = nicknames.get(i);
            Player p = new Player(i, name);
            this.players.add(p);
        }

        this.turnOrder = new ArrayList<>(this.players);
        this.orderTileOrder = new ArrayList<>(this.players);
    }

    // ==========================================
    //  State Pattern: Phase control & execution
    // ==========================================

    /**
     * Executes the current phase's behavior.
     * Delegates to the {@link GamePhaseBehavior} associated with the current
     * {@link GamePhase}. Does nothing if the phase has no behavior (e.g., event phases).
     */
    public void executePhase() {
        gamePhase = phase.getBehavior();
        if (gamePhase != null) {
            gamePhase.execute(this);
        }
    }

    // ==========================================
    //  Getters & Setters
    // ==========================================

    /** @return The current Age of the game (Age I, II, or III). */
    public Age getAge() {
        return this.age;
    }

    /** @return The current round number. */
    public int getTurn() {
        return this.turn;
    }

    /** @return The current phase within the turn. */
    public GamePhase getPhase() {
        return this.phase;
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
     * Switches the game to a new phase and updates the associated behavior.
     * @param newPhase The next GamePhase.
     * @return true if the phase was updated.
     */
    public void setPhase(GamePhaseBehavior newPhase) {
        this.gamePhase = newPhase;
        // Autostart della fase non appena viene settata!
        this.gamePhase.execute(this);
    }

    /**
     * Sets the current tile index for tile scanning in TurnPhase.
     * @param index The tile index.
     */
    public void setCurrentTileIndex(int index) {
        this.currentTileIndex = index;
    }


    public boolean nextPlayerInTurnOrderTile(){
        currentPlayerOrderIndex++;
        if(currentPlayerOrderIndex >= orderTileOrder.size()){
            currentPlayerOrderIndex = 0;
            return false;
        }
        return true;
    }
    public boolean nextPlayerInTurnOrder(){
        currentPlayerIndex++;
        if(currentPlayerIndex >= turnOrder.size()){
            currentPlayerIndex = 0;
            return false;
        }
        return true;
    }

    public int getExtraIndex() {
        return extraIndex;
    }

    public void setExtraIndex(int extraIndex) {
        this.extraIndex = extraIndex;
    }

    public GameConfig getConfig() {
        return config;
    }
    public boolean updateTurnOrder(Player p){
        if (p == null || turnOrder.contains(p)) return false;
        this.turnOrder.add(p);
        return true;
    }
    public boolean setOrderTileOrder(List<Player> orderTileOrder) {
        if (orderTileOrder == null || orderTileOrder.isEmpty())return false;
        this.orderTileOrder.clear();
        this.orderTileOrder.addAll(orderTileOrder);
        return true;
    }
    public boolean clearTurnOrder() {
        this.turnOrder.clear();
        return true;
    }
}
