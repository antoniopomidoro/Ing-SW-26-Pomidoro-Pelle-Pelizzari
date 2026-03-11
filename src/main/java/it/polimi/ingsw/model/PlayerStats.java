package it.polimi.ingsw.model;

import java.util.Map;

public class PlayerStats {
    private Map<CharacterEnum, Integer> characterCounts;
    private int buildingDiscount;
    private int ritualLossMultiplier;
    private int sustainmentDiscount;
    private int stars;
    private int builderPp;
    private int ritualWinBoost;
    private int differentToolNumber;

    public void incrementCharacter(CharacterEnum type) {
        // Skeleton method
    }

    public int getCharacterCount(CharacterEnum type) {
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

    public int getSustainmentDiscount() {
        return sustainmentDiscount;
    }

    public void addStars(int amount) {
        // Skeleton method
    }

    public int getStars() {
        return stars;
    }

    public void setBuilderPp(int amount) {
        // Skeleton method
    }

    public int getBuilderPp() {
        return builderPp;
    }

    public void setRitualWinBoost(int amount) {
        // Skeleton method
    }

    public int getRitualWinBoost() {
        return ritualWinBoost;
    }

    public void updateDifferentToolNumber(int amount) {
        // Skeleton method
    }

    public int getDifferentToolNumber() {
        return differentToolNumber;
    }
}
