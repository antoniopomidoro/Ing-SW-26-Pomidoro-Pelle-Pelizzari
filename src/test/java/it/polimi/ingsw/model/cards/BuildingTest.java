package it.polimi.ingsw.model.cards;


import it.polimi.ingsw.controller.GameConfig;
import it.polimi.ingsw.model.board.Board;
import it.polimi.ingsw.model.board.OrderTile;
import it.polimi.ingsw.model.board.Tile;
import it.polimi.ingsw.model.board.TileSet;
import it.polimi.ingsw.model.cards.characters.Artist;
import it.polimi.ingsw.model.cards.characters.Builder;
import it.polimi.ingsw.model.cards.characters.CharacterEnum;
import it.polimi.ingsw.model.effects.ContextualEffect;
import it.polimi.ingsw.model.game.Age;
import it.polimi.ingsw.model.game.GameState;
import it.polimi.ingsw.model.game.TriggerKey;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Totem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BuildingTest {
    private class DummyContextualEffect implements ContextualEffect {
        private DummyContextualEffect() {
            super();
        }
    }

    private Building building = new Building(Age.AGE_1, "id", 1, 1, new DummyContextualEffect(), TriggerKey.START_TURN);
    Player player = new Player(Totem.RED, "nick");
    private GameState state;

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
        state = new GameState(players, gc, board, decks, "1");
    }

    /*
     * TEST METHODS
     * Repeated tests are used when one or more random values are used
     */

    /** Tests for
     * - triggerBuildingEffect()
     * - onAddedToPlayer()
     * - addToDeck()
     */

    @DisplayName("The method triggers the effect")
    @Test
    public void triggerBuildingEffectCorrect() {
        gameStateSetUp();
        boolean ret = building.triggerBuildingEffect(player, state);
        assertTrue(ret);
    }

    @DisplayName("If the effect is null it returns false")
    @Test
    public void triggerBuildingEffectNullEffect() {
        Building building = new Building(Age.AGE_1, "id", 1, 1, null, TriggerKey.START_TURN);
        gameStateSetUp();
        boolean ret = building.triggerBuildingEffect(player, state);
        assertFalse(ret);
    }

    @DisplayName("If the player is null it returns false")
    @Test
    public void triggerBuildingEffectNullPlayer() {
        Building building = new Building(Age.AGE_1, "id", 1, 1, new DummyContextualEffect(), TriggerKey.START_TURN);
        gameStateSetUp();
        boolean ret = building.triggerBuildingEffect(null, state);
        assertFalse(ret);
    }

    @DisplayName("If the game state is null it returns false")
    @Test
    public void triggerBuildingEffectNullGameState() {
        Building building = new Building(Age.AGE_1, "id", 1, 1, new DummyContextualEffect(), TriggerKey.START_TURN);
        boolean ret = building.triggerBuildingEffect(player, null);
        assertFalse(ret);
    }

    @DisplayName("Adds one building to a player")
    @Test
    public void onAddedToPlayerOneAddiction() {
        boolean ret = building.onAddedToPlayer(player);

        assertTrue(ret);
    }

    @DisplayName("Add one building to a null player")
    @Test
    public void onAddedToPlayerNullPlayer() {
        boolean ret = building.onAddedToPlayer(null);
        assertFalse(ret);
    }

    @DisplayName("Add one building with null effect to a player")
    @Test
    public void onAddedToPlayerNullEffect() {
        Building building = new Building(Age.AGE_1, "id", 1, 1, null, TriggerKey.START_TURN);
        boolean ret = building.onAddedToPlayer(null);
        assertFalse(ret);
    }
/*
    @DisplayName("Adds one building to a deck")
    @Test
    public void addToDeckCorrect() {
        List<Card> cards = new ArrayList<>();
        List<Building> buildings = new ArrayList<>();
        Decks deck = new Decks(cards, buildings);
        boolean ret = building.addToDeck(deck);

        assertTrue(ret);
        assertEquals(1, deck.getBuildings(building.getAge()).size());
    }
*/
    @DisplayName("Adds one building to a deck with null age")
    @Test
    public void addToDeckNullAge() {
        Building building = new Building(null, "id", 1, 1, new DummyContextualEffect(), TriggerKey.START_TURN);
        List<Card> cards = new ArrayList<>();
        List<Building> buildings = new ArrayList<>();
        Decks deck = new Decks(cards, buildings);
        boolean ret = building.addToDeck(deck);

        assertFalse(ret);
    }
}
