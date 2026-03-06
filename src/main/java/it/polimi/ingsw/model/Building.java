package it.polimi.ingsw.model;

/**
 * Represents a Building card, which has a food cost and provides prestige points.
 * Instances of this class are intended to be created from JSON data.
 */
public class Building extends Card {
    private int foodCost;
    private int pp;

    /**
     * Default constructor for Building.
     * The constructor is empty as instances of this class will be populated using JSON deserialization.
     */
    public Building() {
        super();
    }

    /**
     * Gets the food cost required to build this building.
     * @return The food cost.
     */
    public int getFoodCost() {
        return this.foodCost;
    }

    /**
     * Sets the food cost for this building. This method is intended to be used by the JSON deserializer.
     * @param foodCost The food cost to set.
     * @return True if the food cost was set successfully, false otherwise.
     */
    public boolean setFoodCost(int foodCost) {
        if (foodCost < 0) {
            return false;
        }

        this.foodCost = foodCost;
        return true;
    }

    /**
     * Gets the prestige points (PP) provided by this building.
     * @return The prestige points.
     */
    public int getPP() {
        return this.pp;
    }

    /**
     * Sets the prestige points (PP) for this building. This method is intended to be used by the JSON deserializer.
     * @param pp The prestige points to set.
     * @return True if the prestige points were set successfully, false otherwise.
     */
    public boolean setPP(int pp) {
        if (pp < 0) {
            return false;
        }

        this.pp=pp;
        return true;
    }
}
