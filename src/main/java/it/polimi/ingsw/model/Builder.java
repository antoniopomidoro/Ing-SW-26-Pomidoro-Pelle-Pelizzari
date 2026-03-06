package it.polimi.ingsw.model;

public class Builder extends Card {
    private int discount;
    private int pp;

    public Builder() {

    }

    public int getDiscount() {
        return this.discount;
    }

    public boolean setDiscount(int discount) {
        if (discount < 0) {
            return false;
        }

        this.discount = discount;
        return true;
    }

    public int getPP() {
        return this.pp;
    }

    public boolean setPP(int pp) {
        if (pp < 0) {
            return false;
        }

        this.pp = pp;
        return true;
    }
}
