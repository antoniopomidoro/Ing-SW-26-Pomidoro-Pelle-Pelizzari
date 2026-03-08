package it.polimi.ingsw.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TileTest {
    private Tile tile;
    private Player player;
    @BeforeEach
    void setUp(){
        tile = new Tile();
        player = new Player();
    }
    @Test
    void testOccupyEptyTileSuccess(){
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



}