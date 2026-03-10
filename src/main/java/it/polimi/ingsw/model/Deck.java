package it.polimi.ingsw.model;

import java.util.ArrayList;
import java.util.Collections;




public class Deck {
    private ArrayList<Card> deckE1;
    private ArrayList<Card> deckE2;
    private ArrayList<Card> deckE3;
    private ArrayList<Building> deckB1;
    private ArrayList<Building> deckB2;
    private ArrayList<Building> deckB3;
    public Deck() {
        // Skeleton constructor
    }

    public void shuffle() {
        Collections.shuffle(deckE1);
        Collections.shuffle(deckE2);
        Collections.shuffle(deckE3);
        return;
    }

    //add a card to the characters deck in the specified Age
    public boolean addCard(Card c, Age a) throws BuildingInDeckException {
        if(c.getClass() == Building.class){
            throw new BuildingInDeckException();
        }
        switch(a){
            case AGE_1:
                deckE1.add(c);
                return true;
            case AGE_2:
                deckE2.add(c);
                return true;
            case AGE_3:
                deckE3.add(c);
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
                return deckE1.removeFirst();
            case AGE_2:
                return deckE2.removeFirst();
            case AGE_3:
                return deckE3.removeFirst();
            default:
                return null;
        }

    }

    public Building getBuildings(Age age) {
             return null;
    }

    public static class BuildingInDeckException extends Exception {

    }

}
