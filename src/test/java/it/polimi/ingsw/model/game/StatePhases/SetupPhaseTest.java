package it.polimi.ingsw.model.game.StatePhases;

import it.polimi.ingsw.controller.GameConfig;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.board.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.lang.reflect.Field;

class SetupPhaseTest {
    private GameState context;
    private List<Player> players;
    private Board board;
    private Decks decks;
    private GameConfig config;

    @BeforeEach
    void setUp() throws Exception {
        players = new ArrayList<>();
        players.add(new Player(Totem.BLUE_TOTEM, "Player1"));
        players.add(new Player(Totem.RED_TOTEM, "Player2"));

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

        context = new GameState(players, config, board, decks,"testId");
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

    @Test
    void testSetupPhaseExecute() {
        SetupPhase setupPhase = new SetupPhase();

        for (Player p : players) {
            assertEquals(5, p.getFood(), "every player gets 5 food");
        }
        assertFalse(board.getTopCards().isEmpty(), "board top is inserted");
        assertFalse(board.getBottomCards().isEmpty(), "board bottom is inserted");
        assertFalse(context.getCurrentPhase() instanceof SetupPhase, "next phase is setted");
    }
    @Test
    void testSetupPhaseCardQuantities() throws Exception {
        int topExtra = 2;
        int bottomExtra = 3;
        int playerCount = players.size(); // 2 players

        int expectedTopCount = topExtra + playerCount;
        int expectedBottomCount = bottomExtra + playerCount;

        Field topField = GameConfig.class.getDeclaredField("topExtraCards");
        topField.setAccessible(true);
        topField.set(config, topExtra);

        Field bottomField = GameConfig.class.getDeclaredField("bottomExtraCards");
        bottomField.setAccessible(true);
        bottomField.set(config, bottomExtra);

        SetupPhase setupPhase = new SetupPhase();
        context.setPhase(setupPhase);

        assertEquals(expectedTopCount, context.getBoard().getTopCards().size(),
                "NumTopBoardCard=topExtraCards +NumPlayers");
        assertEquals(expectedBottomCount, context.getBoard().getBottomCards().size(),
                "NumBottomCard=bottomExtraCards + NumPlayers");

    }
    @Test
    void testExecute_FoodListTooShort_ShouldThrowIndexOutOfBounds() {
        // 1. Setup the physical environment: Create 5 players
        List<Player> players = new ArrayList<>();
        players.add(new Player(Totem.BLUE_TOTEM, "Player1"));
        players.add(new Player(Totem.RED_TOTEM, "Player2"));
        players.add(new Player(Totem.WHITE_TOTEM, "Player3"));
        players.add(new Player(Totem.BLACK_TOTEM, "Player4"));
        players.add(new Player(Totem.YELLOW_TOTEM, "Player5"));
        // 2. Create an invalid configuration: Only 4 food values provided (Index 0-3)
        List<Integer> insufficientFood = new ArrayList<>(List.of(1, 2, 3, 4));
        // 3. Inject data using reflection (Keep the source code pristine)
        safeInjectField(context, "players", players);
        safeInjectField(config, "startingFood", insufficientFood);
        // 4. Assert the implicit exception thrown by JVM
        // The JVM will throw this when i=4 because the list length is only 4
        SetupPhase setupPhase = new SetupPhase();
        IndexOutOfBoundsException exception = assertThrows(IndexOutOfBoundsException.class, () -> {
            setupPhase.execute(context);
        });

        // 5. Precise diagnosis: Verify the system-generated message
        // Expected: Index 4 out of bounds for length 4
        assertEquals("Index 4 out of bounds for length 4", exception.getMessage());
    }

    @Test
    @DisplayName("Verify SetupPhase successfully transitions to StartTurnPhase")
    void testExecute_Success_ShouldTransitionToNextPhase() throws Exception {
        // 1. Arrange: Prepare players and sufficient resources
        List<Player> players = new ArrayList<>(List.of(
                new Player(Totem.RED_TOTEM, "A"),
                new Player(Totem.WHITE_TOTEM, "B")
        ));
        List<Integer> sufficientFood = new ArrayList<>(List.of(5, 5));

        // 2. Inject valid data using reflection to avoid failures
        safeInjectField(context, "players", players);
        safeInjectField(config, "startingFood", sufficientFood);

        // 3. Action: Execute the setup phase
        // The execute method should return true upon successful transition logic
        SetupPhase setupPhase = new SetupPhase();
        boolean result = setupPhase.execute(context);

        // 4. Assert: Verify the state transition
        assertTrue(result, "The execute method should return true.");

        // Look at the actual type (Right side): check if phase is now StartTurnPhase
        assertInstanceOf(StartTurnPhase.class, context.getCurrentPhase(),
                "The game state should have transitioned to StartTurnPhase.");
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
}