package it.polimi.ingsw.model;

public class Inventor extends Card {
    private Tool tool;

    public Inventor() {
        super();
    }

    public Tool getTool() {
        return tool;
    }

    public boolean setTool(Tool tool) {
        if(tool == null){
            return false;
        }
        this.tool = tool;
        return true;
    }
}
