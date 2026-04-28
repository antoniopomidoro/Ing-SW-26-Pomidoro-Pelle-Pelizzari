package it.polimi.ingsw.model.game.StatePhases;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.controller.GameConfig;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.board.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;
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
        players.add(new Player(Totem.RED, "Player1"));
        players.add(new Player(Totem.BLUE, "Player2"));

        board = new Board(new OrderTile(), new TileSet(new ArrayList<>()));

        decks = new Decks(new ArrayList<>(), new ArrayList<>());
        for (int i = 0; i < 20; i++) {
            Card c = createMockCard();
            c.addToDeck(decks);
        }

        ObjectMapper mapper = new ObjectMapper();
        InputStream is = getClass().getClassLoader().getResourceAsStream("json/config.json");
        config = mapper.readValue(is, GameConfig.class);

        context = new GameState(players, config, board, decks,"testId");

    }


    private Card createMockCard() {
        return new Card() {
            @Override public boolean addToDeck(Decks d) { return d.addCard(this); }
            @Override public Age getAge() { return Age.AGE_1; }
            @Override public boolean isBuyable() { return true; }
            @Override public CardCategory getCategory() { return CardCategory.BUILDING; }
            @Override public int getResolutionPriority() { return 0; }
        };
    }

    @Test
    void testSetupPhaseExecute() {
        SetupPhase setupPhase = new SetupPhase();
        setupPhase.execute(context);

        assertEquals(2,players.get(0).getFood());
        assertEquals(3,players.get(1).getFood());
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
        players.add(new Player(Totem.BLUE, "Player1"));
        players.add(new Player(Totem.RED, "Player2"));
        players.add(new Player(Totem.WHITE, "Player3"));
        players.add(new Player(Totem.BLACK, "Player4"));
        players.add(new Player(Totem.YELLOW, "Player5"));
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

    /**
     * Physical Verification: SetupPhase state verification.
     * Confirms that the Board correctly receives buildings from Decks based on Config.
     */
    @Test
    void testAddTopBuildingsStateVerification() throws Exception {
        //  Synchronize Physical Environment
        int playerCount = context.getPlayers().size();
        int ageValue = context.getAge().getValue();
        assertEquals(2, playerCount, "Physical Check: Current game must be 2-player.");

        // We inject 2 to ensure Decks has enough supply for any potential matrix value
        Decks decks = context.getDeck();
        Building b1 = createMockBuilding(Age.AGE_1);
        Building b2 = createMockBuilding(Age.AGE_1);
        injectBuildingToDecksInternal(decks, Age.AGE_1, b1);
        injectBuildingToDecksInternal(decks, Age.AGE_1, b2);

        int expectedFromConfig = config.getBuildingsCount(playerCount, ageValue);

        SetupPhase setupPhase = new SetupPhase();
        setupPhase.execute(context);

        List<Building> topBuildings = board.getTopBuildings();

        // Assertions
        assertNotNull(topBuildings, "Physical Error: Board's building list is null.");
        assertEquals(expectedFromConfig, topBuildings.size(),
                "Board count must physically match the value defined in config.json.");
    }
    /**
     * Helper: Creates a mock Building object satisfying interface requirements.
     */
    private Building createMockBuilding(Age age) {
        return new Building() {
            @Override public CardCategory getCategory() { return CardCategory.BUILDING; }
            @Override public Age getAge() { return age; }
            @Override public boolean addToDeck(Decks d) { return true; }
            @Override public boolean isBuyable() { return true; }
            @Override public int getResolutionPriority() { return 0; }
        };
    }

    /**
     * Helper: Injects buildings into the private 'buildings' Map within Decks
     * to align with the retrieval logic in Decks.getBuildings.
     */
    private void injectBuildingToDecksInternal(Decks decks, Age age, Building b) throws Exception {
        // Access the private 'buildings' field via reflection.
        java.lang.reflect.Field field = Decks.class.getDeclaredField("buildings");
        field.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<Age, List<Building>> buildingsMap = (Map<Age, List<Building>>) field.get(decks);

        // Ensure the list for the specific age is initialized and populated.
        buildingsMap.computeIfAbsent(age, k -> new ArrayList<>()).add(b);
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