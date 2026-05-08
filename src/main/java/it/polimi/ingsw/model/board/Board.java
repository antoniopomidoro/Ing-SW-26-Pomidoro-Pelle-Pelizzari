package it.polimi.ingsw.model.board;

import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.cards.characters.*;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.effects.contextual.*;
import it.polimi.ingsw.model.effects.events.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Board implements Serializable {
    private OrderTile orderTile;
    private TileSet tiles;
    private List<Card> topCards;
    private List<Card> bottomCards;
    private List<Building> topBuildings;
    private List<Building> bottomBuildings;

    /**
     * Default constructor for Jackson deserialization.
     */
    protected Board() {
        this.topCards = new ArrayList<>();
        this.bottomCards = new ArrayList<>();
        this.topBuildings = new ArrayList<>();
        this.bottomBuildings = new ArrayList<>();
    }

    public Board(OrderTile orderTile, TileSet tiles) {
        this.topCards = new ArrayList<>();
        this.bottomCards = new ArrayList<>();
        this.topBuildings = new ArrayList<>();
        this.bottomBuildings = new ArrayList<>();
        this.orderTile = orderTile;
        this.tiles = tiles;
    }

    public Card pickTopCard(int index) {
        return topCards.remove(index);
    }

    public Card pickBottomCard(int index) {
        return bottomCards.remove(index);
    }

    public Building pickTopBuilding(int index) {
        return topBuildings.remove(index);
    }

    public Building pickBottomBuilding(int index) {
        return bottomBuildings.remove(index);
    }



    public boolean cardBottomToTop(int index){
        if(index >=0 && index < bottomCards.size()){
            Card c = bottomCards.remove(index);
            topCards.add(c);
            return true;
        }return false;
    }

    public boolean addTopCard(Card c) {
        if(c != null) {
            topCards.add(c);
            return true;
        }
        return false;
    }

    public boolean addBottomCard(Card c) {
        if(c == null) {
            return false;
        }
        if(!c.isBuyable()) addTopCard(c);
        else bottomCards.add(c);
        return true;
    }

    public boolean addTopBuildings(List<Building> b) {
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
    /**
     * Returns the set of game tiles.
     * @return The TileSet.
     */
    public TileSet getTiles() {
        return this.tiles;
    }

    /**
     * Returns the order tile.
     * @return The OrderTile.
     */
    public OrderTile getOrderTile() {
        return this.orderTile;
    }

    public boolean discardBottomCards(GameState state){
        if (state == null) return false;
        bottomCards.stream()
                .sorted(Comparator.comparingInt(Card::getResolutionPriority))
                .forEach(c -> c.onDiscard(state));
        bottomCards.clear();
        return true;
    }
    public boolean discardBoard(GameState state){
        if (state == null) return false;
        List<Card> allCards = new ArrayList<>();
        allCards.addAll(topCards);
        allCards.addAll(bottomCards);
        allCards.stream()
                .sorted(Comparator.comparingInt(Card::getResolutionPriority))
                .forEach(c -> c.onDiscard(state));
        topCards.clear();
        bottomCards.clear();
        return true;
    }
    //getter for top and bottom cards and buildings, returns a copy of the list to avoid external modification
    public List<Card> getTopCards(){
        List<Card> sup = new ArrayList<>();
        sup.addAll(topCards);
        return sup;

    }

    public List<Card> getBottomCards(){
        List<Card> sup = new ArrayList<>();
        sup.addAll(bottomCards);
        return sup;
    }

    public List<Building> getBottomBuildings(){
        List<Building> sup = new ArrayList<>();
        sup.addAll(bottomBuildings);
        return sup;
    }

    public List<Building> getTopBuildings(){
        List<Building> sup = new ArrayList<>();
        sup.addAll(topBuildings);
        return sup;
    }
    public Card seeTopCard(int index){
        if(index >=0 && index < topCards.size()){
            return topCards.get(index);
        }
        return null;

    }

    public Card seeBottomCard(int index){
        if(index >=0 && index < bottomCards.size()){
            return bottomCards.get(index);
        }
        return null;
    }


    public Building seeTopBuilding(int index){
        if(index >=0 && index < topBuildings.size()){
            return topBuildings.get(index);
        }
        return null;
    }

    public Building seeBottomBuilding(int index){
        if(index >=0 && index < bottomBuildings.size()){
            return bottomBuildings.get(index);
        }
        return null;

    }
    public boolean discardBottomBuildings(){
        bottomBuildings.clear();
        return true;
    }

    // --- Setters for save/load restoration ---
    public void setTopCards(List<Card> cards) { this.topCards = new ArrayList<>(cards); }
    public void setBottomCards(List<Card> cards) { this.bottomCards = new ArrayList<>(cards); }
    public void setTopBuildings(List<Building> b) { this.topBuildings = new ArrayList<>(b); }
    public void setBottomBuildings(List<Building> b) { this.bottomBuildings = new ArrayList<>(b); }

}
