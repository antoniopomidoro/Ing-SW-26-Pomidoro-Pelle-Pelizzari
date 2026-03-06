package it.polimi.ingsw.model;

/**
 * Represents a Builder card, a specific type of card that provides discounts and prestige points.
 * Instances of this class are intended to be created from JSON data.
 */
public class Builder extends Card {
    private int discount;
    private int pp;

    /**
     * Default constructor for Builder.
     * The constructor is empty as instances of this class will be populated using JSON deserialization.
     */
    public Builder() {

    }

    /**
     * Gets the discount value provided by the card.
     * @return The discount value.
     */
    public int getDiscount() {
        return this.discount;
    }

    /**
     * Sets the discount value for the card. This method is intended to be used by the JSON deserializer.
     * @param discount The discount value to set.
     * @return True if the discount was set successfully, false otherwise.
     */
    public boolean setDiscount(int discount) {
        if (discount < 0) {
            return false;
        }

        this.discount = discount;
        return true;
    }

    /**
     * Gets the prestige points (PP) provided by the card.
     * @return The prestige points.
     */
    public int getPP() {
        return this.pp;
    }

    /**
     * Sets the prestige points (PP) for the card. This method is intended to be used by the JSON deserializer.
     * @param pp The prestige points to set.
     * @return True if the prestige points were set successfully, false otherwise.
     */
    public boolean setPP(int pp) {
        if (pp < 0) {
            return false;
        }

        this.pp = pp;
        return true;
    }
}
