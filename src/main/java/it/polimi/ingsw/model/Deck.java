package it.polimi.ingsw.model;
import java.util.ArrayList;
import java.util.Collections;




public class Deck {
    private ArrayList<Card> deckA1;
    private ArrayList<Card> deckA2;
    private ArrayList<Card> deckA3;
    private ArrayList<Building> deckB1;
    private ArrayList<Building> deckB2;
    private ArrayList<Building> deckB3;
    public Deck() {
        // Skeleton constructor
    }

    public void shuffle() {
        Collections.shuffle(deckA1);
        Collections.shuffle(deckA2);
        Collections.shuffle(deckA3);
        return;
    }

    //add a card to the characters deck in the specified Age
    public boolean addCard(Card c, Age a) throws buildingInDeckException {
        if(c.getClass() == Building.class){
            throw new buildingInDeckException();
        }
        switch(a){
            case AGE_1:
                deckA1.add(c);
                return true;
            case AGE_2:
                deckA2.add(c);
                return true;
            case AGE_3:
                deckA3.add(c);
                return true;
            default:
                return false;
        }

    }
    public boolean addBuilding(Building b, Age a){
        switch (a){
            case AGE_1:
                deckB1.add(b);
                return true;
            case AGE_2:
                deckB2.add(b);
                return true;
            case AGE_3:
                deckB3.add(b);
                return true;
            default:
                return false;
        }


    }
//return the first card on the deck if the deck is empty throws an exception
    public Card popCard(Age a) {
        switch(a){
            case AGE_1:
                return deckA1.removeFirst();
            case AGE_2:
                return deckA2.removeFirst();
            case AGE_3:
                return deckA3.removeFirst();
            default:
                return null;
        }

    }

    public ArrayList getBuildings(Age age) {
            switch (age) {
                case AGE_1:
                    return deckB1;
                case AGE_2:
                    return deckB2;
                case AGE_3:
                    return deckB3;
                default:
                    return null;
            }
    }


    public static class buildingInDeckException extends Exception {
    }
}
