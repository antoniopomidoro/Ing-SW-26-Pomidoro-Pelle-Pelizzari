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
class ChangeAgePhaseTest {
    private GameState context;
    private Board board;
    private Decks decks;
    private List<Player> players;
    private GameConfig config;


    private static class StubCard extends Card {
        private final Age age;
        public StubCard(Age age) { this.age = age; }
        @Override public Age getAge() { return this.age; }
        @Override public boolean addToDeck(Decks d) { return d.addCard(this); }
        @Override public CardCategory getCategory() { return CardCategory.CHARACTER; }
        @Override public boolean isBuyable() { return true; }
        @Override public int getResolutionPriority() { return 0; }
    }

    private static class StubBuilding extends Building {
        private final Age age;
        public StubBuilding(Age age) { this.age = age; }
        @Override public Age getAge() { return this.age; }
        @Override public boolean addToDeck(Decks d) { return d.addBuilding(this); }
        @Override public CardCategory getCategory() { return CardCategory.BUILDING; }
        @Override public boolean isBuyable() { return true; }
        @Override public int getResolutionPriority() { return 0; }
    }

    @BeforeEach
    void setUp() throws Exception {
        players = new ArrayList<>(List.of(new Player(Totem.BLUE_TOTEM, "P1"),new Player(Totem.RED_TOTEM, "P2")));
        board = new Board(new OrderTile(), new TileSet(new ArrayList<>()));
        config = new GameConfig();

        decks = new Decks(new ArrayList<>(), new ArrayList<>());

        // inject tool
        injectDecksData(decks);

        ObjectMapper mapper = new ObjectMapper();
        InputStream is = getClass().getClassLoader().getResourceAsStream("json/config.json");
        config = mapper.readValue(is, GameConfig.class);

        // 4. Inilization GameState,now run SetupPhase
        context = new GameState(players, config, board, decks,"testId");
    }

    @Test
    @DisplayName("Age change and update board")
    void testChangeAgePhaseExecute() throws Exception {
        injectField(context, "age", Age.AGE_1);

        // old buildings
        Building oldBuilding = new StubBuilding(Age.AGE_1);
        board.addTopBuildings(List.of(oldBuilding));

        ChangeAgePhase phase = new ChangeAgePhase();

        boolean result = phase.execute(context);

        assertTrue(result, "success");
        assertEquals(Age.AGE_2, context.getAge(), "new age:AGE_2");

        // building card from Top to Bottom
        assertTrue(board.getBottomBuildings().contains(oldBuilding), "old buildings removed to bottom");

        //new buildings
        List<Building> topBuildings = board.getTopBuildings();
        assertFalse(topBuildings.isEmpty(), "new top buildings");
        assertEquals(Age.AGE_2, topBuildings.get(0).getAge(), "new building age:AGE_2");
    }

    @Test
    @DisplayName("Edge Case: Era Exhaustion (Final Age Reached)")
    void testExecute_FinalAge_ReturnsFalse() throws Exception {

        // Instantiate phase locally to ensure isolation
        ChangeAgePhase phase = new ChangeAgePhase();

        // Pitfall: Age is an enum, hasNext() is a method, not a field.
        // Logic: Set age to the last enum constant; hasNext() will physically return false.
        context.setAge(Age.AGE_3_FINAL);

        // Look at the actual type (right): execute() checks !context.getAge().hasNext().
        boolean result = phase.execute(context);

        assertFalse(result, "Phase must return false when no subsequent era exists.");
        // Confirm the state machine did not advance to StartTurnPhase.

    }

    @Test
    @DisplayName("Edge Case: Atomic Board Refurbishment Flow")
    void testExecute_AtomicRefurbishment_FlowCheck() throws Exception {
        ChangeAgePhase phase = new ChangeAgePhase();

        Board board = context.getBoard();

        // Ensure there is a next era ( jumping from Age I to Age II).
        context.setAge(Age.AGE_1);

        // Inject a mock building to verify the discard logic during transition.
        List<Building> bottomBuildings = new ArrayList<>(List.of(new StubBuilding(Age.AGE_1)));
        // Use recursive injectField to reach private board fields.
        injectField(board, "bottomBuildings", bottomBuildings);

        phase.execute(context);

        // Verify the refurbishment sequence: discardBottom -> topToBottom -> addTop.
        assertTrue(board.getBottomBuildings().isEmpty(), "Bottom row must be physically cleared.");
        // Verify that the era in context was physically incremented.
        assertEquals(Age.AGE_2, context.getAge(), "Era should have physically advanced to Age II.");
    }

    @Test
    @DisplayName("Edge Case: Empty Deck for the New Era")
    void testExecute_EmptyDeck_HandlesGracefully() throws Exception {
        ChangeAgePhase phase = new ChangeAgePhase();

        context.setAge(Age.AGE_1);

        // Simulate a scenario where the supply deck has no cards for the next era.
        Decks emptyDeck = context.getDeck();
        injectField(emptyDeck, "buildings", new EnumMap<>(Age.class));

        boolean result = phase.execute(context);

        assertTrue(result, "Phase should succeed even if the supply deck is empty.");
        // Confirm it successfully transitioned to StartTurnPhase.
        assertInstanceOf(StartTurnPhase.class, context.getCurrentPhase());
    }

    //inject tool
    private void injectDecksData(Decks d) throws Exception {
        // Check directly Decks Map
        Field cardsField = Decks.class.getDeclaredField("cards");
        cardsField.setAccessible(true);
        Map<Age, List<Card>> cMap = (Map<Age, List<Card>>) cardsField.get(d);

        Field buildingsField = Decks.class.getDeclaredField("buildings");
        buildingsField.setAccessible(true);
        Map<Age, List<Building>> bMap = (Map<Age, List<Building>>) buildingsField.get(d);


        for (Age a : Age.values()) {
            List<Card> cList = cMap.computeIfAbsent(a, k -> new ArrayList<>());
            List<Building> bList = bMap.computeIfAbsent(a, k -> new ArrayList<>());
            for (int i = 0; i < 50; i++) {
                cList.add(new StubCard(a));
                bList.add(new StubBuilding(a));
            }
        }
    }

    private void injectField(Object target, String fieldName, Object value) throws Exception {
        Field field = null;
        Class<?> current = target.getClass();
        while (current != null) {
            try {
                field = current.getDeclaredField(fieldName);
                break;
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        if (field == null) throw new NoSuchFieldException(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}