package it.polimi.ingsw.model.game.StatePhases;

import it.polimi.ingsw.controller.GameConfig;
import it.polimi.ingsw.model.board.Board;
import it.polimi.ingsw.model.board.OrderTile;
import it.polimi.ingsw.model.board.Tile;
import it.polimi.ingsw.model.board.TileSet;
import it.polimi.ingsw.model.cards.Building;
import it.polimi.ingsw.model.cards.Card;
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
import static it.polimi.ingsw.model.game.StatePhases.PhaseTestSupport.loadConfig;
import static it.polimi.ingsw.model.game.StatePhases.PhaseTestSupport.mockCard;
import static it.polimi.ingsw.model.game.StatePhases.PhaseTestSupport.setField;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link PlayerTurnPhase}: the drafting actions (top/bottom cards and
 * buildings) with their pick-count and affordability guards, plus the tile food
 * bonus applied on entry.
 * <p>
 * Each successful action calls {@code nextPhase}, which once picks are exhausted
 * hands control back through the machine. {@code maxTurns} is pinned to 1 so that
 * cascade settles in the terminal {@link EndGamePhase} without refilling the
 * board, keeping the board assertions below deterministic.
 */
@DisplayName("PlayerTurnPhase")
class PlayerTurnPhaseTest {

    private GameState context;
    private Board board;
    private Player activePlayer;
    private Player otherPlayer;

    @BeforeEach
    void setUp() {
        activePlayer = new Player(Totem.BLUE, "Active");
        otherPlayer = new Player(Totem.RED, "Other");
        board = new Board(new OrderTile(), new TileSet(new ArrayList<>()));

        Decks decks = new Decks(new ArrayList<>(), new ArrayList<>());
        GameConfig config = loadConfig();
        setField(config, "maxTurns", 1);

        context = new GameState(List.of(activePlayer), config, board, decks, "testId");
    }

    /** A phase bound to the active player with explicit, restore-style pick counts. */
    private PlayerTurnPhase phaseWithPicks(int upperPicks, int bottomPicks) {
        return new PlayerTurnPhase(activePlayer, new Tile(), upperPicks, bottomPicks);
    }

    @Test
    @DisplayName("picks a top card into the player's hand and spends an upper pick")
    void picksTopCardSuccessfully() {
        Card card = mockCard(Age.AGE_1);
        board.setTopCards(List.of(card));
        PlayerTurnPhase phase = phaseWithPicks(1, 0);

        boolean result = phase.pickTopCard(context, 0, activePlayer, card.getInstanceId());

        assertTrue(result);
        assertTrue(board.getTopCards().isEmpty(), "card leaves the board");
        assertTrue(activePlayer.getCards().contains(card), "card enters the player's hand");
        assertEquals(0, phase.getUpperPicks(), "an upper pick was spent");
    }

    @Test
    @DisplayName("rejects a top-card pick when no upper picks remain")
    void rejectsTopCardWithoutPicks() {
        PlayerTurnPhase phase = phaseWithPicks(0, 0);

        IllegalMoveException exception = assertThrows(IllegalMoveException.class,
                () -> phase.pickTopCard(context, 0, activePlayer, "anyId"));
        assertEquals("No upper picks remaining", exception.getMessage());
    }

    @Test
    @DisplayName("rejects a pick from anyone other than the active player")
    void rejectsPickFromNonActivePlayer() {
        Card card = mockCard(Age.AGE_1);
        board.setTopCards(List.of(card));
        PlayerTurnPhase phase = phaseWithPicks(1, 0);

        IllegalMoveException exception = assertThrows(IllegalMoveException.class,
                () -> phase.pickTopCard(context, 0, otherPlayer, card.getInstanceId()));
        assertEquals("Only the active player can perform picks", exception.getMessage());
    }

