package it.polimi.ingsw.model.cards;

import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.cards.characters.*;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.effects.contextual.*;
import it.polimi.ingsw.model.effects.events.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;


/**
 * Represents an abstract game card with its basic properties.
 * This class serves as a base for all specific card types in the game.
 * Concrete subclasses of this class are intended to be created from JSON data.
 */
public abstract class Card {
    private Age age;

    /**
     * Default constructor for Card.
     * The constructor is empty as instances of its subclasses will be populated using JSON deserialization.
     */
    public Card() {

    }

    /**
     * Gets the age (era) of the card.
     * @return The age of the card.
     */
    public Age getAge() {
        return this.age;
    }
}
