package it.polimi.ingsw.model.cards.characters;

import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.cards.characters.*;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.effects.contextual.*;
import it.polimi.ingsw.model.effects.events.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;


/**
 * Represents a Shaman card, which has a certain number of stars.
 * Instances of this class are intended to be created from JSON data.
 */
public class Shaman extends it.polimi.ingsw.model.cards.Character {
    private int stars;

    /**
     * Default constructor for Shaman.
     * The constructor is empty as instances of this class will be populated using JSON deserialization.
     */
    public Shaman() {
        super();
    }

    /**
     * Gets the number of stars on the card.
     * @return The number of stars.
     */
    public int getStars() {
        return stars;
    }

    /**
     * Sets the number of stars for the card. This method is intended to be used by the JSON deserializer.
     * @param stars The number of stars to set.
     * @return True if the stars were set successfully, false otherwise.
     */
    public boolean setStars(int stars) {
        if (stars <= 0){
            return false;
        }
        this.stars = stars;
        return true;
    }
}
