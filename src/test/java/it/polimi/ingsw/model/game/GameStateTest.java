package it.polimi.ingsw.model.game;
import it.polimi.ingsw.controller.GameConfig;
import it.polimi.ingsw.model.board.Board;
import it.polimi.ingsw.model.board.OrderTile;
import it.polimi.ingsw.model.board.Tile;
import it.polimi.ingsw.model.board.TileSet;
import it.polimi.ingsw.model.cards.Building;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.Decks;
import it.polimi.ingsw.model.game.StatePhases.IllegalMoveException;
import it.polimi.ingsw.model.game.StatePhases.SetupPhase;
import it.polimi.ingsw.model.game.StatePhases.StartTurnPhase;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.game.GameEvent;
import it.polimi.ingsw.model.game.GameStateObserver;
import it.polimi.ingsw.model.game.StatePhases.GamePhaseBehavior;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import it.polimi.ingsw.model.player.Totem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
/**
 * GameStateTest - A standalone test suite using manual stubs instead of Mockito.
 * This ensures the core engine is testable even without third-party dependencies.
 */
public class GameStateTest {
    private GameState gameState;
    private Player playerA, playerB, playerC;
    private TestObserver observer;
    //MANUAL STUBS
    /**
     * A manual stub for GameStateObserver to capture event broadcasts.
     */
    private static class TestObserver implements GameStateObserver {
        public GameEvent lastEvent;
        public int callCount = 0;

        @Override
        public void onGameEvent(GameEvent event) {
            this.lastEvent = event;
            this.callCount++;
        }
    }

    /**
     * UNIFIED STUB: This single class handles both auto-start and action logic.
     * It successfully implements the interface required by setPhase.
     */
    private static class GamePhaseStub implements GamePhaseBehavior {
        public  boolean pickTopCardCalled = false;

        @Override
        public boolean execute(GameState context) {
            // Fulfills the requirement of setPhase returning true
            return true;
        }

