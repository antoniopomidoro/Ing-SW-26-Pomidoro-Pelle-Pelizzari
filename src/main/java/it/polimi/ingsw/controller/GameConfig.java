package it.polimi.ingsw.controller;

import it.polimi.ingsw.model.player.Player;

import java.util.List;

/**
 * Holds game configuration values loaded from {@code config.json}.
 * <p>
 * Fields are populated via Jackson field-access deserialization.
 * <ul>
 *   <li>{@code buildingPerPlayer} – matrix [playerCount-2][era] with the number of buildings to draw</li>
 *   <li>{@code startingFood} – list of starting food per player position (1st through 5th)</li>
 * </ul>
 */
public class GameConfig {

    private int gameMaxPlayers;
    private int gameMinPlayers;
    private int[][] buildingPerPlayer;
    private List<Integer> startingFood;
    private int bottomExtraCards;
    private int topExtraCards;
    private int maxTurns;



    /**
     * Default constructor for Jackson deserialization.
     */
    public GameConfig() {
    }

    /**
     * Returns the number of buildings to draw for the given player count and age.
     *
     * @param playerCount number of players (2–5)
     * @param age         age index (0 = Era I, 1 = Era II, 2 = Era III)
     * @return number of buildings to draw
     */
    public int getBuildingsCount(int playerCount, int age) {
        return buildingPerPlayer[playerCount - gameMinPlayers][age];
    }

    /**
     * Returns the full building-per-player matrix.
     * Row index = playerCount − minPlayers; column index = age (0–2).
     *
     * @return the building matrix
     */
    public int[][] getBuildingPerPlayer() {
        return buildingPerPlayer;
    }

    /**
     * Returns the list of starting food values per player position.
     *
     * @return starting food list
     */
    public List<Integer> getStartingFood() {
        return startingFood;
    }

    public int getGameMaxPlayers() {
        return gameMaxPlayers;
    }

    public int getGameMinPlayers() {
        return gameMinPlayers;
    }

    public int getBottomCardsQuantity(List<Player> p) {
        return bottomExtraCards + p.size();
    }

    public int getTopCardsQuantity(List<Player> p) {
        return topExtraCards + p.size();
    }

    public int getMaxTurns() {
        return maxTurns;
    }
}
