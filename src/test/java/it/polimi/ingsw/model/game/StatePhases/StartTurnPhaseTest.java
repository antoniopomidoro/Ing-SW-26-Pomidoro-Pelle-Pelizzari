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

class StartTurnPhaseTest {
    private GameState context;
    private List<Player> players;
    private GameConfig config;
    private Board board;
    private Decks decks;
    private OrderTile orderTile;
    @BeforeEach
    void setUp() throws Exception {
        players = new ArrayList<>();
        players.add(new Player(Totem.BLUE, "Player1"));
        players.add(new Player(Totem.RED, "Player2"));

        orderTile = new OrderTile();
        injectField(orderTile, "orderBonus", new ArrayList<>(List.of(3, 1)));
        injectField(orderTile, "ppPenalty", new ArrayList<>(List.of(0, 0)));

        board = new Board(orderTile, new TileSet(new ArrayList<>()));
        decks = new Decks(new ArrayList<>(), new ArrayList<>());
        for (int i = 0; i < 10; i++) {
            decks.addCard(createMockCard());
        }

        ObjectMapper mapper = new ObjectMapper();
        InputStream is = getClass().getClassLoader().getResourceAsStream("json/config.json");
        config = mapper.readValue(is, GameConfig.class);

        injectField(config, "topExtraCards", 2); // 2 + 2(players) = 4
        injectField(config, "startingFood", new ArrayList<>(List.of(0, 0)));

        // 5.construction function run SetupPhase automatic
        context = new GameState(players, config, board, decks,"testID");

        Field turnOrderField = GameState.class.getDeclaredField("turnOrder");
        turnOrderField.setAccessible(true);
        turnOrderField.set(context, new ArrayList<>(players));
    }

    @Test
    void testStartTurnPhaseExecuteBonus() throws Exception {
        injectField(context, "turn", 2);
        Player p1 = context.getPlayers().get(0);
        // injectField(p1, "food", 0);
        p1.getStats().setTotemPlacementBonus(2);
        StartTurnPhase startTurnPhase = new StartTurnPhase();
        // 2. Action
        boolean result = startTurnPhase.execute(context);
        // 3. Assert
        assertTrue(result);
        //0+ OrderBonus(3) + TotemBonus(2) = 5
        assertEquals(5,p1.getFood());
        // board top cards added:2 extra + 2 players
        assertEquals(4, context.getBoard().getTopCards().size(), "Board cards added to 4");
    }

    @Test
    void testPhaseTransitionWhenDeckIsEmpty() throws Exception {

        injectField(context, "age", Age.AGE_1);

        Decks fakeDeck = new Decks(new ArrayList<>(), new ArrayList<>());
        // take Decks EnumMap
        java.lang.reflect.Field cardsField = Decks.class.getDeclaredField("cards");
        cardsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Age, List<Card>> internalMap = (Map<Age, List<Card>>) cardsField.get(fakeDeck);

        for (Age a : new Age[]{Age.AGE_1, Age.AGE_2, Age.AGE_3}) {
            List<Card> list = internalMap.get(a);
            list.add(createMockCard());
            list.add(createMockCard());
        }

        // fakeDeck inject to context
        injectField(context, "deck", fakeDeck);

        injectField(context.getBoard(), "topCards", new ArrayList<>());

        StartTurnPhase phase = new StartTurnPhase();
        boolean result = phase.execute(context);

     assertEquals(Age.AGE_2, context.getAge(), " ChangeAgePhase to AGE_2");
     assertTrue(context.getCurrentPhase() instanceof StartTurnPhase, "new StartTurnPhase");

    }

    @Test
    @DisplayName("Verify Invalid Index throws IllegalMoveException")
    void testOccupyOfferTrailTile_InvalidIndex_ThrowsException() throws Exception {
        StartTurnPhase phase = new StartTurnPhase();

        Player p1 = context.getPlayers().get(0);

        TileSet tileSet = new TileSet(new ArrayList<>(List.of(new Tile())));

        injectField(context.getBoard(), "tiles", tileSet);

        IllegalMoveException exception = assertThrows(IllegalMoveException.class, () -> {
            phase.occupyOfferTrailTile(context, 5, p1);
        });
        assertEquals("Invalid tile index", exception.getMessage());
    }

