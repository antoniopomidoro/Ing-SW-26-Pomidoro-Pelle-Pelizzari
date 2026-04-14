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
                new Player(Totem.RED_TOTEM, "P1"),
                new Player(Totem.BLUE_TOTEM, "P2")
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

        System.out.println("Before Execute - Current Phase: " + context.getCurrentPhase().getClass().getSimpleName());

        TurnPhase turnPhase = new TurnPhase();
        boolean result = turnPhase.execute(context);

        System.out.println("After Execute - Current Phase: " + context.getCurrentPhase().getClass().getSimpleName());
        System.out.println("Result: " + result + ", Index: " + context.getCurrentTileIndex());


        assertTrue(result, "execute return true");
        // hit index=1 Tile，next index: 1+1=2
        assertEquals(2, context.getCurrentTileIndex(), "wrong index");
        assertTrue(context.getCurrentPhase() instanceof PlayerTurnPhase, "wrong phase");
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