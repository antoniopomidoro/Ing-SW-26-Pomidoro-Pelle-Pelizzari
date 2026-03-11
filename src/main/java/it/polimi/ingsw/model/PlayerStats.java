package it.polimi.ingsw.model;

import java.util.Map;

public class PlayerStats {
    private Map<Character_Enum, Integer> characterCounts;
    private int buildingDiscount;
    private int ritualLossMultiplier;
    private int sustainmentDiscount;
    private int stars;
    private int builderPp;
    private int ritualWinBoost;
    private int differentToolNumber;

    public void incrementCharacter(Character_Enum type) {
        // Skeleton method
    }

    public int getCharacterCount(Character_Enum type) {
        return 0;
    }

    public void addBuildingDiscount(int amount) {
        // Skeleton method
    }

    public int getBuildingDiscount() {
        return buildingDiscount;
    }

    public void setRitualLossMultiplier(int mult) {
        // Skeleton method
    }

    public int getRitualLossMultiplier() {
        return ritualLossMultiplier;
    }

    public void addSustainmentDiscount(int amount) {
        // Skeleton method
    }

    public void addStars(int amount) {
        // Skeleton method
    }

    public void setBuilderPp(int amount) {
        // Skeleton method
    }

    public void setRitualWinBoost(int amount) {
        // Skeleton method
    }

    public void updateDifferentToolNumber(int amount) {
        // Skeleton method
    }
}
