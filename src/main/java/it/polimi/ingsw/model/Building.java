package it.polimi.ingsw.model;

public class Building extends Card {
    private int foodCost;
    private int pp;

    public Building() {
        super();
    }

    public int getFoodCost() {
        return this.foodCost;
    }

    public boolean setFoodCost(int foodCost) {
        if (foodCost < 0) {
            return false;
        }

        this.foodCost = foodCost;
        return true;
    }

    public int getPP() {
        return this.pp;
    }

    public boolean setPP(int pp) {
        if (pp < 0) {
            return false;
        }

        this.pp=pp;
        return true;
    }
}
