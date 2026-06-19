package it.polimi.ingsw.model.cards;
import java.io.Serializable;
import java.util.*;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.Building;
import it.polimi.ingsw.model.game.Age;


/**
 * Holds the game's draw decks, keeping cards and buildings separated and
 * partitioned by {@link Age}. Cards and buildings are routed into the correct
 * collection through the Visitor pattern ({@link Card#addToDeck(Decks)}).
 */
public class Decks implements Serializable {
    private  Map<Age,List<Building>> buildings;
    private  Map<Age,List<Card>> cards;
    /** Default constructor for Jackson deserialization. */
    public Decks() {
        this.cards = new EnumMap<>(Age.class);
        this.buildings = new EnumMap<>(Age.class);
        for (Age a : Age.values()) {
            this.cards.put(a, new ArrayList<>());
            this.buildings.put(a, new ArrayList<>());
        }
    }

    /**
     * Builds the decks from flat lists of cards and buildings, partitioning each
     * element by age. Every element is dispatched through {@link Card#addToDeck(Decks)}
     * (Visitor pattern) so it is routed to the correct per-age collection.
     *
     * @param cards     the cards to distribute across the age decks
     * @param buildings the buildings to distribute across the age decks
     */
    public Decks(List<Card> cards, List<Building> buildings){
        this.cards = new EnumMap<>(Age.class);
        this.buildings= new EnumMap<>(Age.class);
        for(Age a:Age.values()){
            this.cards.put(a,new ArrayList<>());
            this.buildings.put(a,new ArrayList<>());
        }
        for(Card c :cards){
            c.addToDeck(this);}

        for(Building b : buildings){
            b.addToDeck(this);
        }
        
    }

    /* Protected methods for testing purposes */

    /**
     * Returns the full cards map for all ages.
     *
     * @return the cards map keyed by age
     */
    public Map<Age, List<Card>> getCards() {
        return cards;
    }

    /**
     * Returns the full buildings map for all ages.
     * @return the buildings map
     */
    public Map<Age, List<Building>> getAllBuildings() {
        return buildings;
    }

    /**
     * Replaces the per-age cards map. Used by Jackson deserialization and tests.
     *
     * @param cards the cards map to set
     */
    public void setCards(Map<Age, List<Card>> cards) {
        this.cards = cards;
    }

    /**
     * Replaces the per-age buildings map. Used by Jackson deserialization and tests.
     *
     * @param buildings the buildings map to set
     */
    public void setBuildings(Map<Age, List<Building>> buildings) {
        this.buildings = buildings;
    }

    /**
     * Shuffles every non-empty per-age card deck in place.
     *
     * @return true if the decks were shuffled, false if the cards map is null
     */
    public boolean shuffle() {
        if (cards == null) {
            return false;
        }
        for(Age a:Age.values()){
            List<Card> ageDeck = cards.get(a);
            List<Building> ageBuildings = buildings.get(a);
            if (ageDeck != null) {
                Collections.shuffle(ageDeck);
            }
        }
        return true;
    }


    /**
     * Returns the top card of the deck for the given age without removing it.
     *
     * @param a the age whose deck to peek
     * @return the top card, or empty if the deck is empty
     */
    public Optional<Card> peekCard(Age a) {
        List<Card> deck = cards.get(a);
        if (deck == null) return Optional.empty();
        if (!deck.isEmpty()) return Optional.of(deck.getLast());
        if (a.hasNext() && a.getNext().getValue() == a.getValue()) {
            List<Card> overflow = cards.get(a.getNext());
            if (overflow != null && !overflow.isEmpty()) return Optional.of(overflow.getLast());
        }
        return Optional.empty();
    }

    /**
     * Removes and returns the top card of the deck for the given age. If the
     * age's deck is empty, falls back to the overflow deck of the next age when
     * it shares the same numeric value.
     *
     * @param a the age whose deck to draw from
     * @return the drawn card, or empty if no card is available
     */
    public Optional<Card> popCard(Age a) {
        List<Card> deck = cards.get(a);
        if (deck == null) return Optional.empty();
        if (!deck.isEmpty()) {
            return Optional.of(deck.removeLast());
        }
        // If the next age shares the same numeric value it is an overflow deck for this age
        if (a.hasNext() && a.getNext().getValue() == a.getValue()) {
            List<Card> overflowDeck = cards.get(a.getNext());
            if (overflowDeck != null && !overflowDeck.isEmpty()) {
                return Optional.of(overflowDeck.removeLast());
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the buildings available for the given age.
     *
     * @param age the age to query
     * @return the list of buildings for that age, or an empty list if none
     */
    public List<Building> getBuildings(Age age) {
        if (buildings == null || buildings.get(age) == null) {
            return Collections.emptyList();
        }
        return buildings.get(age);

    }

    /**
     * Adds a card to the deck of its own age.
     *
     * @param c the card to add
     * @return true if added, false if the card or its age is null or unknown
     */
    public boolean addCard(Card c) {
        if (c == null || c.getAge() == null || this.cards.get(c.getAge()) == null) {
            return false;
        }
        this.cards.get(c.getAge()).add(c);
        return true;
    }

    /**
     * Returns the number of cards remaining in the deck for the given age,
     * including any overflow from the next age with the same numeric value.
     *
     * @param a the age to query
     * @return remaining card count (0 if none or age is null)
     */
    public int remainingCards(Age a) {
        if (a == null || cards == null) return 0;
        List<Card> deck = cards.get(a);
        int count = deck == null ? 0 : deck.size();
        if (a.hasNext() && a.getNext().getValue() == a.getValue()) {
            List<Card> overflow = cards.get(a.getNext());
            if (overflow != null) count += overflow.size();
        }
        return count;
    }

    /**
     * Adds a building to the deck of its own age.
     *
     * @param b the building to add
     * @return true if added, false if the building or its age is null or unknown
     */
    public boolean addBuilding(Building b) {
        if (b == null || b.getAge() == null || this.buildings.get(b.getAge()) == null) {
            return false;
        }
        this.buildings.get(b.getAge()).add(b);
        return true;
    }

    }
