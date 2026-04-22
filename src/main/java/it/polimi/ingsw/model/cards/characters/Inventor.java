package it.polimi.ingsw.model.cards.characters;

import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.cards.Character;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.effects.contextual.*;
import it.polimi.ingsw.model.effects.events.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;

/**
 * Represents an Inventor card, which is associated with a specific tool.
 * Instances of this class are intended to be created from JSON data.
 */
public class Inventor extends Character {
    private Tool tool;

    /**
     * Default constructor for Inventor.
     * The constructor is empty as instances of this class will be populated using JSON deserialization.
     */
    public Inventor() {
        super();
    }
    public Inventor(Age age, Tool tool){
        this.age = age;
        this.tool = tool;
    }

    /* protected constructor for test purposes */
    protected Inventor(Tool tool) {
        this.tool = tool;
    }

    /**
     * Gets the tool associated with this card.
     * @return The tool.
     */
    public Tool getTool() {
        return tool;
    }

    @Override
    public boolean onAddedToPlayer(Player p) {
        if (p == null) {
            return false;
        }
        return p.getStats().incrementCharacter(CharacterEnum.INVENTOR)
                && p.getStats().incrementTool(tool);
    }
    @Override
    public String toString(){
        return "INVENTOR, " + super.age + ", TOOL: " + tool + "\n";
    }
}
