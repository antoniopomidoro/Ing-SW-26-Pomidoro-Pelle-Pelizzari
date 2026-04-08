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
        @JsonSubTypes.Type(value = Artist.class, name = "ARTIST"),
        @JsonSubTypes.Type(value = Gatherer.class, name = "GATHERER"),
        @JsonSubTypes.Type(value = Shaman.class, name = "SHAMAN"),
        @JsonSubTypes.Type(value = Builder.class, name = "BUILDER"),
        @JsonSubTypes.Type(value = Inventor.class, name = "INVENTOR"),
        @JsonSubTypes.Type(value = Hunter.class, name = "HUNTER"),
        @JsonSubTypes.Type(value = Event.class, name = "EVENT"),
        @JsonSubTypes.Type(value = Building.class, name = "BUILDING")
})
public abstract class Card{
    protected Age age;
    protected int minPlayers = 0;

    public enum CardCategory {
        CHARACTER,
        EVENT,
        BUILDING
    }

    /**
     * Categoria semantica della carta usata per serializzazione DTO e rendering client.
     */
    public abstract CardCategory getCategory();

    /**
     * Default constructor for Card.
     * The constructor is empty as instances of its subclasses will be populated using JSON deserialization.
     */
    public Card() {

    }

    /* Protected methods for testing purposes */
    protected void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }

    protected void setAge(Age age) {
        this.age = age;
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
     * Gets the minimum number of players required for this card to be available.
     * Cards without a specific requirement can leave this value to 0.
     *
     * @return minimum required players
     */
    public int getMinPlayers() {
        return minPlayers;
    }

    /**
     * OOP helper used by setup logic to decide if this card can be included.
     *
     * @param playerCount current game player count
     * @return true if the card is valid for this player count
     */
    public boolean isAvailableForPlayers(int playerCount) {
        if (playerCount <= 0) {
            return false;
        }
        return playerCount >= minPlayers;
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
     * @return True if the discard action was processed successfully.
     */
    public boolean onDiscard(GameState state) {
        return true;
    }
    //method that says if the selected card is a building
    //method that add the card to a list
    public boolean addToDeck(Decks manager) {
        if (this.age == null) return false;
        manager.addCard(this);
        return true;
    }
    public boolean isBuyable() {
        return true;
    }


}
