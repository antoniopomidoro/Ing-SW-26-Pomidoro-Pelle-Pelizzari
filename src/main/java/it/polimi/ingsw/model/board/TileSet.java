package it.polimi.ingsw.model.board;

import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.cards.characters.*;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.effects.contextual.*;
import it.polimi.ingsw.model.effects.events.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a collection of game tiles.
 */
public class TileSet implements Serializable {
    private List<Tile> tiles;

    /** Default constructor for Jackson deserialization. */
    protected TileSet() { this.tiles = new ArrayList<>(); }

    /**
     * Constructs a TileSet with a given list of tiles.
     * @param tiles The initial list of tiles.
     */
    public TileSet(List<Tile> tiles) {
        if (tiles == null) {
            this.tiles = new ArrayList<>();
        }else{
            this.tiles = new ArrayList<>(tiles);
        }
    }

    /**
     * Retrieves a tile by its ID.
     * @param id The ID of the tile to retrieve.
     * @return The tile with the specified ID, or null if not found.
     */
    public Tile getTile(int index) {
        if (index < 0 || index >= tiles.size()) {
            throw new IndexOutOfBoundsException("Index out of bounds: " + index);
        }
        return tiles.get(index);
    }

    /**
     * Gets an unmodifiable view of the list of tiles.
     * @return An unmodifiable list of tiles.
     */
    public List<Tile> getTiles() {
        return Collections.unmodifiableList(this.tiles);
    }

    /** Returns the index of a tile in the set, or -1 if not found. */
    public int indexOf(Tile tile) {
        return tiles.indexOf(tile);
    }
}
