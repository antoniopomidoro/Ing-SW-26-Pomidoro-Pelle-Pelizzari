package it.polimi.ingsw.model.cards;
import java.io.Serializable;
import java.util.*;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.Building;
import it.polimi.ingsw.model.game.Age;


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

    //the constructor initialize 2 enum map that contains lists divided by era of cards and buildings
    //then for each card and buildin in the lists passed in input creates the ages decks using the function addToDeck of card
    //using visitors pattern
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

    public void setCards(Map<Age, List<Card>> cards) {
        this.cards = cards;
    }

    public void setBuildings(Map<Age, List<Building>> buildings) {
        this.buildings = buildings;
    }

    //the shuffle method remove the final methods then shuffle all non-empty decks then add final events
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

    public List<Building> getBuildings(Age age) {
        if (buildings == null || buildings.get(age) == null) {
            return Collections.emptyList();
        }
        return buildings.get(age);

    }
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

    public boolean addBuilding(Building b) {
        if (b == null || b.getAge() == null || this.buildings.get(b.getAge()) == null) {
            return false;
        }
        this.buildings.get(b.getAge()).add(b);
        return true;
    }

    }
