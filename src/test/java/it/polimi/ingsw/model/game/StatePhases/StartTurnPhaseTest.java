package it.polimi.ingsw.model.game.StatePhases;

import it.polimi.ingsw.controller.GameConfig;
import it.polimi.ingsw.model.board.Board;
import it.polimi.ingsw.model.board.OrderTile;
import it.polimi.ingsw.model.board.Tile;
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

import static it.polimi.ingsw.model.game.StatePhases.PhaseTestSupport.fillCardDeck;
import static it.polimi.ingsw.model.game.StatePhases.PhaseTestSupport.loadConfig;
import static it.polimi.ingsw.model.game.StatePhases.PhaseTestSupport.mockCard;
import static it.polimi.ingsw.model.game.StatePhases.PhaseTestSupport.setField;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link StartTurnPhase}: board refill, age change on deck exhaustion,
 * order-tile food bonuses and tile occupation (including its guard clauses).
 */
@DisplayName("StartTurnPhase")
class StartTurnPhaseTest {

    private GameState context;
    private Player p1;
    private Player p2;
    private OrderTile orderTile;
    private GameConfig config;

    @BeforeEach
    void setUp() {
        p1 = new Player(Totem.BLUE, "Player1");
        p2 = new Player(Totem.RED, "Player2");
        List<Player> players = new ArrayList<>(List.of(p1, p2));

        // Order bonuses: 1st position +3 food, 2nd position +1 food, no penalties.
        orderTile = new OrderTile(new ArrayList<>(List.of(3, 1)), new ArrayList<>(List.of(0, 0)));
        Board board = new Board(orderTile, new TileSet(new ArrayList<>()));

        Decks decks = new Decks(new ArrayList<>(), new ArrayList<>());
        fillCardDeck(decks, 30);

        config = loadConfig();
        context = new GameState(players, config, board, decks, "testID");
    }

    /** Replaces the board with one backed by the given tiles. */
    private void useTiles(Tile... tiles) {
        context.setBoard(new Board(orderTile, new TileSet(new ArrayList<>(List.of(tiles)))));
    }

    @Test
    @DisplayName("from the second turn on, applies order bonus plus totem placement bonus")
    void appliesOrderAndTotemBonusFromSecondTurn() {
        context.setTurn(2);
        p1.getStats().setTotemPlacementBonus(2);

        assertTrue(new StartTurnPhase().execute(context));

        assertEquals(3 + 2, p1.getFood(), "first player: order bonus 3 + totem bonus 2");
        assertEquals(1, p2.getFood(), "second player: order bonus 1, no totem bonus");
        assertEquals(config.getTopCardsQuantity(context.getPlayers()), context.getBoard().getTopCards().size(),
                "board is refilled to the configured top-card quantity");
    }

    @Test
    @DisplayName("does not hand out food on the very first turn")
    void doesNotApplyBonusOnFirstTurn() {
        context.setTurn(1);

        new StartTurnPhase().execute(context);

        assertEquals(0, p1.getFood(), "no order bonus on turn 1");
        assertEquals(0, p2.getFood(), "no order bonus on turn 1");
    }

    @Test
    @DisplayName("changes age when the current deck runs out mid-refill")
    void changesAgeWhenDeckExhausted() {
        // Force a refill of 4 cards but supply only 2 for the current age, so the
        // refill drains AGE_1 and rolls over into AGE_2.
        setField(config, "topExtraCards", 2);
        Decks scarce = new Decks(new ArrayList<>(), new ArrayList<>());
        scarce.getCards().get(Age.AGE_1).add(mockCard(Age.AGE_1));
        scarce.getCards().get(Age.AGE_1).add(mockCard(Age.AGE_1));
        scarce.getCards().get(Age.AGE_2).add(mockCard(Age.AGE_2));
        scarce.getCards().get(Age.AGE_2).add(mockCard(Age.AGE_2));
        context.setDeck(scarce);
        context.setAge(Age.AGE_1);

        new StartTurnPhase().execute(context);

        assertEquals(Age.AGE_2, context.getAge(), "deck exhaustion advances the age");
        assertInstanceOf(StartTurnPhase.class, context.getCurrentPhase(),
                "the new age re-enters StartTurnPhase");
    }

    @Test
    @DisplayName("occupy rejects an out-of-range tile index")
    void occupyRejectsInvalidIndex() {
        useTiles(new Tile());
        StartTurnPhase phase = new StartTurnPhase();

        IllegalMoveException exception = assertThrows(IllegalMoveException.class,
                () -> phase.occupyOfferTrailTile(context, 5, p1));
        assertEquals("Invalid tile index", exception.getMessage());
    }

    @Test
    @DisplayName("occupy rejects a player acting out of turn")
    void occupyRejectsWrongTurn() {
        useTiles(new Tile());
        // Make p2 the player currently expected to place a totem.
        context.restoreOrderTileOrder(List.of(p2));
        context.setCurrentPlayerOrderIndex(0);
        assertSame(p2, context.getCurrentOrderTileOrderPlayer(), "p2 should be the current player");
        StartTurnPhase phase = new StartTurnPhase();

        IllegalMoveException exception = assertThrows(IllegalMoveException.class,
                () -> phase.occupyOfferTrailTile(context, 0, p1));
        assertEquals("Not your turn", exception.getMessage());
    }

    @Test
    @DisplayName("occupy rejects an already occupied tile")
    void occupyRejectsOccupiedTile() {
        Tile occupied = new Tile();
        occupied.occupy(p2);
        useTiles(occupied);
        context.restoreOrderTileOrder(List.of(p1));
        context.setCurrentPlayerOrderIndex(0);
        StartTurnPhase phase = new StartTurnPhase();

        IllegalMoveException exception = assertThrows(IllegalMoveException.class,
                () -> phase.occupyOfferTrailTile(context, 0, p1));
        assertEquals("Tile already occupied", exception.getMessage());
    }

    @Test
    @DisplayName("a single player occupying the last tile drives a full turn cycle back to StartTurnPhase")
    void occupyLastTileCompletesTurnCycle() {
        useTiles(new Tile());
        context.restoreOrderTileOrder(List.of(p1));
        context.setCurrentPlayerOrderIndex(0);
        StartTurnPhase phase = new StartTurnPhase();

        boolean result = phase.occupyOfferTrailTile(context, 0, p1);

        // With no further players to place, the machine runs TURN → PLAYER_TURN →
        // END_TURN and lands on the next turn's StartTurnPhase.
        assertTrue(result);
        assertInstanceOf(StartTurnPhase.class, context.getCurrentPhase());
    }
}
