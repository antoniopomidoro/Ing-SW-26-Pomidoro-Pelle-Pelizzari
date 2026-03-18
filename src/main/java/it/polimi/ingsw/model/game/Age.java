package it.polimi.ingsw.model.game;

import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.cards.characters.*;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.effects.contextual.*;
import it.polimi.ingsw.model.effects.events.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;


public enum Age {
    AGE_1(1),
    AGE_2(2),
    AGE_3(3),
    AGE_3_FINAL(3);

    // Il campo immutabile che contiene il vero valore numerico
    private final int numericValue;

    // Il costruttore dell'Enum
    Age(int numericValue) {
        this.numericValue = numericValue;
    }

    // Il metodo pubblico e sicuro da chiamare in giro per il codice
    public int getValue() {
        return this.numericValue;
    }
}
