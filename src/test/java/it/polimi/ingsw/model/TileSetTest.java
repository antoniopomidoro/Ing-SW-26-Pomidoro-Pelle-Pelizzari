package it.polimi.ingsw.model;

import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.cards.characters.*;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.effects.contextual.*;
import it.polimi.ingsw.model.effects.events.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class TileSetTest {
    @Test
    void testConstructorDefensiveCopy(){
        List<Tile> externalList = new ArrayList<>();
        externalList.add(new Tile());
        TileSet tileSet = new TileSet(externalList);
        assertEquals(1, tileSet.getTiles().size(), "The tileset must contain the tile");
        externalList.clear();
        assertEquals(1, tileSet.getTiles().size(), "If the list is somehow cleared, the tileset has to still contain the tile");
    }
    @Test
    void testConstructorWithNullList() {
        TileSet tileSet = new TileSet(null);
        assertNotNull(tileSet.getTiles(), "The tileset must not be null");
        assertTrue(tileSet.getTiles().isEmpty(), "The tileset must be empty");
    }

}
