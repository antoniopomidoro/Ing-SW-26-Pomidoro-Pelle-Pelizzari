package it.polimi.ingsw.model.cards;

import it.polimi.ingsw.controller.GameConfig;
import it.polimi.ingsw.model.board.Board;
import it.polimi.ingsw.model.board.OrderTile;
import it.polimi.ingsw.model.board.Tile;
import it.polimi.ingsw.model.board.TileSet;
import it.polimi.ingsw.model.effects.ContextualEffect;
import it.polimi.ingsw.model.effects.EventEffect;
import it.polimi.ingsw.model.game.Age;
import it.polimi.ingsw.model.game.GameState;
import it.polimi.ingsw.model.game.TriggerKey;
import it.polimi.ingsw.model.player.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EventTest {
    private class DummyEventEffect implements EventEffect {
        private DummyEventEffect() {
            super();
        }

        @Override
        public boolean applyEffect(GameState state, Age age) {
            return true;
        }
    }

    private GameState state;
    private final DummyEventEffect dummyEventEffect = new DummyEventEffect();
    private Event event = new Event(Age.AGE_1, "id", dummyEventEffect, TriggerKey.START_TURN, 0);

    private void gameStateSetUp() {
        List<Player> players = new ArrayList<>();
        GameConfig gc = new GameConfig();
        List<Card> cards = new ArrayList<>();
        List<Building> buildings = new ArrayList<>();
        OrderTile ot = new OrderTile();
        List<Tile> tiles = new ArrayList<>();
        TileSet t = new TileSet(tiles);
        Board board = new Board(ot, t);
        Decks decks = new Decks(cards, buildings);
        state = new GameState(players, gc, board, decks);
    }

    /*
     * TEST METHODS
     * Repeated tests are used when one or more random values are used
     */

    /** Tests for
     * - onDiscard()
     */
    /* TODO scoppia */
    @DisplayName("The method works correctly if the effect is not null")
    @Test
    public void onDiscardCorrectValues() {
        gameStateSetUp();
        boolean ret = event.onDiscard(state);
        assertTrue(ret);
    }

    @DisplayName("The method returns false if the effect is null")
    @Test
    public void onDiscardNullEffect() {
        event = new Event(Age.AGE_1, "id", null, TriggerKey.START_TURN, 0);
        boolean ret = event.onDiscard(state);
        assertFalse(ret);
    }
}
