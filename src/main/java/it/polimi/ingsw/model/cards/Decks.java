package it.polimi.ingsw.model.cards;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import it.polimi.ingsw.model.game.Age;


public class Decks {
    private ArrayList<ArrayList<Card>> cards;
    private ArrayList<ArrayList<Card>> buildings;
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

    public void shuffle() {
        Event LastEvent1 = (Event)cards.get(2).removeLast();
        Event LastEvent2 = (Event)cards.get(2).removeLast();
        for(ArrayList<Card> a:cards){
            Collections.shuffle(a);
        }
        Random rn = new Random();
        int num = rn.nextInt(2);
        if(num==1){
            cards.get(2).addLast(LastEvent1);
        }else{
            cards.get(2).addLast(LastEvent2);
        }
        return;
    }


    //return the first card on the age deck if the deck is empty throws an exception if the last deck is empty throws endgame exception
    public Card popCard(Age a) throws endEraEx,endGameEx{
        if(cards.get(a.ordinal()).isEmpty()){
            if(a.ordinal()==2){
                throw new endGameEx();
            }else{
                throw new endEraEx();
            }
    }else{
            return cards.get(a.ordinal()).removeFirst();
        }
    }

    public ArrayList<Card> getBuildings(Age age) {
        return buildings.get(age.ordinal());
    }

    public static class endGameEx extends Exception{

    }

    public static class endEraEx extends Exception {
    }

    public static class buildingInDeckEx extends Exception {
    }
}
