package it.polimi.ingsw.model.game.StatePhases;

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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link TurnPhase}: scanning the board tiles to hand control to each
 * connected occupier, granting extra picks, and ending the turn.
 */
@DisplayName("TurnPhase")
class TurnPhaseTest {

    private GameState context;
    private OrderTile orderTile;
    private Player p1;
    private Player p2;

    @BeforeEach
    void setUp() {
        p1 = new Player(Totem.RED, "P1");
        p2 = new Player(Totem.BLUE, "P2");
        List<Player> players = new ArrayList<>(List.of(p1, p2));

        // Neutral order tile (no bonus, no penalty) so downstream phases never starve.
        orderTile = new OrderTile(new ArrayList<>(List.of(0, 0)), new ArrayList<>(List.of(0, 0)));
        Board board = new Board(orderTile, new TileSet(new ArrayList<>()));

        Decks decks = new Decks(new ArrayList<>(), new ArrayList<>());
        fillCardDeck(decks, 30);

        context = new GameState(players, loadConfig(), board, decks, "testId");
    }

    /** Installs a board whose tiles are the given ones and a single buyable top card. */
    private void useBoard(Tile... tiles) {
        Board board = new Board(orderTile, new TileSet(new ArrayList<>(List.of(tiles))));
        board.setTopCards(new ArrayList<>(List.of(mockCard(Age.AGE_1))));
        context.setBoard(board);
    }

    @Test
    @DisplayName("hands control to the occupier of a connected tile and advances the scan index")
    void transitionsToPlayerTurnOnConnectedOccupiedTile() {
        Tile occupied = new Tile(p1, 1, 0); // occupied by connected p1, one upper pick
        useBoard(new Tile(), occupied, new Tile());
        context.setCurrentTileIndex(0);

        assertTrue(new TurnPhase().execute(context));

        assertEquals(2, context.getCurrentTileIndex(), "scan resumes just after the processed tile");
        assertInstanceOf(PlayerTurnPhase.class, context.getCurrentPhase(),
                "control passes to the tile occupier");
    }

    @Test
    @DisplayName("skips a disconnected occupier and stops on the next connected one")
    void skipsDisconnectedOccupier() {
        p1.setConnected(false);
        Tile skipped = new Tile(p1, 1, 0);  // disconnected occupier, must be skipped
        Tile active = new Tile(p2, 1, 0);   // connected occupier, takes the turn
        useBoard(skipped, active);
        context.setCurrentTileIndex(0);

        new TurnPhase().execute(context);

        assertEquals(2, context.getCurrentTileIndex(), "scan resumes after the connected tile");
        assertInstanceOf(PlayerTurnPhase.class, context.getCurrentPhase());
        assertEquals(p2, ((PlayerTurnPhase) context.getCurrentPhase()).getActivePlayer(),
                "the connected occupier is the active player");
    }

    @Test
    @DisplayName("grants an extra-pick turn when no tile is occupied but a player earned one")
    void grantsExtraPickTurn() {
        p1.getStats().setExtraUpperPick(1);
        context.setTurnOrder(List.of(p1, p2));
        useBoard(); // no tiles
        context.setCurrentTileIndex(0);

        new TurnPhase().execute(context);

        assertInstanceOf(PlayerTurnPhase.class, context.getCurrentPhase(),
                "the player with an extra pick gets a bonus turn");
        assertEquals(1, context.getExtraIndex(), "the extra-pick cursor advanced past p1");
    }

    @Test
    @DisplayName("ends the turn when there is nothing left to process")
    void endsTurnWhenNothingToProcess() {
        useBoard(); // no occupied tiles, no extra picks
        context.setTurnOrder(new ArrayList<>());
        context.setTurn(1);
        context.setCurrentTileIndex(0);

        new TurnPhase().execute(context);

        // END_TURN runs and rolls the machine into the next turn's StartTurnPhase.
        assertEquals(2, context.getTurn(), "the turn counter advanced");
        assertInstanceOf(StartTurnPhase.class, context.getCurrentPhase());
    }

    @Test
    @DisplayName("returns false for a null context")
    void returnsFalseForNullContext() {
        assertFalse(new TurnPhase().execute(null));
    }
}
