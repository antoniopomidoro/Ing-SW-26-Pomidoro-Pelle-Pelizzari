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
        players.add(new Player(Totem.BLUE_TOTEM, "Player1"));
        players.add(new Player(Totem.RED_TOTEM, "Player2"));


        orderTile = new OrderTile();

        injectField(orderTile, "orderBonus", new ArrayList<>(List.of(3, 1)));
        injectField(orderTile, "ppPenalty", new ArrayList<>(List.of(0, 0)));


        board = new Board(orderTile, new TileSet(new ArrayList<>()));
        decks = new Decks(new ArrayList<>(), new ArrayList<>());
        for (int i = 0; i < 10; i++) {
            decks.addCard(createMockCard());
        }

        config = new GameConfig();
        injectField(config, "topExtraCards", 2); // 2 + 2(players) = 4
        injectField(config, "startingFood", new ArrayList<>(List.of(0, 0))); // 初始食物设为0方便计算

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
        p1.getStats().setTotemPlacementBonus(2);
        System.out.println(p1.getStats().getTotemPlacementBonus());
        StartTurnPhase startTurnPhase = new StartTurnPhase();

        // 2. Action
        boolean result = startTurnPhase.execute(context);

        // 3. Assert
        assertTrue(result);
        //0+ OrderBonus(3) + TotemBonus(2) = 5
        System.out.println(p1.getFood());

        // board top cards added:2 extra + 2 players
        assertEquals(4, context.getBoard().getTopCards().size(), "Board cards added to 4");
    }

    @Test
    void testPhaseTransitionWhenDeckIsEmpty() throws Exception {

        injectField(context, "age", Age.AGE_1);


        Decks fakeDeck = new Decks(new ArrayList<>(), new ArrayList<>());

        // 3. 【核心物理入侵】直接拿到 Decks 内部的 EnumMap

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


    private void injectField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("jnject failed: " + fieldName, e);
        }
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