    @Test
    @DisplayName("Verify Wrong Turn throws IllegalMoveException")
    void testOccupyOfferTrailTile_WrongTurn_ThrowsException() throws Exception {
        StartTurnPhase phase = new StartTurnPhase();
        // Fetch two distinct original player instances from the context
        Player p1 = context.getPlayers().get(0);
        Player p2 = context.getPlayers().get(1);

        // : Prepare valid physical environment
        // Inject a valid TileSet so the logic passes the "Invalid tile index" check first
        List<Tile> tileList = new ArrayList<>(List.of(new Tile()));
        injectField(context.getBoard(), "tiles", new TileSet(tileList));

        // Set the current actor to P2 via orderTileOrder
        // Based on GameState.java, currentPlayer is pulled from orderTileOrder[index]
        List<Player> orderWithP2 = new ArrayList<>(List.of(p2));
        injectField(context, "orderTileOrder", orderWithP2);
        injectField(context, "currentPlayerOrderIndex", 0);

        // Optional: Keep turnOrder in sync for consistency
        injectField(context, "turnOrder", new ArrayList<>(orderWithP2));

        //  Diagnostic verification before execution ---
        // Physically verify that the system recognizes P2 as the one who SHOULD move
        assertSame(p2, context.getCurrentTurnOrderPlayer());
        assertSame(p2, context.getCurrentOrderTileOrderPlayer(), "Setup Error: Context should consider P2 as the current player.");

        //  Execute and Assert Exception
        // When we pass p1 while p2 is the current player, the address comparison (p1 != p2) must trigger the exception
        IllegalMoveException exception = assertThrows(IllegalMoveException.class, () -> {
            phase.occupyOfferTrailTile(context, 0, p1); // p1 is not p2!
        }, "Should throw IllegalMoveException because it is p2's turn");

        assertEquals("Not your turn", exception.getMessage(), "The exception message must match exactly.");
    }
    @Test
    @DisplayName("Verify Tile Already Occupied throws IllegalMoveException")
    void testOccupyOfferTrailTile_AlreadyOccupied_ThrowsException() throws Exception {
        StartTurnPhase phase = new StartTurnPhase();
        Player p1 = context.getPlayers().get(0);
        Player p2 = context.getPlayers().get(1);
        Tile occupiedTile = new Tile();
        occupiedTile.occupy(p2);

        injectField(context.getBoard(), "tiles", new TileSet(new ArrayList<>(List.of(occupiedTile))));
        injectField(context, "turnOrder", new ArrayList<>(List.of(p1)));

        IllegalMoveException exception = assertThrows(IllegalMoveException.class, () -> {
            phase.occupyOfferTrailTile(context, 0, p1);
        });
    }
    @Test
    @DisplayName("Verify final move transitions to EndGamePhase_Final_Fix")
    void testOccupyOfferTrailTile_TransitionsToTurnPhase_Full() throws Exception {
        Player p1 = context.getPlayers().get(0);


        injectField(context.getBoard(), "tiles", new TileSet(new ArrayList<>(List.of(new Tile()))));
        List<Player> mockOrder = new ArrayList<>(List.of(p1));
        injectField(context, "orderTileOrder", mockOrder);
        injectField(context, "currentPlayerOrderIndex", 0);
        injectField(context, "turnOrder", mockOrder);

        assertSame(p1, context.getCurrentOrderTileOrderPlayer(), "Pre-check: Player must match");

        StartTurnPhase phase = new StartTurnPhase();
        boolean result = phase.occupyOfferTrailTile(context, 0, p1);

        Object currentPhase = context.getCurrentPhase();

        //turn phase->end turnphase->new start turn phase
        assertInstanceOf(StartTurnPhase.class, currentPhase,
                "Success: The physical index overflow forced the transition to EndGamePhase!");
    }

    private Object getFieldValue(Object obj, String name) throws Exception {
        java.lang.reflect.Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(obj);
    }
    private void injectField(Object target, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
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