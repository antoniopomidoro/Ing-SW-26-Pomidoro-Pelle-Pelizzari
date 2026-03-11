package it.polimi.ingsw.model;

import java.util.List;

public class Player {
    private int id;
    private String nickname;
    private List<Card> cards;
    private List<Building> buildings;
    private PlayerStats stats;
    private int food;
    private int pp;
    private boolean isChoosing;

    public Player() {
        // Skeleton constructor
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

    public List<Building> getBuildings() {
        return null;
    }

    public PlayerStats getStats() {
        return null;
    }

    public boolean addCard(Card c) {
        return false;
    }

    public boolean addBuilding(Building b) {
        return false;
    }

    public boolean addFood(int amount) {
        return false;
    }

    public boolean payFood(int amount) {
        return false;
    }

    public boolean addPP(int amount) {
        return false;
    }

    // getBuildingDiscount() is now in PlayerStats
    // getStars() is now in PlayerStats
    // getSustainmentDiscount() is now in PlayerStats
    // payPP() is removed from UML
    // choose() is removed from UML
}
