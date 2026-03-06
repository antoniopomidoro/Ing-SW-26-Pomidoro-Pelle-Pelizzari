package it.polimi.ingsw.model;

/**
 * Represents a Shaman card, which has a certain number of stars.
 * Instances of this class are intended to be created from JSON data.
 */
public class Shaman extends Card {
    private int stars;

    /**
     * Default constructor for Shaman.
     * The constructor is empty as instances of this class will be populated using JSON deserialization.
     */
    public Shaman() {
        super();
    }

    /**
     * Gets the number of stars on the card.
     * @return The number of stars.
     */
    public int getStars() {
        return stars;
    }

    /**
     * Sets the number of stars for the card. This method is intended to be used by the JSON deserializer.
     * @param stars The number of stars to set.
     * @return True if the stars were set successfully, false otherwise.
     */
    public boolean setStars(int stars) {
        if (stars <= 0){
            return false;
        }
        this.stars = stars;
        return true;
    }
}
