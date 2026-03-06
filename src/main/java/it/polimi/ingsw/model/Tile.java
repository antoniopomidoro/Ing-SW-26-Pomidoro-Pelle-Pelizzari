package it.polimi.ingsw.model;

public class Tile {
    private TileId tileId;
    private boolean isOccupied;
    private Player occupier;
    private String effectId;
    private int minPlayers;

    public Tile() {

    }

    public TileId getId() {
        return tileId ;
    }

    public boolean setId(TileId id) {
        if(id == null){
            return false;
        }
        this.tileId = id;
        return true;
    }

    public boolean isOccupied() {
        return isOccupied;
    }

    public Player getOccupier() {
        return this.occupier;
    }

    public String getEffectId() {
        return effectId;
    }

    public boolean setEffectId(String effectId) {
        if(effectId == null || effectId.trim().isEmpty()){
            return false;
        }
        return true;
    }

    public int getMinPlayers() {
        return this.minPlayers;
    }

    public boolean setMinPlayers(int minPlayers) {
        if(minPlayers <= 1 || minPlayers > 5) {
            return false;
        }
        this.minPlayers = minPlayers;
        return true;
    }

    public boolean occupy(Player p) {
        if(this.isOccupied || p == null){
            return false;
        }
        this.occupier = p;
        this.isOccupied = true;
        return false;
    }

    public boolean deOccupy() {
        if(!this.isOccupied){
            return false;
        }
        this.occupier = null;
        this.isOccupied = false;
        return true;
    }
}
