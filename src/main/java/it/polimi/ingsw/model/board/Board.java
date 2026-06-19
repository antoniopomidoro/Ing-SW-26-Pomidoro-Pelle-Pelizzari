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

/**
 * Represents the shared game board: the turn-order tile, the set of player
 * tiles and the four card/building rows (top and bottom for cards and
 * buildings). Provides the operations to pick, add, move and discard those
 * elements during a game. Getters return defensive copies of the lists.
 */
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

    /**
     * Constructs a board with the given turn-order tile and tile set, starting
     * with empty card and building rows.
     *
     * @param orderTile the turn-order tile
     * @param tiles     the set of player tiles
     */
    public Board(OrderTile orderTile, TileSet tiles) {
        this.topCards = new ArrayList<>();
        this.bottomCards = new ArrayList<>();
        this.topBuildings = new ArrayList<>();
        this.bottomBuildings = new ArrayList<>();
        this.orderTile = orderTile;
        this.tiles = tiles;
    }

    /**
     * Removes and returns the card at the given index from the top row.
     *
     * @param index the position of the card to pick
     * @return the removed card
     */
    public Card pickTopCard(int index) {
        return topCards.remove(index);
    }

    /**
     * Removes and returns the card at the given index from the bottom row.
     *
     * @param index the position of the card to pick
     * @return the removed card
     */
    public Card pickBottomCard(int index) {
        return bottomCards.remove(index);
    }

    /**
     * Removes and returns the building at the given index from the top row.
     *
     * @param index the position of the building to pick
     * @return the removed building
     */
    public Building pickTopBuilding(int index) {
        return topBuildings.remove(index);
    }

    /**
     * Removes and returns the building at the given index from the bottom row.
     *
     * @param index the position of the building to pick
     * @return the removed building
     */
    public Building pickBottomBuilding(int index) {
        return bottomBuildings.remove(index);
    }



    /**
     * Moves the card at the given index from the bottom row to the top row.
     *
     * @param index the position of the card in the bottom row
     * @return true if the card was moved, false if the index is out of range
     */
    public boolean cardBottomToTop(int index){
        if(index >=0 && index < bottomCards.size()){
            Card c = bottomCards.remove(index);
            topCards.add(c);
            return true;
        }return false;
    }

    /**
     * Adds a card to the top row.
     *
     * @param c the card to add
     * @return true if added, false if the card is null
     */
    public boolean addTopCard(Card c) {
        if(c != null) {
            topCards.add(c);
            return true;
        }
        return false;
    }

    /**
     * Adds a card to the bottom row. Non-buyable cards (e.g. events) are routed
     * to the top row instead.
     *
     * @param c the card to add
     * @return true if added, false if the card is null
     */
    public boolean addBottomCard(Card c) {
        if(c == null) {
            return false;
        }
        if(!c.isBuyable()) addTopCard(c);
        else bottomCards.add(c);
        return true;
    }

    /**
     * Adds all the given buildings to the top row.
     *
     * @param b the buildings to add
     * @return true if added, false if the list is null
     */
    public boolean addTopBuildings(List<Building> b) {
        if(b != null) {
            topBuildings.addAll(b);
            return true;
        }
        return false;
    }

    /**
     * Moves every card from the top row to the bottom row, replacing the
     * previous bottom content.
     *
     * @return true
     */
    public boolean topToBottomCards() {
        bottomCards.clear();
        bottomCards.addAll(topCards);
        topCards.clear();
        return true;
    }

    /**
     * Moves every building from the top row to the bottom row, replacing the
     * previous bottom content.
     *
     * @return true
     */
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

    /**
     * Discards all bottom-row cards, triggering each card's discard logic in
     * resolution-priority order (lower priority first), then clears the row.
     *
     * @param state the current game state
     * @return true if discarded, false if the state is null
     */
    public boolean discardBottomCards(GameState state){
        if (state == null) return false;
        bottomCards.stream()
                .sorted(Comparator.comparingInt(Card::getResolutionPriority))
                .forEach(c -> c.onDiscard(state));
        bottomCards.clear();
        return true;
    }

    /**
     * Discards every card on the board (top and bottom rows), triggering each
     * card's discard logic in resolution-priority order, then clears both rows.
     *
     * @param state the current game state
     * @return true if discarded, false if the state is null
     */
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

    /**
     * Returns a defensive copy of the top-row cards.
     *
     * @return a copy of the top cards list
     */
    public List<Card> getTopCards(){
        List<Card> sup = new ArrayList<>();
        sup.addAll(topCards);
        return sup;

    }

    /**
     * Returns a defensive copy of the bottom-row cards.
     *
     * @return a copy of the bottom cards list
     */
    public List<Card> getBottomCards(){
        List<Card> sup = new ArrayList<>();
        sup.addAll(bottomCards);
        return sup;
    }

    /**
     * Returns a defensive copy of the bottom-row buildings.
     *
     * @return a copy of the bottom buildings list
     */
    public List<Building> getBottomBuildings(){
        List<Building> sup = new ArrayList<>();
        sup.addAll(bottomBuildings);
        return sup;
    }

    /**
     * Returns a defensive copy of the top-row buildings.
     *
     * @return a copy of the top buildings list
     */
    public List<Building> getTopBuildings(){
        List<Building> sup = new ArrayList<>();
        sup.addAll(topBuildings);
        return sup;
    }

    /**
     * Returns, without removing, the top-row card at the given index.
     *
     * @param index the position to look at
     * @return the card at that index, or null if the index is out of range
     */
    public Card seeTopCard(int index){
        if(index >=0 && index < topCards.size()){
            return topCards.get(index);
        }
        return null;

    }

    /**
     * Returns, without removing, the bottom-row card at the given index.
     *
     * @param index the position to look at
     * @return the card at that index, or null if the index is out of range
     */
    public Card seeBottomCard(int index){
        if(index >=0 && index < bottomCards.size()){
            return bottomCards.get(index);
        }
        return null;
    }


    /**
     * Returns, without removing, the top-row building at the given index.
     *
     * @param index the position to look at
     * @return the building at that index, or null if the index is out of range
     */
    public Building seeTopBuilding(int index){
        if(index >=0 && index < topBuildings.size()){
            return topBuildings.get(index);
        }
        return null;
    }

    /**
     * Returns, without removing, the bottom-row building at the given index.
     *
     * @param index the position to look at
     * @return the building at that index, or null if the index is out of range
     */
    public Building seeBottomBuilding(int index){
        if(index >=0 && index < bottomBuildings.size()){
            return bottomBuildings.get(index);
        }
        return null;

    }

    /**
     * Discards all bottom-row buildings.
     *
     * @return true
     */
    public boolean discardBottomBuildings(){
        bottomBuildings.clear();
        return true;
    }

    // --- Setters for save/load restoration ---

    /**
     * Replaces the top-row cards with a copy of the given list (save/load restore).
     *
     * @param cards the cards to set
     */
    public void setTopCards(List<Card> cards) { this.topCards = new ArrayList<>(cards); }

    /**
     * Replaces the bottom-row cards with a copy of the given list (save/load restore).
     *
     * @param cards the cards to set
     */
    public void setBottomCards(List<Card> cards) { this.bottomCards = new ArrayList<>(cards); }

    /**
     * Replaces the top-row buildings with a copy of the given list (save/load restore).
     *
     * @param b the buildings to set
     */
    public void setTopBuildings(List<Building> b) { this.topBuildings = new ArrayList<>(b); }

    /**
     * Replaces the bottom-row buildings with a copy of the given list (save/load restore).
     *
     * @param b the buildings to set
     */
    public void setBottomBuildings(List<Building> b) { this.bottomBuildings = new ArrayList<>(b); }

}
