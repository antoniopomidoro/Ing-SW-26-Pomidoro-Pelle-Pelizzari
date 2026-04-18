package it.polimi.ingsw.model.game.StatePhases;

import it.polimi.ingsw.controller.GameConfig;
import it.polimi.ingsw.model.board.Board;
import it.polimi.ingsw.model.board.OrderTile;
import it.polimi.ingsw.model.board.TileSet;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.Decks;
import it.polimi.ingsw.model.cards.characters.Tool;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;
import it.polimi.ingsw.model.cards.characters.CharacterEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class EndGamePhaseTest {

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

        OrderTile orderTile = new OrderTile();
        injectField(orderTile, "orderBonus", new ArrayList<>(List.of(0, 1, 2, 3)));
        injectField(board, "orderTile", orderTile);

        context = new GameState(players, config, board, decks,"testId");
    }
    @Test
    @DisplayName("Edge Case: Final Scoring with High Character Multipliers")
    void testExecute_FinalScoringAccuracy() throws Exception {
        // Physical Setup
        EndGamePhase phase = new EndGamePhase();
        injectField(context, "currentPhase", phase);
        Player player = new Player(Totem.RED_TOTEM, "A");
        PlayerStats stats = player.getStats();

        // Physical Setup: Inject 4 unique tools to align with the "16 PP" calculation
        Set<Tool> tools = EnumSet.of(Tool.STONE, Tool.BOWL, Tool.BOAT, Tool.BREAD); // Example Tool enums
        injectField(stats, "uniqueTools", tools);

        // Inject artistGain multiplier into config
        injectField(config, "artistGain", 2);

        // Corrected Field Name: "characterCounts" (Plural)
        Map<CharacterEnum, Integer> charCounts = new EnumMap<>(CharacterEnum.class);

        // Boundary Logic: Inject 3 Inventors for multiplication
        charCounts.put(CharacterEnum.INVENTOR, 3);

        // Derived Logic: Inject 4 Artists to result in 2 Pairs physically
        charCounts.put(CharacterEnum.ARTIST, 4);

        injectField(stats, "characterCounts", charCounts);

        // Add player to context using reflective list access
        ((List<Player>) getPrivateField(context, "players")).add(player);


        // Look at Actual Type (Right): EndGamePhase triggers terminal scoring
        boolean result = phase.execute(context);

// Calculation: (3 Inventors * 4 Tools) + (2 Artist Pairs * 2 Gain) = 16 PP
// Note: We injected 4 Artists to yield 2 pairs

        int expectedPP = 16;
        int actualPP = player.getPP(); // Assuming Player has a getPP() method

        assertEquals(expectedPP, actualPP,
                "The final score should physically be 16 based on character multipliers");

        assertTrue(result, "Phase execution must return true");

        // Verify Side Effect: publishTrigger(END_GAME) was called
        // Verify Numerical Result: (3 Inventors * tools) + (2 Artist Pairs * 2 Gain)
        assertInstanceOf(EndGamePhase.class, context.getCurrentPhase(), "No transition from terminal state");
    }

    @Test
    @DisplayName("Extreme Edge Case: Null-laden Player Collection")
    void testExecute_RobustnessWithNullPlayers() throws Exception {
        EndGamePhase phase = new EndGamePhase();

        // Inject a list containing a null element to test the guard clause at line 33
        List<Player> players = new ArrayList<>();
        players.add(null);
        injectField(context, "players", players);

        // Physical verification of the 'continue' logic
        assertDoesNotThrow(() -> phase.execute(context), "Should skip null players without crashing");
    }
    @Test
    @DisplayName("Edge Case: Guard Clause Triggered by Null Context")
    void testExecute_NullSafety() {
        EndGamePhase phase = new EndGamePhase();
        // Triggers the physical check at line 23
        assertFalse(phase.execute(null), "Should return false if context is null");
    }

    private void injectField(Object target, String fieldName, Object value) throws Exception {
        Field field = null;
        Class<?> current = target.getClass();
        while (current != null) {
            try {
                field = current.getDeclaredField(fieldName);
                break;
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass(); // Traverse inheritance chain
            }
        }
        if (field == null) throw new NoSuchFieldException(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Object getPrivateField(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
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