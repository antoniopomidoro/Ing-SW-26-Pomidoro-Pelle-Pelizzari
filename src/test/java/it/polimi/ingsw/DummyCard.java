package it.polimi.ingsw;

import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.game.Age;

public class DummyCard extends Card {
    public DummyCard(){
        super();
    }

    public DummyCard(Age age) {
        this.age = age;
    }

    @Override
    public CardCategory getCategory() {
        return null;
    }
}
