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
        setId(CharacterEnum.INVENTOR);
    }
    /**
     * Constructs an Inventor for the given age associated with a tool.
     *
     * @param age  the age the card belongs to
     * @param tool the tool associated with the inventor
     */
    public Inventor(Age age, Tool tool){
        this.age = age;
        this.tool = tool;
        setId(CharacterEnum.INVENTOR);
    }

    /**
     * Test-only constructor that sets the tool without an age.
     *
     * @param tool the tool associated with the inventor
     */
    protected Inventor(Tool tool) {
        this.tool = tool;
        setId(CharacterEnum.INVENTOR);
    }

    /**
     * Gets the tool associated with this card.
     * @return The tool.
     */
    public Tool getTool() {
        return tool;
    }

    /**
     * Updates the player's stats when this Inventor is added to their hand,
     * incrementing the character count and the associated tool count.
     *
     * @param p the player receiving the card
     * @return true on success, false if the player is null
     */
    @Override
    public boolean onAddedToPlayer(Player p) {
        if (p == null) {
            return false;
        }
        return p.getStats().incrementCharacter(getId())
                && p.getStats().incrementTool(tool);
    }

    /**
     * Returns a human-readable representation of this Inventor card.
     *
     * @return a string describing age and tool
     */
    @Override
    public String toString(){
        return "INVENTOR, " + super.age + ", TOOL: " + tool + "\n";
    }
}
