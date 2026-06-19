package it.polimi.ingsw.model.game.StatePhases;

import it.polimi.ingsw.controller.GameConfig;
import it.polimi.ingsw.model.board.Board;
import it.polimi.ingsw.model.board.OrderTile;
import it.polimi.ingsw.model.board.TileSet;
import it.polimi.ingsw.model.cards.Decks;
import it.polimi.ingsw.model.game.Age;
import it.polimi.ingsw.model.game.GameState;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Totem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static it.polimi.ingsw.model.game.StatePhases.PhaseTestSupport.building;
import static it.polimi.ingsw.model.game.StatePhases.PhaseTestSupport.fillCardDeck;
import static it.polimi.ingsw.model.game.StatePhases.PhaseTestSupport.loadConfig;
import static it.polimi.ingsw.model.game.StatePhases.PhaseTestSupport.setField;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link SetupPhase}: starting-food distribution, board population and
 * the transition into {@link StartTurnPhase}.
 */
@DisplayName("SetupPhase")
class SetupPhaseTest {

    private GameState context;
    private List<Player> players;
    private Board board;
    private Decks decks;
    private GameConfig config;

    @BeforeEach
    void setUp() {
        players = new ArrayList<>(List.of(
                new Player(Totem.RED, "Player1"),
                new Player(Totem.BLUE, "Player2")));
        board = new Board(new OrderTile(), new TileSet(new ArrayList<>()));
        decks = new Decks(new ArrayList<>(), new ArrayList<>());
        fillCardDeck(decks, 30);
        config = loadConfig();
        context = new GameState(players, config, board, decks, "testId");
    }

    @Test
    @DisplayName("distributes starting food by turn order")
    void distributesStartingFoodByTurnOrder() {
        // SetupPhase shuffles the turn order and hands out config.startingFood = [2, 3, ...]
        // by turn-order position, so the two players end up holding {2, 3} in some order.
        new SetupPhase().execute(context);

        int food0 = players.get(0).getFood();
        int food1 = players.get(1).getFood();
        int firstFood = config.getStartingFood().get(0);
        int secondFood = config.getStartingFood().get(1);

        assertEquals(firstFood + secondFood, food0 + food1, "total starting food distributed");
        assertTrue((food0 == firstFood && food1 == secondFood)
                        || (food0 == secondFood && food1 == firstFood),
                "each player gets a distinct starting-food value by turn order");
    }

    @Test
    @DisplayName("fills the board with the configured number of cards")
    void fillsBoardWithConfiguredCardQuantities() {
        new SetupPhase().execute(context);

        assertEquals(config.getTopCardsQuantity(players), board.getTopCards().size(),
                "top cards = topExtraCards + players");
        assertEquals(config.getBottomCardsQuantity(players), board.getBottomCards().size(),
                "bottom cards = bottomExtraCards + players");
    }

    @Test
    @DisplayName("adds the age's buildings to the board top row")
    void addsAgeBuildingsToBoard() {
        int expected = config.getBuildingsCount(players.size(), Age.AGE_1);
        for (int i = 0; i < expected; i++) {
            decks.getAllBuildings().get(Age.AGE_1).add(building(Age.AGE_1, 0, 0));
        }

        new SetupPhase().execute(context);

        assertEquals(expected, board.getTopBuildings().size(),
                "board buildings must match config.getBuildingsCount");
    }

    @Test
    @DisplayName("transitions to StartTurnPhase once setup completes")
    void transitionsToStartTurnPhase() {
        new SetupPhase().execute(context);

        assertInstanceOf(StartTurnPhase.class, context.getCurrentPhase(),
                "setup hands over to StartTurnPhase");
    }

    @Test
    @DisplayName("throws when the starting-food list is shorter than the player count")
    void throwsWhenStartingFoodShorterThanPlayers() {
        List<Player> fivePlayers = new ArrayList<>(List.of(
                new Player(Totem.BLUE, "P1"),
                new Player(Totem.RED, "P2"),
                new Player(Totem.WHITE, "P3"),
                new Player(Totem.BLACK, "P4"),
                new Player(Totem.YELLOW, "P5")));
        context.setPlayers(fivePlayers);
        context.restoreOrderTileOrder(fivePlayers);
        // Only four food values for five turn-order positions.
        setField(config, "startingFood", new ArrayList<>(List.of(1, 2, 3, 4)));

        IndexOutOfBoundsException exception = assertThrows(IndexOutOfBoundsException.class,
                () -> new SetupPhase().execute(context));
        assertEquals("Index 4 out of bounds for length 4", exception.getMessage());
    }

    @Test
    @DisplayName("returns false for a null context")
    void returnsFalseForNullContext() {
        assertFalse(new SetupPhase().execute(null), "null context must be rejected");
    }
}