        @Override
        public boolean pickTopCard(GameState context, int index, Player player,String cardInstanceId) {
            // Directing the flow to trigger raiseSuccessfulAction indirectly
            this.pickTopCardCalled = true;
            return true;
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        // Initialize real player objects to avoid NullPointerExceptions
        playerA = new Player(Totem.RED,"A");
        playerB = new Player(Totem.WHITE,"B");
        playerC = new Player(Totem.BLUE,"C");
        List<Player> players = Arrays.asList(playerA, playerB, playerC);
        GameConfig config = new GameConfig();
        List<Integer> startingFood = new ArrayList<>(List.of(5, 5, 5, 5, 5));

        Field foodField = GameConfig.class.getDeclaredField("startingFood");
        foodField.setAccessible(true);
        foodField.set(config, startingFood);

        OrderTile orderTile=new OrderTile();
        List<Tile> tiles=new ArrayList<>();
        tiles.add(new Tile());
        tiles.add(new Tile());
        tiles.add(new Tile());
        TileSet tileSet=new TileSet(tiles);
        Board board=new Board(orderTile,tileSet);
        List<Card> cards=new ArrayList<>();
        List<Building> buildings=new ArrayList<>();

        Decks decks = new Decks(new ArrayList<>(), new ArrayList<>());
        for (int i = 0; i < 20; i++) {
            Card c = createMockCard();
            c.addToDeck(decks);
        }

        // Instantiate GameState with manual setup (assuming null for complex dependencies)
        gameState = new GameState(players, config, board, decks,"testId");
        // Setup our manual observer
        observer = new TestObserver();

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

    /**
     * TEST 1: PHASE TRANSITION DELEGATION
     * Verifies that setPhase updates the state and triggers the execute() method.
     */
    @Test
    @DisplayName("Verify setPhase correctly triggers the auto-start execute() logic")
    void testSetPhaseFlow() {
        // Step 1: Create the actual implementation (Right Side)
        GamePhaseStub myStub = new GamePhaseStub();

        // Step 2: Pass it to the method expecting the interface (Left Side)
        boolean result = gameState.setPhase(myStub);

        // Step 3: Verification
        assertTrue(result, "setPhase should return true because the stub's execute() returns true.");
        assertEquals(myStub, gameState.getCurrentPhase(), "The internal currentPhase should match the injected stub.");
    }

     /* TEST 2: SEQUENCE REORDERING (Stream Logic)
     * Verifies the two-pass filtering for disconnected players.
     */
    @Test
    @DisplayName("Verify sequence reordering ensures online players come first")
    void testOrderTileOrderWithDisconnection() {
        // Scenario: Bob is disconnected
        playerB.setConnected(false);
        playerA.setConnected(true);
        playerC.setConnected(true);

        // Input sequence: [B, C, A]
        List<Player> inputOrder = Arrays.asList(playerB, playerC, playerA);

        // Execute reordering
        gameState.setOrderTileOrder(inputOrder);

        // Numerical Verification: Online [C, A] then Offline [B]
        List<Player> result = gameState.getOrderTileOrder();

        assertEquals(playerC, result.get(0));
        assertEquals(playerA, result.get(1));
        assertEquals(playerB, result.get(2), "Disconnected player must be at the end.");
    }

    /**
     * TEST 3: PHASE DELEGATION
     * Verifies GameState forwards calls to the currentPhase.
     */
    @Test
    @DisplayName("Verify pickTopCard is delegated to the active phase handler")
    void testActionDelegation() {
        // Prepare: Use our manual TestPhase stub
        GamePhaseStub manualPhase = new GamePhaseStub();
        gameState.setPhase(manualPhase);

        // Action: Call through GameState API
        gameState.pickTopCard(5, playerA,"cardID");

        // Assert: Check if the phase's stub method was reached
        assertTrue(manualPhase.pickTopCardCalled, "The call should be routed to the phase handler.");
    }
    /**
     * TEST : addObserver
     * Verification: Ensure observers are added and not ignored if non-null.
     */
    @Test
    void testAddObserver() {
        gameState.addObserver(observer);
        // We verify it was added by triggering an event later
        gameState.raiseEvent(new GameEvent(GameEvent.Type.SUCCESSFUL_ACTION, null));
        assertEquals(1, observer.callCount, "Observer should have been added and notified.");
    }

    /**
     * TEST : raiseEvent
     * Verification: Ensure all registered observers receive the broadcast.
     */
    @Test
    void testRaiseEventBroadcasting() {
        TestObserver observer2 = new TestObserver();
        gameState.addObserver(observer);
        gameState.addObserver(observer2);

        GameEvent event = new GameEvent(GameEvent.Type.SUCCESSFUL_ACTION, null);
        gameState.raiseEvent(event);

        // Precise Numerical Verification
        assertEquals(1, observer.callCount);
        assertEquals(1, observer2.callCount);
        assertEquals(event, observer.lastEvent, "The captured event should match the broadcasted one.");
        assertEquals(event, observer2.lastEvent, "The captured event should match the broadcasted one.");
    }

    /**
     * TEST: nextPlayerInTurnOrderTile (Conditional Skipping Flow)
     * Verification: Ensures the while(true) loop skips disconnected players
     * but terminates correctly at the list boundary.
     */
    @Test
    void testNextPlayerInTurnOrderTile_SkipDisconnected() {
        // 1. Prepare: Simulate Bob dropping out (A:On, B:Off, C:On)
        playerA.setConnected(true);
        playerB.setConnected(false);
        playerC.setConnected(true);
        gameState.setOrderTileOrder(Arrays.asList(playerA, playerB, playerC));
        // Currently at "A"

        // 2. Action: Find next connected player. It must skip index 1 (Bob).
        boolean result = gameState.nextPlayerInTurnOrderTile();

        // 3. Numerical Verification: Check if the index jumped directly to 2.
        assertTrue(result, "Should successfully find the next online player (C).");
        assertSame(playerC,gameState.getCurrentOrderTileOrderPlayer());
    }

    /**
     * TEST: nextPlayerInTurnOrderTile (Edge Case: All Players Offline)
     * Verification: Confirms the safety check prevents infinite loops if no one is online.
     */
    @Test
    void testNextPlayerInTurnOrderTile_NoOneOnline() {
        // 1. Prepare: Everyone is disconnected
        playerA.setConnected(false);
        playerB.setConnected(false);
        gameState.setOrderTileOrder(Arrays.asList(playerA, playerB));

        // 2. Action: Attempt to find a connected player.
        boolean result = gameState.nextPlayerInTurnOrderTile();

        // 3. Verification: The logic should cycle through and hit the 'if' breaker.
        assertFalse(result, "Must return false when a full cycle reveals no connected players.");

    }
    @Test
    void testNextPlayerInTurnOrder_LinearProgression() {
        // 1. Arrange: Setup players and add them to the standard turn order
        List<Tile>tiles=gameState.getBoard().getTiles().getTiles();
        tiles.get(0).occupy(playerA);
        tiles.get(1).occupy(playerB);
        gameState.updateTurnOrder();

        // Ensure we start at the first player (Index 0)
        // Note: Implicit initialization to 0 is assumed from JVM specs

        // 2. Action: Move from the first player to the second (A -> B)
        boolean hasNext = gameState.nextPlayerInTurnOrder();

        // 3. Numerical Verification: Verify the increment logic
        assertTrue(hasNext, "Should return true as there is still another player in the sequence.");
        assertSame(playerB, gameState.getCurrentTurnOrderPlayer(), "The index should have incremented from 0 to 1.");

        // 4. Boundary Action: Move past the last player (B -> A)
        boolean loopedBack = gameState.nextPlayerInTurnOrder();

        // 5. Boundary Verification: Verify the reset logic
        assertFalse(loopedBack, "Should return false to indicate the turn cycle has restarted.");

    }
    @Test
    @DisplayName("Verify next round execution order is built from previous turnOrder")
    void testNextRoundOrderTileUpdateFromPreviousTurnOrder() {
        List<Tile>tiles=gameState.getBoard().getTiles().getTiles();
        // 1. Arrange: Round 1 finishes with a specific turn order
        // Sequence: A (Online), B (Offline), C (Online)
        playerA.setConnected(true);
        playerB.setConnected(false);
        playerC.setConnected(true);

        tiles.get(0).occupy(playerA);
        tiles.get(1).occupy(playerB);
        tiles.get(2).occupy(playerC);
       gameState.updateTurnOrder();
        List<Player> round1Result = gameState.getTurnOrder();
        assertEquals(3, round1Result.size(), "Round 1 should have 3 players.");

        // 2. Action: Transition to Round 2 by updating orderTileOrder
        // The system calls setOrderTileOrder using the list from the previous round
        boolean updateSuccessful = gameState.setOrderTileOrder(round1Result);
        assertTrue(updateSuccessful, "OrderTile update should be successful.");

        // 3. Numerical Verification: Check the partitioned sequence
        List<Player> round2ExecutionOrder = gameState.getOrderTileOrder();

        // Rule: Online players first (A, C), followed by Offline players (B)
        assertEquals(3, round2ExecutionOrder.size(), "Total count preserved.");

        // Index 0 & 1 must be Online players (A and C)
        assertTrue(round2ExecutionOrder.get(0).isConnected(), "First player must be online.");
        assertTrue(round2ExecutionOrder.get(1).isConnected(), "Second player must be online.");

        // Index 2 must be the Offline player (B)
        assertFalse(round2ExecutionOrder.get(2).isConnected(), "Last player should be the offline one.");
        assertEquals("B", round2ExecutionOrder.get(2).getNickname(), "Bob (offline) pushed to the end.");
    }
    /**
     *  TEST: GameState Delegation & Event Dispatching
     */

    @Test
    @DisplayName("Should throw exception when calling action without an active phase")
    void testActionFailureWithNullPhase() {
        // 1. Arrange: Force a null phase state
        gameState.setPhase(null);
        assertThrows(IllegalMoveException.class, () -> {
            gameState.occupyOfferTrailTile(0, playerA);
        }, "The default interface implementation must throw an exception.");
    }


    /**
     * A helper method to inject private fields using Java Reflection.
     * This ensures we can test edge cases without modifying the original source code.
     */
    private void safeInjectField(Object target, String fieldName, Object value) {
        try {
            // Look at the actual class (Right side)
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            // Bypass the private modifier physically
            field.setAccessible(true);
            // Inject the mocked data into the target object
            field.set(target, value);
        } catch (Exception e) {
            // If the field name is wrong, we throw a runtime exception to fail the test early
            throw new RuntimeException("Reflection failed for field: " + fieldName, e);
        }
    }
    private Object getPrivateField(Object target, String fieldName) throws Exception {
        java.lang.reflect.Field field = null;
        Class<?> current = target.getClass();
        while (current != null) {
            try {
                field = current.getDeclaredField(fieldName);
                break;
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass(); // 物理支持继承链查找
            }
        }
        if (field == null) throw new NoSuchFieldException("Field " + fieldName + " not found");
        field.setAccessible(true);
        return field.get(target);
    }
}
