package it.polimi.ingsw.model.cards;
import java.util.*;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.Building;
import it.polimi.ingsw.model.game.Age;


public class Decks {
    private  Map<Age,List<Card>> buildings;
    private  Map<Age,List<Card>> cards;
    private Optional<Card> card;
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
            c.addToDeck(this.cards);}

        for(Building b : buildings){
            b.addToDeck(this.buildings);
        }
        
    }
    //the shuffle method remove the final methods then shuffle all non-empty decks then add final events
    public void shuffle() {
        for(Age a:Age.values()){
           Collections.shuffle(cards.get(a));
        }
    }


    //return the first card on the age deck if the deck is empty send an empty optional
    public Optional<Card> popCard(Age a){
        if(cards.get(a).isEmpty() && a.getValue() != Age.values()[a.ordinal()+1].getValue()){
            return Optional.empty();
    }else if(cards.get(a).isEmpty() && a.getValue() == Age.values()[a.ordinal()+1].getValue()){
            //this else if statement hide the Age_3_final enum to external classes
            if(cards.get(Age.values()[a.ordinal()+1]).isEmpty()){
                return Optional.empty();
            }else{
                card = Optional.of(cards.get(Age.values()[a.ordinal()+1]).removeLast());
                return card;
            }
        }
        else{
            card = Optional.of(cards.get(Age.values()[a.ordinal()]).removeLast());
            return card;
        }
    }

    public List<Card> getBuildings(Age age) {
        List<Card> sup = buildings.get(age);
        return sup;
    }


    }
