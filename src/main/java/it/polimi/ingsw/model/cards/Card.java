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

import it.polimi.ingsw.model.game.TriggerKey;

import java.io.Serializable;
import java.util.*;


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
public abstract class Card implements Serializable {
    protected Age age;
    protected int minPlayers = 0;
    protected String instanceId = UUID.randomUUID().toString();

    /** Card ID from JSON (e.g. "C1"), used by the client to map card → PNG image. */
    protected String cardId;

    public enum CardCategory {
        CHARACTER,
        EVENT,
        BUILDING
    }

    /**
     * Semantic category of the card used for DTO serialization and client rendering.
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

    public String getInstanceId() {
        return instanceId;
    }

    /** @return Card ID from JSON, used for client-side image association. */
    public String getCardId() {
        return cardId;
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
     * Trigger key associated with this card, if any.
     * Event and Building cards override this to return their key.
     *
     * @return an Optional containing the TriggerKey, or empty if not applicable
     */
    public Optional<TriggerKey> getTriggerKey() {
        return Optional.empty();
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

    /**
     * Hook invoked when this card is added to a player's hand.
     * Default is a no-op; character subclasses override to update player stats.
     *
     * @param p the player receiving the card
     * @return true on success
     */
    public boolean onAddedToPlayer(Player p) {
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

    /**
     * Polymorphic accessor for the character type. Returns {@code null} for
     * non-character cards; {@link Character} overrides to return its enum id.
     * Callers that need null-safety should use
     * {@code Comparator.nullsLast(Comparator.naturalOrder())}.
     */
    public CharacterEnum getId() {
        return null;
    }

    public String toString() {return "";}


}
