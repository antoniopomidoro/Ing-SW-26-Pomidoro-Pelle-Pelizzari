package it.polimi.ingsw.model.player;

import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.cards.characters.*;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.effects.contextual.*;
import it.polimi.ingsw.model.effects.events.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;


import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class PlayerStats {
    private Map<CharacterEnum, Integer> characterCounts = new EnumMap<>(CharacterEnum.class);
    private int buildingDiscount;
    private int ritualLossMultiplier = 1;
    private int sustainmentDiscount;
    private int stars;
    private int builderPp;
    private int ritualWinBoost = 1;
    private int extraUpperPick = 0;
    private Set<Tool> uniqueTools = EnumSet.noneOf(Tool.class);

    public boolean incrementCharacter(CharacterEnum type) {
        if(type == null) return false;
        characterCounts.put(type, characterCounts.getOrDefault(type, 0) + 1);
        return true;
    }

    public int getCharacterCount(CharacterEnum type) {
        if(type == null) return 0;
        return characterCounts.getOrDefault(type, 0);
    }

    public boolean addBuildingDiscount(int amount) {
        this.buildingDiscount += amount;
        return true;
    }

    public int getBuildingDiscount() {
        return buildingDiscount;
    }

    public boolean setRitualLossMultiplier(int mult) {
        if(mult < 0) return false;
        this.ritualLossMultiplier = mult;
        return true;
    }

    public int getRitualLossMultiplier() {
        return ritualLossMultiplier;
    }

    public boolean addSustainmentDiscount(int amount) {
        this.sustainmentDiscount += amount;
        return true;
    }

    public int getSustainmentDiscount() {
        return sustainmentDiscount;
    }

    public boolean addStars(int amount) {
        this.stars += amount;
        return true;
    }

    public int getStars() {
        return stars;
    }

    public boolean addBuilderPp(int amount) {
        this.builderPp += amount;
        return true;
    }

    public int getBuilderPp() {
        return builderPp;
    }

    public boolean setRitualWinBoost(int amount) {
        this.ritualWinBoost = amount;
        return true;
    }

    public int getRitualWinBoost() {
        return ritualWinBoost;
    }

    public boolean updateDifferentToolNumber(Tool tool) {
        uniqueTools.add(tool);
        return true;
    }

    public int getDifferentToolNumber() {
        return uniqueTools.size();
    }

    public int getExtraUpperPick() {
        return extraUpperPick;
    }

    public void setExtraUpperPick(int extraUpperPick) {
        this.extraUpperPick = extraUpperPick;
    }
}
