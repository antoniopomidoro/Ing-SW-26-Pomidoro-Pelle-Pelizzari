package it.polimi.ingsw.model.player;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.cards.Character;
import it.polimi.ingsw.model.cards.characters.*;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.effects.contextual.*;
import it.polimi.ingsw.model.effects.events.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;


import java.io.Serializable;
import java.util.*;


/**
 * Represents a participant in the game: identified by a {@link Totem} colour and
 * a nickname, owning food, prestige points, a hand of cards and a set of
 * buildings, and tracking aggregate {@link PlayerStats}. Also carries the
 * connection flag used by the networking layer to skip disconnected players.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Player implements Serializable {
    private Totem id;
    private String nickname;
    private List<Card> cards;
    private List<Building> buildings;
    private PlayerStats stats;
    private int food;
    private int pp;
    private boolean isChoosing;
    private boolean isConnected = true;

    /** Default constructor for Jackson deserialization. */
    protected Player() {
        this.buildings = new ArrayList<>();
        this.cards = new ArrayList<>();
        this.stats = new PlayerStats();
    }

    /**
     * Constructs a player with the given totem and nickname, starting with no
     * food, no prestige points and empty collections.
     *
     * @param id       the totem identifying the player
     * @param nickname the player's nickname
     */
    public Player(Totem id, String nickname) {

        // Other initializations
        this.id = id;
        this.nickname = nickname;
        buildings = new ArrayList<>();
        cards = new ArrayList<>();
        stats = new PlayerStats();
        // The food gets added later
        food = 0;
        pp = 0;
        isChoosing = false;
    }

    /**
     * Indicates whether the player is currently in the middle of choosing.
     *
     * @return true if the player is choosing
     */
    public boolean getIsChoosing() {
        return isChoosing;
    }

    /**
     * Gets the totem identifying this player.
     *
     * @return the player's totem
     */
    public Totem getId() {
        return id;
    }

    /**
     * Gets the player's nickname.
     *
     * @return the nickname
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * Gets the player's current food supply.
     *
     * @return the amount of food
     */
    public int getFood() {
        return food;
    }

    /**
     * Sets the player's food supply (used by save/load restoration).
     *
     * @param food the food amount to set
     */
    public void setFood(int food) {
        this.food = food;
    }

    /**
     * Gets the player's current prestige points.
     *
     * @return the prestige points
     */
    public int getPP() {
        return pp;
    }

    /**
     * Gets the player's hand of cards.
     *
     * @return the list of cards held by the player
     */
    public List<Card> getCards() {
        return cards;
    }

    /**
     * Gets the player's totem (test/internal accessor).
     *
     * @return the player's totem
     */
    protected Totem getID() {
        return id;
    }





    /**
     * Gets the aggregate statistics of this player.
     *
     * @return the player's stats
     */
    public PlayerStats getStats() {
        return stats;
    }

    /**
     * Adds a card to the player's hand and runs its acquisition hook.
     *
     * @param c the card to add
     * @return true
     */
    public boolean addCard(Card c) {
        cards.add(c);
        c.onAddedToPlayer(this);
        return true;
    }

    /**
     * Adds food to the player's supply.
     *
     * @param amount the amount of food to add
     * @return true
     */
    public boolean addFood(int amount) {
        this.food += amount;
        return true;
    }

    /**
     * Deducts food from this player's supply.
     *
     * <p><strong>Precondition:</strong> the caller must verify affordability before invoking
     * this method (e.g. {@code player.getFood() >= amount}). This is an internal model
     * operation and is never invoked without prior validation.
     *
     * @param amount the quantity of food to remove; must be &ge; 0 and &le; current food balance
     * @return {@code true} after the deduction succeeds
     * @throws IllegalArgumentException if {@code amount < 0} or {@code amount > getFood()}
     */
    public boolean payFood(int amount) {
        if (amount < 0 || amount > food) {
            throw new IllegalArgumentException("Invalid food amount to pay");
        }
        this.food -= amount;
        return true;
    }

    /**
     * Adds prestige points to the player.
     *
     * @param amount the amount of prestige points to add
     * @return true
     */
    public boolean addPP(int amount) {
        this.pp += amount;
        return true;
    }

    /**
     * Removes prestige points from the player (the total may become negative).
     *
     * @param amount the amount of prestige points to remove
     * @return true
     */
    public boolean payPP(int amount) {
        this.pp -= amount;
        return true;
    }

    /**
     * Pays food up to the player's available supply; any shortfall is converted
     * into a prestige-point penalty (missing food times the penalty factor).
     *
     * @param requiredFood the food required
     * @param penalty      the per-unit prestige penalty for missing food
     * @return true
     */
    public boolean payFoodWithPenalty(int requiredFood, int penalty) {
        int missingFood = Math.max(0, requiredFood - food);
        int foodToPay = Math.min(requiredFood, food);
        payFood(foodToPay);
        if (missingFood > 0) {
            int ppPenalty = missingFood * penalty;
            payPP(ppPenalty);
        }
        return true;
    }

    /**
     * Checks whether the player can afford the given building after discounts.
     *
     * @param b the building to evaluate
     * @return true if the player has enough food
     */
    public boolean canBuy(Building b){
        return food >= calculateRealPrice(b);
    }

    /**
     * Pays for the given building at its discounted price, if affordable.
     *
     * @param b the building to pay for
     * @return true if paid, false if the player cannot afford it
     */
    public boolean payBuilding(Building b){
        if(!canBuy(b))return false;
        return payFood(calculateRealPrice(b));
    }
    private int calculateRealPrice(Building b) {
        int realPrice = b.getFoodCost() - stats.getBuildingDiscount();
        return Math.max(0, realPrice);
    }


    /**
     * Adds a building to the player's collection and runs its acquisition hook.
     *
     * @param b the building to add
     * @return true
     */
    public boolean addBuilding(Building b){
        buildings.add(b);
        b.onAddedToPlayer(this);
        return true;
    }

    /**
     * Returns a defensive copy of the player's buildings.
     *
     * @return a copy of the buildings list
     */
    public List<Building> getBuildings(){
        List<Building> sup = new ArrayList<>();
        sup.addAll(buildings);
        return sup;
    }

    /**
     * Returns all buildings owned by this player that match the given trigger key.
     * @param key The trigger key to filter by.
     * @return A list of buildings with the matching trigger key.
     */
    public List<Building> getBuildingsByTrigger(TriggerKey key) {
        List<Building> result = new ArrayList<>();
        for (Building b : buildings) {
            if (b.getTriggerKey().filter(k -> k == key).isPresent()) {
                result.add(b);
            }
        }
        return result;
    }
    /**
     * Sums the prestige points granted by all of the player's buildings.
     *
     * @return the total building prestige points
     */
    public int getBuildingsPP(){
        return buildings.stream()
                .mapToInt(Building::getPP)
                .sum();
    }

    /**
     * Indicates whether the player is currently connected.
     *
     * @return true if connected
     */
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * Sets the player's connection state.
     *
     * @param b true to mark connected, false to mark disconnected
     */
    public void setConnected(boolean b) {
        isConnected = b;
    }

    /**
     * Two players are considered equal when they share the same totem.
     *
     * @param obj the object to compare with
     * @return true if {@code obj} is a player with the same totem
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        Player otherPlayer = (Player) obj;
        return this.id == otherPlayer.id;
    }
    /**
     * Computes the hash code consistently with {@link #equals(Object)}, based on
     * the player's totem.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
