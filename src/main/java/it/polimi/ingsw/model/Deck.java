package it.polimi.ingsw.model;

import java.util.List;
import java.util.ArrayList;

public class Deck {
    private List<Card> age_1_Cards;
    private List<Card> age_2_Cards;
    private List<Card> age_3_Cards;
    private List<Building> age_1_Buildings;
    private List<Building> age_2_Buildings;
    private List<Building> age_3_Buildings;

    public Deck() {
        // Skeleton constructor
    }

    public boolean shuffle() {
        return false;
    }

    public Card popCard(Age age) {
        return null;
    }

    public Building popBuilding(Age age) {
        return null;
    }
}
