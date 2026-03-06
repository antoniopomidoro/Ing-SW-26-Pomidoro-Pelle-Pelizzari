package it.polimi.ingsw.model;

/**
 * Represents a Hunter card, which may or may not provide food.
 * Instances of this class are intended to be created from JSON data.
 */
public class Hunter extends Card {
    private boolean food;

    /**
     * Default constructor for Hunter.
     * The constructor is empty as instances of this class will be populated using JSON deserialization.
     */
    public Hunter() {
        super();
    }

    /**
     * Checks if the card provides food.
     * @return True if the card provides food, false otherwise.
     */
    public boolean hasFood() {
        return food;
    }

    /**
     * Sets whether the card provides food. This method is intended to be used by the JSON deserializer.
     * @param food True if the card should provide food, false otherwise.
     * @return Always returns true, indicating the value was set.
     */
    public boolean setFood(boolean food) {
        this.food = food;
        return true;
    }
}
