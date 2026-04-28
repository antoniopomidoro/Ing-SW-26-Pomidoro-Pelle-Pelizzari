package it.polimi.ingsw.model.cards;
import java.util.*;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.Building;
import it.polimi.ingsw.model.game.Age;


public class Decks {
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

    public boolean addBuilding(Building b) {
        if (b == null || b.getAge() == null || this.buildings.get(b.getAge()) == null) {
            return false;
        }
        this.buildings.get(b.getAge()).add(b);
        return true;
    }

    }
