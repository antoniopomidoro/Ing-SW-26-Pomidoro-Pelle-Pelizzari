package it.polimi.ingsw.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a collection of game tiles.
 */
public class TileSet {
    private List<Tile> tiles;

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
    public Tile getTile(TileId id) {
        for (Tile t : this.tiles) {
            if (t.getId() == id) {
                return t;
            }
        }
        return null;
    }

    /**
     * Gets an unmodifiable view of the list of tiles.
     * @return An unmodifiable list of tiles.
     */
    public List<Tile> getTiles() {
        return Collections.unmodifiableList(this.tiles);
    }
}
