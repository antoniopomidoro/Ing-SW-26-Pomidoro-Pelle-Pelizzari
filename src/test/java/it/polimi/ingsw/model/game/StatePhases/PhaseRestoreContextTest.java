package it.polimi.ingsw.model.game.StatePhases;

import it.polimi.ingsw.model.board.Board;
import it.polimi.ingsw.model.board.OrderTile;
import it.polimi.ingsw.model.board.Tile;
import it.polimi.ingsw.model.board.TileSet;
import it.polimi.ingsw.model.cards.Decks;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the save/load restore path of the state machine: restoring a phase or
 * an order must reinstate state <em>without</em> running phase side effects, in
 * contrast to {@link GameState#setPhase} which auto-executes.
 */
@DisplayName("Phase restore (save/load)")
class PhaseRestoreContextTest {

    private GameState context;
    private Board board;
    private Player p1;
    private Player p2;

    @BeforeEach
    void setUp() {
        p1 = new Player(Totem.BLUE, "P1");
        p2 = new Player(Totem.RED, "P2");
        List<Player> players = new ArrayList<>(List.of(p1, p2));
        board = new Board(new OrderTile(), new TileSet(new ArrayList<>()));
        Decks decks = new Decks(new ArrayList<>(), new ArrayList<>());
        fillCardDeck(decks, 30);
        context = new GameState(players, loadConfig(), board, decks, "testId");
    }

    @Test
    @DisplayName("the constructor restores SetupPhase without executing it")
    void constructorRestoresSetupPhaseWithoutSideEffects() {
        assertInstanceOf(SetupPhase.class, context.getCurrentPhase(), "starts in SetupPhase");
        assertTrue(board.getTopCards().isEmpty(), "no board population happened");
        assertTrue(board.getBottomCards().isEmpty(), "no board population happened");
        assertEquals(0, p1.getFood(), "no starting food was handed out");
        assertEquals(0, p2.getFood(), "no starting food was handed out");
    }

    @Test
    @DisplayName("restorePhase swaps the phase without triggering its execute")
    void restorePhaseDoesNotExecute() {
        // EndGamePhase.execute() would award prestige points; restore must not run it.
        context.restorePhase(new EndGamePhase());

        assertInstanceOf(EndGamePhase.class, context.getCurrentPhase());
        assertEquals(0, p1.getPP(), "restoring a phase must not score the game");
        assertEquals(0, p2.getPP(), "restoring a phase must not score the game");
    }

    @Test
    @DisplayName("restoreOrderTileOrder keeps the saved order verbatim")
    void restoreOrderTileOrderPreservesGivenOrder() {
        p2.setConnected(false);
        // Unlike setOrderTileOrder (which sorts connected players first), restore is verbatim.
        context.restoreOrderTileOrder(List.of(p2, p1));

        assertEquals(List.of(p2, p1), context.getOrderTileOrder(),
                "saved order is reinstated without reordering disconnected players");
    }

    @Test
    @DisplayName("the PlayerTurnPhase restore constructor keeps pick counts without executing")
    void playerTurnPhaseRestoreKeepsPickCounts() {
        PlayerTurnPhase restored = new PlayerTurnPhase(p1, new Tile(), 2, 3);

        assertEquals(2, restored.getUpperPicks(), "upper picks restored verbatim");
        assertEquals(3, restored.getBottomPicks(), "bottom picks restored verbatim");
        assertEquals(p1, restored.getActivePlayer(), "active player restored");
    }
}
