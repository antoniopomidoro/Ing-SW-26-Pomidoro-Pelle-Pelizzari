package it.polimi.ingsw.model.game.StatePhases;

import it.polimi.ingsw.controller.GameConfig;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;
import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.cards.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.lang.reflect.Field;

class TurnPhaseTest {
    private GameState context;
    private Board board;
    private List<Player> players;
    private Decks decks;
    private GameConfig config;

    @BeforeEach
    void setUp() throws Exception {
        players = new ArrayList<>(List.of(
                new Player(Totem.RED, "P1"),
                new Player(Totem.BLUE, "P2")
        ));
        board = new Board(new OrderTile(), new TileSet(new ArrayList<>()));
        config = new GameConfig();
        decks = new Decks(new ArrayList<>(), new ArrayList<>());

        injectDecksData(decks);

        safeInjectField(config, "bottomExtraCards", 1);
        safeInjectField(config, "topExtraCards", 1);
        safeInjectField(config, "startingFood", new ArrayList<>(List.of(10, 10)));

        context = new GameState(players, config, board, decks,"testId");

        safeInjectField(context, "turnOrder", new ArrayList<>(players));
        safeInjectField(context, "board", board);
    }

    @Test
    @DisplayName("Verify TurnPhase phase change")
    void testTurnPhaseExecution() throws Exception {
        Player realPlayer = context.getTurnOrder().get(0);
        safeInjectField(realPlayer, "isConnected", true); //

        Tile occupiedTile = new Tile();
        safeInjectField(occupiedTile, "occupier", realPlayer);
        safeInjectField(occupiedTile, "isOccupied", true);

        Board internalBoard = context.getBoard();
        safeInjectField(occupiedTile, "upperPicks", 1);

        Card mockCard = createMockCard(Age.AGE_1);
        List<Card> mockCards = new ArrayList<>(List.of(mockCard));

        // !board.getTopCards().isEmpty() return true，canPickTop return true
        safeInjectField(internalBoard, "topCards", mockCards);

        //  occupiedTile:index=1
        List<Tile> tileList = new ArrayList<>(List.of(new Tile(), occupiedTile, new Tile()));

        //  TileSet instance->private List<Tile> tiles
        //  Board->TileSet
        TileSet tileSet = internalBoard.getTiles();
        // prepared tileList injected to TileSet instance
        safeInjectField(tileSet, "tiles", tileList);

        // start point
        safeInjectField(context, "currentTileIndex", 0);

        StartTurnPhase phase = new StartTurnPhase();
        safeInjectField(context,"currentPhase", phase);

        assertTrue(context.getCurrentPhase() instanceof StartTurnPhase);//before execute:playerturnphase
        TurnPhase turnPhase = new TurnPhase();

        boolean result = turnPhase.execute(context);

        assertTrue(result, "execute return true");
        // hit index=1 Tile，next index: 1+1=2
        assertEquals(2, context.getCurrentTileIndex(), "wrong index");
        assertTrue(context.getCurrentPhase() instanceof PlayerTurnPhase, "wrong phase");
    }
    /**
     * Boundary: Skip Disconnected but Proceed to Next.
     * Logic: If occupier is disconnected, skip via index++ without returning false.
     *
     */
    @Test
    @DisplayName("Boundary: TurnPhase to PlayerTurnPhase transition with Mock Cards")
    void testExecute_TransitionWithMockCards() throws Exception {
        TurnPhase phase = new TurnPhase();
        // Step 1: Physical Setup - Ensure P2 is connected and occupies the second tile
        Player p1_dc = context.getPlayers().get(0);
        Player p2_online = context.getPlayers().get(1);

        // Mark P1 as disconnected to trigger index++ skip logic
        safeInjectField(p1_dc, "isConnected", false);
        safeInjectField(p2_online, "isConnected", true);

        //  Step 2: Tile Configuration - Set picks to satisfy PlayerTurnPhase.nextPhase
        Tile t0 = new Tile();
        t0.occupy(p1_dc); // This tile will be physically skipped

        Tile t1 = new Tile();
        t1.occupy(p2_online);
        // Inject picks so PlayerTurnPhase does not immediately return to TurnPhase
        safeInjectField(t1, "upperPicks", 1);
        safeInjectField(t1, "bottomPicks", 0);

        // Step 3: Resource Injection - Populate Board using createMockCard
        // nextPhase() checks canPickTop, which requires !board.getTopCards().isEmpty()
        List<Card> mockCards = new ArrayList<>();
        // Using your existing helper method to bypass Card constructor constraints
        mockCards.add(createMockCard(Age.AGE_1));

        Board board = context.getBoard();
        safeInjectField(board, "topCards", mockCards);
        safeInjectField(board, "tiles", new TileSet(List.of(t0, t1)));

        // --- Step 4: Reset initial scan index to 0 ---
        safeInjectField(context, "currentTileIndex", 0);

        // Step 5: Physical Execution
        boolean result = phase.execute(context);

        //  Step 6: Physical Verification
        // 1. Verify that a valid active player was found
        assertTrue(result, "Execution should return true after finding an online player.");

        // 2. Verify we stay in PlayerTurnPhase instead of falling back to TurnPhase
        assertInstanceOf(PlayerTurnPhase.class, context.getCurrentPhase(),
                "The system must stay in PlayerTurnPhase when resources and picks are available.");

        // 3. Verify pointer increment: Processing index 1 should persist index 2
        assertEquals(2, context.getCurrentTileIndex(), "The saved index should be (processed_index + 1).");
    }
    // inject tools
    private void safeInjectField(Object target, String fieldName, Object value) throws Exception {
        Field field = null;
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                field = clazz.getDeclaredField(fieldName);
                break;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        if (field == null) throw new NoSuchFieldException("Field " + fieldName + " not found in " + target.getClass().getSimpleName());
        field.setAccessible(true);
        field.set(target, value);
    }
    private void injectDecksData(Decks d) throws Exception {
        Field cardsField = Decks.class.getDeclaredField("cards");
        cardsField.setAccessible(true);
        Map<Age, List<Card>> cardsMap = (Map<Age, List<Card>>) cardsField.get(d);
        for (Age age : Age.values()) {
            List<Card> list = cardsMap.computeIfAbsent(age, k -> new ArrayList<>());
            for(int i=0; i<50; i++) list.add(createMockCard(age));
        }
    }

    private Card createMockCard(Age age) {
        return new Card() {
            @Override public Age getAge() { return age; }
            @Override public boolean addToDeck(Decks d) { return d.addCard(this); }
            @Override public CardCategory getCategory() { return CardCategory.CHARACTER; }
            @Override public boolean isBuyable() { return true; }
            @Override public int getResolutionPriority() { return 0; }
        };
    }
}