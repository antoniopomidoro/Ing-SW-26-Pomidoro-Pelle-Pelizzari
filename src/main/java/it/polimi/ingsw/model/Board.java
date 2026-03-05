package it.polimi.ingsw.model;

import java.util.List;

public class Board {
    private TileSet tiles;
    private List<Card> topCards;
    private List<Card> bottomCards;
    private List<Building> topBuildings;
    private List<Building> bottomBuildings;

    public Card pickTopCard(int index) {
        return null;
    }

    public Card pickBottomCard(int index) {
        return null;
    }

    public Building pickTopBuilding(int index) {
        return null;
    }

    public Building pickBottomBuilding(int index) {
        return null;
    }

    public boolean addTopCard(Card c) {
        return false;
    }

    public boolean addBottomCard(Card c) {
        return false;
    }

    public boolean addTopBuilding(Building b) {
        return false;
    }

    public boolean discardBottomCards() {
        return false;
    }

    public boolean discardBottomBuildings() {
        return false;
    }

    public boolean topToBottomCards() {
        return false;
    }

    public boolean topToBottomBuildings() {
        return false;
    }

    public boolean refillTopCards() {
        return false;
    }

    public boolean refillTopBuildings() {
        return false;
    }
}