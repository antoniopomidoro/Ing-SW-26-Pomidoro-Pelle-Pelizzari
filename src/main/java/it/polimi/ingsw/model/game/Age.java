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
    AGE_1(1, true),
    AGE_2(2, true),
    AGE_3(3, true),
    AGE_3_FINAL(3, false);

    private final int numericValue;
    private final boolean age;

    Age(int numericValue, boolean age) {
        this.numericValue = numericValue;
        this.age = age;
    }

    public int getValue() {
        return this.numericValue;
    }

    public boolean isAge() {
        return this.age;
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
