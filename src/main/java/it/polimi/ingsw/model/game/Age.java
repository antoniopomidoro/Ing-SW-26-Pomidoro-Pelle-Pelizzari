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

    private final int numericValue;

    Age(int numericValue) {
        this.numericValue = numericValue;
    }

    public int getValue() {
        return this.numericValue;
    }

    public boolean hasNext() {
        return this.ordinal() < Age.values().length - 1;
    }

    public Age getNext() {
        if (!hasNext()) {
            throw new IllegalStateException("No next age");
        }
        return Age.values()[this.ordinal() + 1];
    }
}
