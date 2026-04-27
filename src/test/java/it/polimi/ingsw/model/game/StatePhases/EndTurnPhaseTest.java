package it.polimi.ingsw.model.game.StatePhases;

import it.polimi.ingsw.controller.GameConfig;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Totem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EndTurnPhaseTest {
    private GameState context;
    private List<Player> players;
    private Board board;
    private Decks decks;
    private GameConfig config;

    @BeforeEach
    void setUp() throws Exception {
        players = new ArrayList<>();
        players.add(new Player(Totem.BLUE, "Player1"));
        players.add(new Player(Totem.RED, "Player2"));

        board = new Board(new OrderTile(), new TileSet(new ArrayList<>()));

        decks = new Decks(new ArrayList<>(), new ArrayList<>());
        for (int i = 0; i < 20; i++) {
            Card c = createMockCard();
            c.addToDeck(decks);
        }
        config = new GameConfig();
        List<Integer> startingFood = new ArrayList<>(List.of(5, 5, 5, 5, 5));

        Field foodField = GameConfig.class.getDeclaredField("startingFood");
        foodField.setAccessible(true);
        foodField.set(config, startingFood);

        OrderTile orderTile = new OrderTile();
        injectField(orderTile, "orderBonus", new ArrayList<>(List.of(0, 1, 2, 3)));
        injectField(board, "orderTile", orderTile);

        context = new GameState(players, config, board, decks,"testId");
    }
    @Test
    @DisplayName("Edge Case: Game Termination on Max Turns")
    void testExecute_MaxTurnsReached_TransitionsToEndGame() throws Exception {
        EndTurnPhase phase = new EndTurnPhase();

        // Setup MaxTurns = 5 and current turn = 5 to trigger the branch
        injectField(context.getConfig(), "maxTurns", 5);
        context.setTurn(5);

        boolean result = phase.execute(context);

        assertTrue(result, "Phase should execute successfully.");
        // Verify physical transition to EndGamePhase
        assertInstanceOf(EndGamePhase.class, context.getCurrentPhase(),
                "Should transition to EndGamePhase when current turn reaches max turns.");
    }

    @Test
    @DisplayName("Edge Case: Atomic Turn Reset and Board Refurbishment")
    void testExecute_NormalTurnEnd_PerformsPhysicalCleanup() throws Exception {
        EndTurnPhase phase = new EndTurnPhase();

        // Prepare board with an occupied tile to verify de-occupation logic
        Tile occupiedTile = new Tile();
        injectField(occupiedTile, "isOccupied", true);

        // Get the Tiles object from board and inject our occupied tile
        TileSet boardTiles = board.getTiles();
        injectField(boardTiles, "tiles", new ArrayList<>(List.of(occupiedTile)));

        context.setTurn(1);
        injectField(context.getConfig(), "maxTurns", 10);

        // Mock current turn order and players to verify sequence: save -> clear
        List<Player> turnOrder = new ArrayList<Player>();
        turnOrder.add(new Player(Totem.BLUE, "Player1"));
        injectField(context, "turnOrder", turnOrder);

        phase.execute(context);

        // Verify Tile Physical State: Must be de-occupied
        assertFalse(occupiedTile.isOccupied(), "All tiles should be physically de-occupied for the next turn.");

        // Verify Turn Counter: Must be incremented
        assertEquals(2, context.getTurn(), "Turn counter should physically increment by 1.");

        // Verify Turn Order: Should be cleared after the sequence
        assertTrue(context.getTurnOrder().isEmpty(), "Turn order list should be physically cleared.");

        // Verify Phase Transition: Should move to StartTurnPhase
        assertInstanceOf(StartTurnPhase.class, context.getCurrentPhase(), "Should transition to StartTurnPhase.");
    }

    @Test
    @DisplayName("Edge Case: Null Component Protection")
    void testExecute_NullContext_ReturnsFalse() {
        EndTurnPhase phase = new EndTurnPhase();
        // Look at the declared type (left side): Passing null to guard clauses
        assertFalse(phase.execute(null), "Should return false for null context.");
    }

    /**
     * Recursive field injection tool to bypass private encapsulation.
     */
    private void injectField(Object target, String fieldName, Object value) throws Exception {
        Field field = null;
        Class<?> current = target.getClass();
        while (current != null) {
            try {
                field = current.getDeclaredField(fieldName);
                break;
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass(); // Traverse up the hierarchy
            }
        }
        if (field == null) throw new NoSuchFieldException(fieldName);
        field.setAccessible(true);
        field.set(target, value); // Physical write to memory
    }
    private Card createMockCard() {
        return new Card() {
            @Override public boolean addToDeck(Decks d) { return d.addCard(this); }
            @Override public Age getAge() { return Age.AGE_1; }
            @Override public boolean isBuyable() { return true; }
            @Override public CardCategory getCategory() { return CardCategory.CHARACTER; }
            @Override public int getResolutionPriority() { return 0; }
        };
    }


}