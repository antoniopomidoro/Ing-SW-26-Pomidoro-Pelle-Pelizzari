package it.polimi.ingsw.model.cards;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.cards.characters.*;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.effects.contextual.*;
import it.polimi.ingsw.model.effects.events.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;


/**
 * Represents an abstract game card with its basic properties.
 * This class serves as a base for all specific card types in the game.
 * Concrete subclasses of this class are intended to be created from JSON data.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "cardType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Character.class, name = "CHARACTER"),
        @JsonSubTypes.Type(value = Event.class, name = "EVENT"),
        @JsonSubTypes.Type(value = Building.class, name = "BUILDING")
})
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
     *
     * @return The age of the card.
     */
    public Age getAge() {
        return this.age;
    }

    /**
     * Returns the resolution priority of this card when discarded.
     * Cards with lower priority are resolved first. Default is 0.
     * Subclasses (e.g., Event) can override this to control discard ordering.
     *
     * @return The resolution priority.
     */
    public int getResolutionPriority() {
        return 0;
    }

    /**
     * Handles the logic when a card is discarded.
     * By default, it simply returns true. Subclasses can override this to implement specific behavior (e.g., Events).
     *
     * @param state The current game state.
     * @param p     The list of players involved.
     * @return True if the discard action was processed successfully.
     */
    public boolean onDiscard(GameState state, List<Player> p) {
        return true;
    }
    //method that says if the selected card is a building
    //method that add the card to a list
    public boolean addToDeck(EnumMap<Age,List<Card>> deck){
        if(this.age== null) {
            return false;
        }
        else{deck.get(this.age).add(this);
            return true;}

        }
    public boolean isBuyable() {
        return true;
    }
    public boolean addToDeck(Map<Age,List<Card>> deck) {
        return false;
    }
}

