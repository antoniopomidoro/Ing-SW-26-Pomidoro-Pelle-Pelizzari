package it.polimi.ingsw.model;

public class Hunter extends Card {
    private boolean food;

    public Hunter() {
        super();
    }

    public boolean hasFood() {
        return food;
    }

    public boolean setFood(boolean food) {
        this.food = food;
        return false;
    }
}
