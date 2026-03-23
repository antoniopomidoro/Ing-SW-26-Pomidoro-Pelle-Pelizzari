package it.polimi.ingsw.model.player;

import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.cards.characters.*;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.effects.contextual.*;
import it.polimi.ingsw.model.effects.events.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;


import java.util.*;

public class PlayerStats {
    private Map<CharacterEnum, Integer> characterCounts = new EnumMap<>(CharacterEnum.class);
    private int buildingDiscount;
    private int ritualLossMultiplier = 1;
    private int baseSustainmentDiscount;
    private Map<CharacterEnum, Integer> sustainmentBoosts = new EnumMap<>(CharacterEnum.class);
    private int stars;
    private int builderPp;
    private int ritualWinBoost = 1;
    private int extraUpperPick = 0;
    private int totemPlacementBonusFood = 0;
    private Set<Tool> uniqueTools = EnumSet.noneOf(Tool.class);
    private Map<Tool, Integer> toolCounts = new EnumMap<>(Tool.class);

    public boolean incrementCharacter(CharacterEnum type) {
        if(type == null) return false;
        characterCounts.put(type, characterCounts.getOrDefault(type, 0) + 1);
        return true;
    }

    public boolean incrementTool(Tool tool) {
        if(tool == null) {
            return false;
        }
        toolCounts.put(tool, toolCounts.getOrDefault(tool,0) + 1);
        return true;
    }

    public int getCharacterCount(CharacterEnum type) {
        if(type == null) return 0;
        return characterCounts.getOrDefault(type, 0);
    }

    public int getToolCount(Tool tool) {
        if(tool == null) {
            return 0;
        }
        return toolCounts.getOrDefault(tool, 0);
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
        this.baseSustainmentDiscount += amount;
        return true;
    }

    /**
     * Registers a building-provided sustainment boost for a character type.
     * During Sustenance events, each character of that type will provide
     * an additional discount equal to gainPerCharacter.
     * @param type The character type that provides the boost.
     * @param gainPerCharacter The discount per character of that type.
     */
    public boolean addSustainmentBoost(CharacterEnum type, int gainPerCharacter) {
        if (type == null || gainPerCharacter < 0) {
            return false;
        }
        sustainmentBoosts.merge(type, gainPerCharacter, Integer::sum);
        return true;
    }

    /**
     * Returns the total sustainment discount: base (from Gatherer cards)
     * plus dynamic boosts (from buildings, calculated per current character count).
     * @return The total sustainment discount.
     */
    public int getSustainmentDiscount() {
        int boost = sustainmentBoosts.entrySet().stream()
                .mapToInt(e -> getCharacterCount(e.getKey()) * e.getValue())
                .sum();
        return baseSustainmentDiscount + boost;
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

    public boolean setExtraUpperPick(int extraUpperPick) {
        if (extraUpperPick < 0) {
            return false;
        }
        this.extraUpperPick = extraUpperPick;
        return true;
    }

    public int calculateSet() {
        return Arrays.stream(CharacterEnum.values())
                .mapToInt(this::getCharacterCount)
                .min()
                .orElse(0);
    }

    public boolean setTotemPlacementBonusFood(int bonus) {
        if (bonus < 0) {
            return false;
        }
        totemPlacementBonusFood = bonus;
        return true;
    }

    public boolean setBuilderPp(int pp) {
        if (pp < 0) {
            return false;
        }
        builderPp = pp;
        return true;
    }

}
