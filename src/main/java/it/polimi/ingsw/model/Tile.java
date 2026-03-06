package it.polimi.ingsw.model;

/**
 * Represents a tile on the game board.
 * Instances of this class are intended to be created from JSON data.
 */
public class Tile {
    private TileId tileId;
    private boolean isOccupied;
    private Player occupier;
    private String effectId;
    private int minPlayers;

    /**
     * Default constructor for Tile.
     * The constructor is empty as instances of this class will be populated using JSON deserialization.
     */
    public Tile() {

    }

    /**
     * Gets the ID of the tile.
     * @return The tile ID.
     */
    public TileId getId() {
        return tileId ;
    }

    /**
     * Sets the ID of the tile. This method is intended to be used by the JSON deserializer.
     * @param id The tile ID to set.
     * @return True if the ID was set successfully, false otherwise.
     */
    public boolean setId(TileId id) {
        if(id == null){
            return false;
        }
        this.tileId = id;
        return true;
    }

    /**
     * Checks if the tile is occupied.
     * @return True if the tile is occupied, false otherwise.
     */
    public boolean isOccupied() {
        return isOccupied;
    }

    /**
     * Gets the player who occupies the tile.
     * @return The occupying player, or null if the tile is not occupied.
     */
    public Player getOccupier() {
        return this.occupier;
    }

    /**
     * Gets the ID of the effect associated with this tile.
     * @return The effect ID.
     */
    public String getEffectId() {
        return effectId;
    }

    /**
     * Sets the ID of the effect for this tile. This method is intended to be used by the JSON deserializer.
     * @param effectId The effect ID to set.
     * @return True if the effect ID was set successfully, false otherwise.
     */
    public boolean setEffectId(String effectId) {
        if(effectId == null || effectId.trim().isEmpty()){
            return false;
        }
        this.effectId = effectId;
        return true;
    }

    /**
     * Gets the minimum number of players required for this tile to be in play.
     * @return The minimum number of players.
     */
    public int getMinPlayers() {
        return this.minPlayers;
    }

    /**
     * Sets the minimum number of players for this tile. This method is intended to be used by the JSON deserializer.
     * @param minPlayers The minimum number of players to set.
     * @return True if the value was set successfully, false otherwise.
     */
    public boolean setMinPlayers(int minPlayers) {
        if(minPlayers <= 1 || minPlayers > 5) {
            return false;
        }
        this.minPlayers = minPlayers;
        return true;
    }

    /**
     * Occupies the tile with a player.
     * @param p The player to occupy the tile.
     * @return True if the tile was occupied successfully, false otherwise.
     */
    public boolean occupy(Player p) {
        if(this.isOccupied || p == null){
            return false;
        }
        this.occupier = p;
        this.isOccupied = true;
        return true;
    }

    /**
     * Frees the tile from occupation.
     * @return True if the tile was de-occupied successfully, false otherwise.
     */
    public boolean deOccupy() {
        if(!this.isOccupied){
            return false;
        }
        this.occupier = null;
        this.isOccupied = false;
        return true;
    }
}
