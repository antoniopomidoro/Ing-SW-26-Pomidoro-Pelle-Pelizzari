package it.polimi.ingsw.model.game.StatePhases;

import it.polimi.ingsw.controller.GameConfig;
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
import static it.polimi.ingsw.model.game.StatePhases.PhaseTestSupport.setField;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link EndTurnPhase}: the end-of-game branch on the final turn and
 * the normal turn rollover (tile cleanup, turn increment, transition to the next
 * {@link StartTurnPhase}).
 */
@DisplayName("EndTurnPhase")
class EndTurnPhaseTest {

    private GameState context;
    private OrderTile orderTile;
    private GameConfig config;
    private Player p1;

    @BeforeEach
    void setUp() {
        p1 = new Player(Totem.BLUE, "Player1");
        Player p2 = new Player(Totem.RED, "Player2");
        List<Player> players = new ArrayList<>(List.of(p1, p2));

        orderTile = new OrderTile(new ArrayList<>(List.of(0, 0)), new ArrayList<>(List.of(0, 0)));
        Board board = new Board(orderTile, new TileSet(new ArrayList<>()));

        Decks decks = new Decks(new ArrayList<>(), new ArrayList<>());
        fillCardDeck(decks, 30);

        config = loadConfig();
        context = new GameState(players, config, board, decks, "testId");
    }

    @Test
    @DisplayName("transitions to EndGamePhase once the max turn is reached")
    void transitionsToEndGameOnMaxTurns() {
        setField(config, "maxTurns", 5);
        context.setTurn(5);

        assertTrue(new EndTurnPhase().execute(context));

        assertInstanceOf(EndGamePhase.class, context.getCurrentPhase(),
                "reaching maxTurns ends the game");
    }

    @Test
    @DisplayName("on a normal turn end, frees tiles, increments the turn and starts the next one")
    void rollsOverToNextTurn() {
        Tile occupied = new Tile(p1, 0, 0);
        context.setBoard(new Board(orderTile, new TileSet(new ArrayList<>(List.of(occupied)))));
        context.setTurn(1);
        context.setTurnOrder(List.of(p1));

        new EndTurnPhase().execute(context);

        assertFalse(occupied.isOccupied(), "every tile is freed for the next turn");
        assertEquals(2, context.getTurn(), "the turn counter advances by one");
        assertTrue(context.getTurnOrder().isEmpty(), "turn order is cleared after rebuilding the order tile");
        assertInstanceOf(StartTurnPhase.class, context.getCurrentPhase(), "the next turn begins");
    }

    @Test
    @DisplayName("returns false for a null context")
    void returnsFalseForNullContext() {
        assertFalse(new EndTurnPhase().execute(null));
    }
}
