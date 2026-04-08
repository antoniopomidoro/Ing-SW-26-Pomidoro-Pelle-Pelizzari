package it.polimi.ingsw.model.board;

import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Totem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TileTest {
    Tile tile = new Tile();
    Player player = new Player(Totem.RED_TOTEM, "aldo");

    /*
     * TEST METHODS
     * Repeated tests are used when one or more random values are used
     */

    /** Tests for
     * - occupy()
     * - deOccupy()
     */

    @DisplayName("The method works correctly")
    @Test
    public void occupyCorrect() {
        boolean ret = tile.occupy(player);
        assertTrue(ret);
    }

    @DisplayName("A player can't occupy an already occupied tile")
    @Test
    public void occupyAlreadyOccupied() {
        tile.occupy(player);
        Player player2 = new Player(Totem.YELLOW_TOTEM, "giovanni");
        boolean ret = tile.occupy(player2);
        assertFalse(ret);
    }

    @DisplayName("A null player can't occupy any tile")
    @Test
    public void occupyNullPlayer() {
        boolean ret = tile.occupy(null);
        assertFalse(ret);
    }

    @DisplayName("The method works correctly")
    @Test
    public void deOccupyCorrect() {
        tile.occupy(player);
        boolean ret = tile.deOccupy();
        assertTrue(ret);
    }

    @DisplayName("An already free tile can't be deoccupied")
    @Test
    public void deOccupyAlreadyFree() {
        boolean ret = tile.deOccupy();
        assertFalse(ret);
    }
}