    @Test
    @DisplayName("buys an affordable top building, deducting its food cost")
    void buysTopBuildingSuccessfully() {
        activePlayer.setFood(10);
        Building affordable = building(Age.AGE_1, 5, 0);
        board.setTopBuildings(List.of(affordable));
        PlayerTurnPhase phase = phaseWithPicks(1, 0);

        boolean result = phase.pickTopBuilding(context, 0, activePlayer, affordable.getInstanceId());

        assertTrue(result);
        assertEquals(5, activePlayer.getFood(), "food paid: 10 - 5");
        assertTrue(activePlayer.getBuildings().contains(affordable), "building is owned");
        assertTrue(board.getTopBuildings().isEmpty(), "building leaves the board");
    }

    @Test
    @DisplayName("rejects a top building the player cannot afford")
    void rejectsUnaffordableTopBuilding() {
        activePlayer.setFood(0);
        Building expensive = building(Age.AGE_1, 5, 0);
        board.setTopBuildings(List.of(expensive));
        PlayerTurnPhase phase = phaseWithPicks(1, 0);

        IllegalMoveException exception = assertThrows(IllegalMoveException.class,
                () -> phase.pickTopBuilding(context, 0, activePlayer, expensive.getInstanceId()));
        assertEquals("Player cannot afford this building", exception.getMessage());
        assertEquals(0, activePlayer.getFood(), "food is untouched on failure");
        assertFalse(board.getTopBuildings().isEmpty(), "building stays on the board");
    }

    @Test
    @DisplayName("buys a bottom building, deducting its cost and a bottom pick")
    void buysBottomBuildingSuccessfully() {
        activePlayer.setFood(10);
        Building target = building(Age.AGE_1, 3, 0);
        board.setBottomBuildings(List.of(target));
        PlayerTurnPhase phase = phaseWithPicks(0, 1);

        boolean result = phase.pickBottomBuilding(context, 0, activePlayer, target.getInstanceId());

        assertTrue(result);
        assertEquals(7, activePlayer.getFood(), "food paid: 10 - 3");
        assertEquals(0, phase.getBottomPicks(), "a bottom pick was spent");
        assertTrue(activePlayer.getBuildings().contains(target), "building is owned");
        assertTrue(board.getBottomBuildings().isEmpty(), "building leaves the board");
    }

    @Test
    @DisplayName("rejects a bottom building when no bottom picks remain")
    void rejectsBottomBuildingWithoutPicks() {
        PlayerTurnPhase phase = phaseWithPicks(0, 0);

        IllegalMoveException exception = assertThrows(IllegalMoveException.class,
                () -> phase.pickBottomBuilding(context, 0, activePlayer, "anyId"));
        assertEquals("No bottom picks remaining", exception.getMessage());
    }

    @Test
    @DisplayName("picks a bottom card into the player's hand and spends a bottom pick")
    void picksBottomCardSuccessfully() {
        Card card = mockCard(Age.AGE_1);
        board.setBottomCards(List.of(card));
        PlayerTurnPhase phase = phaseWithPicks(0, 1);

        boolean result = phase.pickBottomCard(context, 0, activePlayer, card.getInstanceId());

        assertTrue(result);
        assertTrue(board.getBottomCards().isEmpty(), "card leaves the board");
        assertTrue(activePlayer.getCards().contains(card), "card enters the player's hand");
        assertEquals(0, phase.getBottomPicks(), "a bottom pick was spent");
    }

    @Test
    @DisplayName("rejects a bottom-card pick when no bottom picks remain")
    void rejectsBottomCardWithoutPicks() {
        PlayerTurnPhase phase = phaseWithPicks(0, 0);

        IllegalMoveException exception = assertThrows(IllegalMoveException.class,
                () -> phase.pickBottomCard(context, 0, activePlayer, "anyId"));
        assertEquals("No bottom picks remaining", exception.getMessage());
    }

    @Test
    @DisplayName("applies the active tile's food bonus on execute")
    void executeAppliesTileFoodBonus() {
        int foodBonus = 5;
        Tile bonusTile = new Tile(null, 1, 0, foodBonus, 2);
        PlayerTurnPhase phase = new PlayerTurnPhase(activePlayer, bonusTile);

        assertTrue(phase.execute(context));

        assertEquals(foodBonus, activePlayer.getFood(), "tile food bonus is granted to the player");
    }
}
