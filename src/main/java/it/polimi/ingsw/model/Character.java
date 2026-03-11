package it.polimi.ingsw.model;

/**
 * Represents a Character card, which extends the basic Card.
 * Characters have a specific type (ID) and a minimum number of players requirement.
 * Instances of this class are intended to be created from JSON data.
 */
public abstract class Character extends Card {
    private Character_Enum id;
    private int minPlayers;

    /**
     * Default constructor for Character.
     */
    public Character() {

    }

    /**
     * Gets the type (ID) of the character.
     * @return The character type.
     */
    public Character_Enum getId() {
        return id;
    }

    /**
     * Sets the type (ID) of the character.
     * @param id The character type to set.
     * @return True if the ID was set successfully, false otherwise.
     */
    public boolean setId(Character_Enum id) {
        if (id == null) {
            return false;
        }
        this.id = id;
        return true;
    }

    /**
     * Gets the minimum number of players required for this character card.
     * @return The minimum number of players.
     */
    public int getMinPlayers() {
        return minPlayers;
    }

    /**
     * Sets the minimum number of players required for this character card.
     * @param minPlayers The minimum number of players.
     * @return True if the minimum number of players was set successfully, false otherwise.
     */
    public boolean setMinPlayers(int minPlayers) {
        if (minPlayers < 1) { // Assuming minPlayers must be positive
            return false;
        }
        this.minPlayers = minPlayers;
        return true;
    }

    /**
     * Method triggered when the character is added to a player.
     * @param p The player adding the character.
     */
    public void onAddedToPlayer(Player p) {
        // Skeleton method
    }
}
