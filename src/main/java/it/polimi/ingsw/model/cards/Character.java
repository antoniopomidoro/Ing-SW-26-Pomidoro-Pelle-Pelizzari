package it.polimi.ingsw.model.cards;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.cards.characters.*;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.effects.contextual.*;
import it.polimi.ingsw.model.effects.events.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;


/**
 * Represents a Character card, which extends the basic Card.
 * Characters have a specific type (ID) and a minimum number of players requirement.
 * Instances of this class are intended to be created from JSON data.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "id",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Artist.class, name = "ARTIST"),
        @JsonSubTypes.Type(value = Gatherer.class, name = "GATHERER"),
        @JsonSubTypes.Type(value = Shaman.class, name = "SHAMAN"),
        @JsonSubTypes.Type(value = Builder.class, name = "BUILDER"),
        @JsonSubTypes.Type(value = Inventor.class, name = "INVENTOR"),
        @JsonSubTypes.Type(value = Hunter.class, name = "HUNTER")
})
public abstract class Character extends Card {
    private CharacterEnum id;

    /**
     * Default constructor for Character.
     */
    public Character() {
        super();
    }

    /**
     * Gets the type (ID) of the character.
     * @return The character type.
     */
    @Override
    public CharacterEnum getId() {
        return id;
    }

    /**
     * Test-only setter. Production instances populate {@code id} via Jackson
     * deserialization; non-Jackson constructors must call this so {@link #getId()}
     * returns the correct value at runtime.
     */
    protected void setId(CharacterEnum id) {
        this.id = id;
    }

    /**
     * Gets the minimum number of players required for this character card.
     * @return The minimum number of players.
     */
    public int getMinPlayers() {
        return super.getMinPlayers();
    }

    /**
     * Method triggered when the character is added to a player.
     * @param p The player adding the character.
     */
    public boolean onAddedToPlayer(Player p) {
        return p != null;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link CardCategory#CHARACTER}
     */
    @Override
    public CardCategory getCategory() {
        return CardCategory.CHARACTER;
    }
}
