package it.polimi.ingsw.model.cards;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import it.polimi.ingsw.model.game.Age;


public class Decks {
    private final ArrayList<ArrayList<Card>> cards;
    private final ArrayList<ArrayList<Card>> buildings;
    //the constructor initialize 2 list that contains 3 lists divided by era of cards and buildings
    //then for each card and buildin in the lists passed in input creates the ages decks using the function addToDeck of card
    //using visitors pattern
    public Decks(List<Card> cards, List<Building> buildings) throws buildingInDeckEx {
        this.cards = new ArrayList<>();
        this.buildings= new ArrayList<>();
        for(Card c:cards){
            c.addToDeck(this.cards);}
        for(Building b:buildings){
            if(!b.isBuilding()){
                throw new buildingInDeckEx();
            }
            b.addToDeck(this.buildings);
        }
        
    }
    //the shuffle methond remove the final methods then shuffle all non-empty decks then add final events
    public void shuffle() {
        ArrayList<Card> LastEvents = new ArrayList<>();
        Event e;
        int counter =0;
        for(Card c : cards.getLast()){
            if(c.isEvent()){
                e = (Event)c;
                if(e.isFinal()){
                    LastEvents.add(cards.getLast().remove(counter));
                }
        }counter++;}
        for(ArrayList<Card> a:cards){
            if(!a.isEmpty()){
                Collections.shuffle(a);
            }
           }

        if(!LastEvents.isEmpty()){
            Collections.shuffle(LastEvents);
        }
        for(Card c : LastEvents){
            cards.getLast().addLast(c);
        }
    }


    //return the first card on the age deck if the deck is empty throws an exception if the last deck is empty throws endgame exception
    public Card popCard(Age a) throws endEraEx,endGameEx{
        if(cards.get(a.ordinal()).isEmpty()){
            if(a.ordinal()>=cards.size()-1){
                throw new endGameEx();
            }else{
                throw new endEraEx();
            }
    }else{
            return cards.get(a.ordinal()).removeFirst();
        }
    }

    public ArrayList<Card> getBuildings(Age age) {
        ArrayList<Card> buildingCopy = new ArrayList<>();
        buildingCopy.addAll(this.buildings.get(age.ordinal()));
        return buildingCopy;
    }

    public static class endGameEx extends Exception{

    }

    public static class endEraEx extends Exception {
    }

    public static class buildingInDeckEx extends Exception {
    }
}
