package it.polimi.ingsw.model;

import java.util.List;

public class Player {
    private int id;
    private String nickname;
    private List<Card> cards;
    private List<Building> buildings;
    private int food;
    private int pp;
    private boolean isChoosing;

    public boolean getIsChoosing() {
        return false;
    }

    public int getId() {
        return 0;
    }

    public int getFood() {
        return 0;
    }

    public List<Card> getCards() {
        return null;
    }

    public List<Building> getBuildings() {
        return null;
    }

    public int getBuildingDiscount() {
        return 0;
    }

    public int getStars() {
        return 0;
    }

    public int getSustainmentDiscount() {
        return 0;
    }

    public int getPP() {
        return 0;
    }

    public boolean addCard(Card c) {
        return false;
    }

    public boolean addBuilding(Building b) {
        return false;
    }

    public boolean setPP(int val) {
        return false;
    }

    public boolean setFood(int val) {
        return false;
    }

    public boolean choose() {
        return false;
    }
}