package it.polimi.ingsw.model.cards.characters;

import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.cards.Character;
import it.polimi.ingsw.model.cards.characters.*;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.effects.contextual.*;
import it.polimi.ingsw.model.effects.events.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;


/**
 * Represents a Builder card, a specific type of card that provides discounts and prestige points.
 * Instances of this class are intended to be created from JSON data.
 */
public class Builder extends Character {
    private int discount;
    private int pp;

    /**
     * Default constructor for Builder.
     * The constructor is empty as instances of this class will be populated using JSON deserialization.
     */
    public Builder() {

    }

    /**
     * Gets the discount value provided by the card.
     * @return The discount value.
     */
    public int getDiscount() {
        return this.discount;
    }

    /**
     * Gets the prestige points (PP) provided by the card.
     * @return The prestige points.
     */
    public int getPP() {
        return this.pp;
    }

    @Override
    public void onAddedToPlayer(Player p) {
        p.getStats().incrementCharacter(CharacterEnum.BUILDER);
        p.getStats().addBuilderPp(pp);
        p.getStats().addBuildingDiscount(discount);
    }
}
