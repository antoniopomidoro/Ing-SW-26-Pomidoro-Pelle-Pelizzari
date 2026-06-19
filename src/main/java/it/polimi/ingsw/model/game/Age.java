package it.polimi.ingsw.model.game;

import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.cards.characters.*;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.effects.contextual.*;
import it.polimi.ingsw.model.effects.events.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;


/**
 * Represents the ages (eras) the game progresses through. Each age carries a
 * numeric value used for scoring and penalties; {@code AGE_3_FINAL} shares the
 * numeric value of {@code AGE_3} but acts as a non-playable overflow deck rather
 * than a distinct playable age.
 */
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

    /**
     * Gets the numeric value of this age, used in scoring and penalties.
     *
     * @return the numeric value
     */
    public int getValue() {
        return this.numericValue;
    }

    /**
     * Indicates whether this is a playable age (as opposed to a final overflow age).
     *
     * @return true if this is a playable age
     */
    public boolean isAge() {
        return this.age;
    }

    /**
     * Indicates whether a following age exists after this one.
     *
     * @return true if there is a next age
     */
    public boolean hasNext() {
        return this.ordinal() < Age.values().length - 1;
    }

    /**
     * Returns the age immediately following this one.
     *
     * @return the next age
     * @throws IllegalStateException if this is the last age
     */
    public Age getNext() {
        if (!hasNext()) {
            throw new IllegalStateException("No next age");
        }
        return Age.values()[this.ordinal() + 1];
    }
}
