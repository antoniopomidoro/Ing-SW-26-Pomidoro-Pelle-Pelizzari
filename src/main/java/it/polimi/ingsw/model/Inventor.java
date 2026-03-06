package it.polimi.ingsw.model;

/**
 * Represents an Inventor card, which is associated with a specific tool.
 * Instances of this class are intended to be created from JSON data.
 */
public class Inventor extends Card {
    private Tool tool;

    /**
     * Default constructor for Inventor.
     * The constructor is empty as instances of this class will be populated using JSON deserialization.
     */
    public Inventor() {
        super();
    }

    /**
     * Gets the tool associated with this card.
     * @return The tool.
     */
    public Tool getTool() {
        return tool;
    }

    /**
     * Sets the tool for this card. This method is intended to be used by the JSON deserializer.
     * @param tool The tool to set.
     * @return True if the tool was set successfully, false otherwise.
     */
    public boolean setTool(Tool tool) {
        if(tool == null){
            return false;
        }
        this.tool = tool;
        return true;
    }
}
