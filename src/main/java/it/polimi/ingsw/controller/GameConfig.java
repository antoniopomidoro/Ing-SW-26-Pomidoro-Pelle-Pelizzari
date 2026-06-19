package it.polimi.ingsw.controller;

import it.polimi.ingsw.model.game.Age;
import it.polimi.ingsw.model.player.Player;

import java.io.Serializable;
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
public class GameConfig implements Serializable {

    private int gameMaxPlayers;
    private int gameMinPlayers;
    private int[][] buildingPerPlayer;
    private List<Integer> startingFood;
    private int bottomExtraCards;
    private int topExtraCards;
    private int maxTurns;
    private int artistGain;



    /**
     * Default constructor for Jackson deserialization.
     */
    public GameConfig() {
    }

    /**
     * Protected constructor for testing purposes.
     * @param gameMaxPlayers the maximum number of players allowed in the game
     * @param gameMinPlayers the minimum number of players allowed in the game
     * @param buildingPerPlayer the number of buildings to draw for each player and age
     * @param startingFood the amount of food each player starts with
     * @param bottomExtraCards the number of extra cards to draw from the bottom of the deck
     * @param topExtraCards the number of extra cards to draw from the top of the deck
     * @param maxTurns the maximum number of turns in the game
     * @param artistGain the number of points awarded for completing an artist's work
     */
    protected GameConfig(int gameMaxPlayers, int gameMinPlayers, int[][] buildingPerPlayer, List<Integer> startingFood, int bottomExtraCards, int topExtraCards, int maxTurns, int artistGain) {
        this.gameMaxPlayers = gameMaxPlayers;
        this.gameMinPlayers = gameMinPlayers;
        this.buildingPerPlayer = buildingPerPlayer;
        this.startingFood = startingFood;
        this.bottomExtraCards = bottomExtraCards;
        this.topExtraCards = topExtraCards;
        this.maxTurns = maxTurns;
        this.artistGain = artistGain;
    }

    /**
     * Returns the number of buildings to draw for the given player count and age.
     *
     * @param playerCount number of players (2–5)
     * @param age         age (must satisfy {@link Age#isAge()})
     * @return number of buildings to draw
     * @throws IllegalArgumentException if the age has no associated buildings
     */
    public int getBuildingsCount(int playerCount, Age age) {
        if (!age.isAge()) {
            throw new IllegalArgumentException("Age " + age + " has no buildings");
        }
        return buildingPerPlayer[playerCount - gameMinPlayers][age.getValue() - 1];
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

    /**
     * Returns the maximum number of players allowed in a game.
     *
     * @return the maximum player count
     */
    public int getGameMaxPlayers() {
        return gameMaxPlayers;
    }

    /**
     * Returns the minimum number of players required to start a game.
     *
     * @return the minimum player count
     */
    public int getGameMinPlayers() {
        return gameMinPlayers;
    }

    /**
     * Returns how many bottom-row cards to deal: the configured extra cards plus
     * one per player.
     *
     * @param p the players in the match
     * @return the bottom-row card quantity
     */
    public int getBottomCardsQuantity(List<Player> p) {
        return bottomExtraCards + p.size();
    }

    /**
     * Returns how many top-row cards to deal: the configured extra cards plus
     * one per player.
     *
     * @param p the players in the match
     * @return the top-row card quantity
     */
    public int getTopCardsQuantity(List<Player> p) {
        return topExtraCards + p.size();
    }

    /**
     * Returns the maximum number of turns in a game.
     *
     * @return the turn limit
     */
    public int getMaxTurns() {
        return maxTurns;
    }

    /**
     * Returns the prestige points awarded per artist pair at end game.
     *
     * @return the artist gain
     */
    public int getArtistGain() {
        return artistGain;
    }

    /**
     * Returns the configured number of extra bottom-row cards.
     *
     * @return the bottom extra cards
     */
    public int getBottomExtraCards() { return bottomExtraCards; }

    /**
     * Returns the configured number of extra top-row cards.
     *
     * @return the top extra cards
     */
    public int getTopExtraCards() { return topExtraCards; }
}
