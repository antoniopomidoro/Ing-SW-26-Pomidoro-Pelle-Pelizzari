package it.polimi.ingsw.model;

import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.cards.characters.*;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.effects.contextual.*;
import it.polimi.ingsw.model.effects.events.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/*class TileTest {
    private Tile tile;
    private Player player;
    @BeforeEach
    void setUp(){
        tile = new Tile();
        player = new Player();
    }
    @Test
    void testOccupyEmptyTileSuccess(){
        boolean result = tile.occupy(player);
        assertTrue(result, "Occupying an empty tile should return true");
        assertTrue(tile.isOccupied(), "Tile should be occupied after occupation");
        assertEquals(player, tile.getOccupier(), "Occupier should be set correctly");
    }
    @Test
    void testOccupyAlreadyOccupiedTileFails(){
        Player intruder = new Player();
        tile.occupy(player);
        boolean result = tile.occupy(intruder);
        assertFalse(result, "Occupying an already occupied tile should return false");
        assertEquals(player, tile.getOccupier(), "Occupier should not be changed");
    }
    @Test
    void testOccupyWithNullPlayerFails(){
        boolean result = tile.occupy(null);
        assertFalse(result, "Occupying with null player should return false");
        assertFalse(tile.isOccupied(), "Tile should not be occupied");
        assertNull(tile.getOccupier(), "Occupier should be null");
    }

    // A player can occupy a maximum of 1 tile at the same time
    @Test
    void testOccupyNotMoreThanOneTile() {
        Tile tile2 = new Tile();
        tile.occupy(player);
        assertFalse(tile2.occupy(player), "The attempt to occupy another tile by a player should not be possible.");
    }

    @Test
    void testDeOccupyEmptyTile() {
        tile.occupy(player);
        tile.deOccupy();
        assertFalse(tile.deOccupy(), "Deoccupying an already empty tile should return false.");
        assertNull(tile.getOccupier(), "The occupier of an already deoccupied tile should remain null.");
    }
}*/
