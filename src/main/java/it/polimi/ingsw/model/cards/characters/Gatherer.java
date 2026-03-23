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
 * Represents a Gatherer card.
 * This class serves as a marker to identify cards of the Gatherer type.
 * It has no additional properties or methods beyond the base Character class.
 */
public class Gatherer extends Character {
    private int sustDisc;
    /**
     * Default constructor for Gatherer.
     * The constructor is empty as instances of this class will be populated using JSON deserialization.
     */
    public Gatherer() {
        super();
    }

    public Gatherer(Age age, int sustDisc){
        this.sustDisc = sustDisc;
        this.age = age;
    }


    @Override
    public void onAddedToPlayer(Player p) {
        p.getStats().incrementCharacter(CharacterEnum.GATHERER);
        p.getStats().addSustainmentDiscount(sustDisc);
    }

    public int getSustDisc() {
        return sustDisc;
    }

    @Override
    public Card copy() {
        return new Gatherer(this.age, this.sustDisc);
    }
}
