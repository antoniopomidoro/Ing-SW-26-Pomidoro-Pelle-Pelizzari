package it.polimi.ingsw.model.board;

import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.cards.characters.*;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.effects.contextual.*;
import it.polimi.ingsw.model.effects.events.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;


/**
 * Represents a tile on the game board.
 * Instances of this class are intended to be created from JSON data.
 */
public class Tile {
    private TileId tileId;
    private boolean isOccupied;
    private Player occupier;
    private int upperPicks;
    private int bottomPicks;
    private int foodBonus;
    private int minPlayers;

    /**
     * Default constructor for Tile.
     * The constructor is empty as instances of this class will be populated using JSON deserialization.
     */
    public Tile() {

    }

    public Tile(Player p, int upperPicks, int bottomPicks){
        this.occupier = p;
        this.upperPicks = upperPicks;
        this.bottomPicks = bottomPicks;
        this.isOccupied = true;

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

    public int getUpperPicks() {
        return upperPicks;
    }

    public int getBottomPicks() {
        return bottomPicks;
    }

    public int getFoodBonus() {
        return foodBonus;
    }

    // --- Setters for save/load restoration ---
    public void setFoodBonus(int foodBonus) { this.foodBonus = foodBonus; }
    public void setUpperPicks(int upperPicks) { this.upperPicks = upperPicks; }
    public void setBottomPicks(int bottomPicks) { this.bottomPicks = bottomPicks; }
    public void setOccupier(Player p) {
        this.occupier = p;
        this.isOccupied = (p != null);
    }
}
