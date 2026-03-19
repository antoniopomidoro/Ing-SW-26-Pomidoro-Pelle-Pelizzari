package it.polimi.ingsw.model.player;

import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.cards.characters.*;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.effects.contextual.*;
import it.polimi.ingsw.model.effects.events.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;


import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class Player {
    private int id;
    private String nickname;
    private List<Card> cards;
    // Dispatcher for buildings organized by their trigger phase
    private Map<GamePhase, List<Building>> buildingDispatcher;
    private PlayerStats stats;
    private int food;
    private int pp;
    private boolean isChoosing;
    private int totemPlacementBonus = 0;

    public Player(int id, String nickname) {
        this.buildingDispatcher = new EnumMap<>(GamePhase.class);
        for (GamePhase phase : GamePhase.values()) {
            this.buildingDispatcher.put(phase, new ArrayList<>());
        }
        // Other initializations
        this.id = id;
        this.nickname = nickname;
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

    public int getId() {
        return id;
    }

    public String getNickname() {
        return nickname;
    }

    public int getFood() {
        return food;
    }

    public int getPP() {
        return pp;
    }

    public List<Card> getCards() {
        return cards;
    }

    /**
     * Returns all buildings owned by the player as a flat list.
     * @return List of all buildings.
     */
    public List<Building> getBuildings() {
        List<Building> allBuildings = new ArrayList<>();
        for (List<Building> phaseBuildings : buildingDispatcher.values()) {
            allBuildings.addAll(phaseBuildings);
        }
        return allBuildings;
    }

    /**
     * Returns the buildings that trigger in a specific phase.
     * @param phase The game phase.
     * @return List of buildings for that phase.
     */
    public List<Building> getBuildingsByPhase(GamePhase phase) {
        if (phase == null) return new ArrayList<>();
        return buildingDispatcher.getOrDefault(phase, new ArrayList<>());
    }

    public PlayerStats getStats() {
        return stats;
    }

    public boolean addCard(Card c) {
        cards.add(c);
        return true;
    }

    public boolean addBuilding(Building b) {
        if (b == null) return false;
        
        GamePhase phase = b.getTriggerPhase();
        if (phase == null) {
            return false; 
        }
        
        List<Building> list = buildingDispatcher.get(phase);
        if (list != null) {
            list.add(b);
            b.onAddedToPlayer(this);
            return true;
        }
        return false;
    }

    public boolean addFood(int amount) {
        this.food += amount;
        return true;
    }

    public boolean payFood(int amount) {
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

    public int getTotemPlacementBonus() {
        return totemPlacementBonus;
    }

    public void setTotemPlacementBonus(int totemPlacementBonus) {
        this.totemPlacementBonus = totemPlacementBonus;
    }
}
