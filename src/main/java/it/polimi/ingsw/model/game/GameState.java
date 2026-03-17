package it.polimi.ingsw.model;
import java.util.*;
import java.util.List;
/**
 * GameState: The root aggregate of the game model.
 * It manages the lifecycle of Players, Board, and Deck.
 */
public class GameState {
    // --- Fields (Components inside the box) ---
    private Age age;
    private int turn;
    private GamePhase phase;
    private List<Player> players;       // 1..5 Players as per UML
    private Board board;               // 1 Board per game
    private Deck deck;                 // 1 Deck per game
    private int currentPlayerIndex;    // Tracks whose turn it is
    private List<Player> turnOrder;    // List to manage player sequence

    /**
     * Constructor: Initializes the game environment.
     * @param nicknames The list of names provided from the UI (Argument "names").
     */
    public GameState(List<String> nicknames) {
        // Initializing basic game attributes
        this.age = Age.AGE_1;
        this.turn = 1;
        this.phase = GamePhase.START_TURN;
        this.currentPlayerIndex = 0;

        // Instantiating internal components (Composition)
        this.board = new Board();
        this.deck = new Deck();
        this.players = new ArrayList<>();

        // Converting nicknames (formal parameter) into Player objects
        for (int i = 0; i < nicknames.size(); i++) {
            String name = nicknames.get(i);
            Player p = new Player(i,name);

            // Numerical Verification: Initial food allocation based on rulebook


            // Adding the reference to our internal players list
            this.players.add(p);
        }

        // Initialize turn order based on initial player list
        this.turnOrder = new ArrayList<>(this.players);
    }

    // Getters & Setters
    /** @return The current Age of the game (Age I, II, or III). */
    public Age getAge() {
        return this.age;
    }

    /** @return The current round number. */
    public int getTurn() {
        return this.turn;
    }

    /** @return The current phase within the turn (e.g., START_TURN, EVENT, ACTION). */
    public GamePhase getPhase() {
        return this.phase;
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
     * Switches the game to a new phase.
     * @param phase The next GamePhase.
     * @return true if the phase was updated.
     */
    public boolean setPhase(GamePhase phase) {
        if (phase == null) return false;
        this.phase = phase;
        return true;
    }

    /**
     * Helper method to retrieve the player who is currently taking their turn.
     * @return The active Player object.
     */
    public Player getCurrentPlayer() {
        if (players == null || players.isEmpty()) return null;
        return players.get(currentPlayerIndex);
    }

    /** @return The list of players sorted by their current turn order. */
    public List<Player> getTurnOrder() {
        return this.turnOrder;
    }

    // --- Essential Getters for Controller interaction ---
    public Deck getDeck() { return this.deck; }
    public Board getBoard() { return this.board; }

    /**
     * Advances the game state by progressing through phases, turns.
     * This version aligns with Mesos rules where END_TURN signifies the
     * completion of all player actions, leading directly to the global Event phase.*/

    public void advanceState() {
        switch (this.phase) {
            case START_TURN:
                // Transition from preparation to the active playing phase
                this.phase = GamePhase.IN_TURN;
                break;

            case IN_TURN:
                // Transition once all card-taking actions are finished
                this.phase = GamePhase.END_TURN;
                break;

            case END_TURN:
                /**
                 * All players have completed their actions for the current round.
                 * Reset the index and move directly to the global cleanup/event phase.
                 */
                this.currentPlayerIndex = 0;
                this.phase = GamePhase.EVENT;
                break;

            case EVENT:
                //Increment the round counter
                this.turn++;

                //Termination Check: Validate if the session has concluded
                if (this.turn > 10) {
                    this.phase = GamePhase.END_GAME;
                } else {
                    // Loop back to start a new round
                    this.phase = GamePhase.START_TURN;
                }
                break;

            case END_GAME:
                // Terminal state: No further progression allowed
                break;
        }
    }
}
