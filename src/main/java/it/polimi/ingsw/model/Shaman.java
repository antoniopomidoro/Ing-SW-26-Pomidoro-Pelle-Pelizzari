package it.polimi.ingsw.model;

public class Shaman extends Card {
    private int stars;

    public Shaman() {
        super();
    }

    public int getStars() {
        return stars;
    }

    public boolean setStars(int stars) {
        if (stars <= 0){
            return false;
        }
        this.stars = stars;
        return true;
    }
}
