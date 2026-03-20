package it.polimi.ingsw.model.player;

import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.cards.Character;
import it.polimi.ingsw.model.cards.characters.*;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.effects.contextual.*;
import it.polimi.ingsw.model.effects.events.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;


import java.util.*;


public class Player {
    private int id;
    private String nickname;
    private List<Card> cards;
    private List<Building> buidlings;
    private PlayerStats stats;
    private int food;
    private int pp;
    private boolean isChoosing;
    private int totemPlacementBonus = 0;

    public Player(int id, String nickname) {

        // Other initializations
        this.id = id;
        this.nickname = nickname;
        buidlings = new ArrayList<>();
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






    public PlayerStats getStats() {
        return stats;
    }

    public boolean addCard(Card c) {
        cards.add(c);
        return true;
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

    public boolean addBuilding( Building b){
        buidlings.add(b);
        return true;
    }

    public List<Building> getBuidlings(){
        List<Building> sup = new ArrayList<>();
        sup.addAll(buidlings);
        return sup;
    }




}
