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

    public Player() {
        this.buildingDispatcher = new EnumMap<>(GamePhase.class);
        for (GamePhase phase : GamePhase.values()) {
            this.buildingDispatcher.put(phase, new ArrayList<>());
        }
        // Other initializations would go here in a real implementation
    }

    public boolean getIsChoosing() {
        return false;
    }

    public int getId() {
        return 0;
    }

    public String getNickname() {
        return nickname;
    }

    public int getFood() {
        return 0;
    }

    public int getPP() {
        return pp;
    }

    public List<Card> getCards() {
        return null;
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
        return null;
    }

    public boolean addCard(Card c) {
        return false;
    }

    public boolean addBuilding(Building b) {
        if (b == null) return false;
        
        GamePhase phase = b.getTriggerPhase();
        if (phase == null) {
            // Handle buildings without a specific trigger phase (one shot ones)
            // Might be useful to add a NONE phase to the enum to store the ones without a phase
            return false; 
        }
        
        List<Building> list = buildingDispatcher.get(phase);
        if (list != null) {
            list.add(b);
            return true;
        }
        return false;
    }

    public boolean addFood(int amount) {
        this.food += amount;
        return true;
    }

    public boolean payFood(int amount) {
        return false;
    }

    public boolean addPP(int amount) {
        return false;
    }

    public boolean payPP(int amount) {
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
}
