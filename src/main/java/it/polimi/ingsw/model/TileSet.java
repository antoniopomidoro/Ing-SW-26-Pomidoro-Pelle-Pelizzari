package it.polimi.ingsw.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TileSet {
    private List<Tile> tiles;

    public TileSet(ArrayList<Tile> tiles) {
        if (tiles == null) {
            this.tiles = new ArrayList<>();
        }else{
            this.tiles = new ArrayList<>(tiles);
        }
    }


    public Tile getTile(TileId id) {
        for (Tile t : this.tiles) {
            if (t.getId() == id) {
                return t;
            }
        }
        return null;
    }

    public List<Tile> getTiles() {
        return Collections.unmodifiableList(this.tiles);
    }
}
