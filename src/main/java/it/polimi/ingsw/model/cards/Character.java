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


/**
 * Represents a Character card, which extends the basic Card.
 * Characters have a specific type (ID) and a minimum number of players requirement.
 * Instances of this class are intended to be created from JSON data.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "characterType"
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
    private int minPlayers;

    /**
     * Default constructor for Character.
     */
    public Character() {

    }

    /**
     * Gets the type (ID) of the character.
     * @return The character type.
     */
    public CharacterEnum getId() {
        return id;
    }

    /**
     * Gets the minimum number of players required for this character card.
     * @return The minimum number of players.
     */
    public int getMinPlayers() {
        return minPlayers;
    }

    /**
     * Method triggered when the character is added to a player.
     * @param p The player adding the character.
     */
    public void onAddedToPlayer(Player p) {
        // Skeleton method
    }
}
