package it.polimi.ingsw.model.board;

import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.cards.characters.*;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.effects.contextual.*;
import it.polimi.ingsw.model.effects.events.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;


import java.util.ArrayList;
import java.util.Collections;

public class Board {
    private TileSet tiles;
    private ArrayList<Card> topCards;
    private ArrayList<Card> bottomCards;
    private ArrayList<Building> topBuildings;
    private ArrayList<Building> bottomBuildings;

    public Board() {
        // Skeleton constructor
    }

    public Card pickTopCard(int index) {
        return topCards.remove( index);
    }

    public Card pickBottomCard(int index) {
        return bottomCards.remove( index);
    }

    public Building pickTopBuilding(int index) {
        return topBuildings.remove( index);
    }

    public Building pickBottomBuilding(int index) {
        return bottomBuildings.remove(index);
    }

    public boolean addTopCard(Card c) {
        if(c != null) {
            topCards.add(c);
            return true;
        }
        return false;
    }

    public boolean addBottomCard(Card c) {
        if(c != null) {
            bottomCards.add(c);
            return true;
        }
        return false;
    }

    public boolean addTopBuilding(ArrayList<Building> b) {
        if(b != null) {
            topBuildings.addAll(b);
            return true;
        }
        return false;
    }

    public boolean topToBottomCards() {
        bottomCards.clear();
        bottomCards.addAll(topCards);
        topCards.clear();
        return true;
    }

    public boolean topToBottomBuildings() {
        bottomBuildings.clear();
        bottomBuildings.addAll(topBuildings);
        topBuildings.clear();
        return true;
    }
}
