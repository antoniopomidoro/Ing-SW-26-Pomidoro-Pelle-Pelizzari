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
}