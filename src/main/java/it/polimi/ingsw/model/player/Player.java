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

    public boolean getIsChoosing() {
        return isChoosing;
    }

    public Totem getId() {
        return id;
    }

    public String getNickname() {
        return nickname;
    }

    public int getFood() {
        return food;
    }

    public void setFood(int food) {
        this.food = food;
    }

    public int getPP() {
        return pp;
    }

    public List<Card> getCards() {
        return cards;
    }

    protected Totem getID() {
        return id;
    }





    public PlayerStats getStats() {
        return stats;
    }

    public boolean addCard(Card c) {
        cards.add(c);
        c.onAddedToPlayer(this);
        return true;
    }

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

    public boolean addPP(int amount) {
        this.pp += amount;
        return true;
    }

    public boolean payPP(int amount) {
        this.pp -= amount;
        return true;
    }

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
    public boolean canBuy(Building b){
        return food >= calculateRealPrice(b);
    }
    public boolean payBuilding(Building b){
        if(!canBuy(b))return false;
        return payFood(calculateRealPrice(b));
    }
    private int calculateRealPrice(Building b) {
        int realPrice = b.getFoodCost() - stats.getBuildingDiscount();
        return Math.max(0, realPrice);
    }


    public boolean addBuilding(Building b){
        buildings.add(b);
        b.onAddedToPlayer(this);
        return true;
    }

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
            if (b.getTriggerKey() == key) {
                result.add(b);
            }
        }
        return result;
    }
    public int getBuildingsPP(){
        return buildings.stream()
                .mapToInt(Building::getPP)
                .sum();
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean b) {
        isConnected = b;
    }

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
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
