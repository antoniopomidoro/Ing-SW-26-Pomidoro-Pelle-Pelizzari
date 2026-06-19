package it.polimi.ingsw.model.player;

import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.cards.characters.*;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.effects.contextual.*;
import it.polimi.ingsw.model.effects.events.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;


import java.io.Serializable;
import java.util.*;

/**
 * Aggregates the derived statistics of a {@link Player}: per-character counts,
 * discounts, ritual modifiers, stars, builder prestige points, collected tools
 * and various bonuses. These values are accumulated as cards and buildings are
 * acquired and are consulted by effects and end-game scoring.
 */
public class PlayerStats implements Serializable {
    private Map<CharacterEnum, Integer> characterCounts = new EnumMap<>(CharacterEnum.class);
    private int buildingDiscount;
    private int ritualLossMultiplier = 1;
    private int baseSustainmentDiscount;
    private Map<CharacterEnum, Integer> sustainmentBoosts = new EnumMap<>(CharacterEnum.class);
    private int stars;
    private int builderPp;
    private int ritualWinBoost = 1;
    private int extraUpperPick = 0;
    private int totemPlacementBonus = 0;
    private Set<Tool> uniqueTools = EnumSet.noneOf(Tool.class);

    /**
     * Increments the owned count for the given character type.
     *
     * @param type the character type
     * @return true if incremented, false if the type is null
     */
    public boolean incrementCharacter(CharacterEnum type) {
        if(type == null) return false;
        characterCounts.put(type, characterCounts.getOrDefault(type, 0) + 1);
        return true;
    }

    /**
     * Returns how many cards of the given character type the player owns.
     *
     * @param type the character type
     * @return the owned count, or 0 if the type is null or unseen
     */
    public int getCharacterCount(CharacterEnum type) {
        if(type == null) return 0;
        return characterCounts.getOrDefault(type, 0);
    }

    /**
     * Returns the number of inventor cards that are not matched by a distinct
     * tool, i.e. the count of duplicate (paired) inventors.
     *
     * @return the number of equal inventor pairs
     */
    public int getEqualInventorPair() {
        int count = uniqueTools.size();
        return getCharacterCount(CharacterEnum.INVENTOR) - count;
    }

    /**
     * Increases the building discount by the given amount.
     *
     * @param amount the discount to add
     * @return true
     */
    public boolean addBuildingDiscount(int amount) {
        this.buildingDiscount += amount;
        return true;
    }

    /**
     * Returns the accumulated building discount.
     *
     * @return the building discount
     */
    public int getBuildingDiscount() {
        return buildingDiscount;
    }

    /**
     * Sets the ritual loss multiplier applied to prestige penalties.
     *
     * @param mult the multiplier (must be non-negative)
     * @return true if set, false if the value is negative
     */
    public boolean setRitualLossMultiplier(int mult) {
        if(mult < 0) return false;
        this.ritualLossMultiplier = mult;
        return true;
    }

    /**
     * Returns the ritual loss multiplier.
     *
     * @return the ritual loss multiplier
     */
    public int getRitualLossMultiplier() {
        return ritualLossMultiplier;
    }

    /**
     * Increases the base sustainment discount by the given amount.
     *
     * @param amount the discount to add
     * @return true
     */
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

    /**
     * Adds stars to the player's total.
     *
     * @param amount the number of stars to add
     * @return true
     */
    public boolean addStars(int amount) {
        this.stars += amount;
        return true;
    }

    /**
     * Returns the player's star count.
     *
     * @return the number of stars
     */
    public int getStars() {
        return stars;
    }

    /**
     * Adds to the accumulated builder prestige points.
     *
     * @param amount the prestige points to add
     * @return true
     */
    public boolean addBuilderPp(int amount) {
        this.builderPp += amount;
        return true;
    }

    /**
     * Returns the accumulated builder prestige points.
     *
     * @return the builder prestige points
     */
    public int getBuilderPp() {
        return builderPp;
    }

    /**
     * Sets the ritual win boost applied to prestige rewards.
     *
     * @param amount the win boost factor
     * @return true
     */
    public boolean setRitualWinBoost(int amount) {
        this.ritualWinBoost = amount;
        return true;
    }

    /**
     * Returns the ritual win boost.
     *
     * @return the ritual win boost
     */
    public int getRitualWinBoost() {
        return ritualWinBoost;
    }

    /**
     * Records a tool as owned. Tools are kept as a set, so duplicates do not
     * increase the distinct count.
     *
     * @param tool the tool to record
     * @return true
     */
    public boolean incrementTool(Tool tool) {
        uniqueTools.add(tool);
        return true;
    }

    /**
     * Returns the number of distinct tools owned.
     *
     * @return the distinct tool count
     */
    public int getDifferentToolNumber() {
        return uniqueTools.size();
    }

    /**
     * Returns an unmodifiable view of the distinct tools owned.
     *
     * @return the set of owned tools
     */
    public Set<Tool> getOwnedTools() {
        return Collections.unmodifiableSet(uniqueTools);
    }

    /**
     * Returns the extra top-row picks granted to the player.
     *
     * @return the extra upper picks
     */
    public int getExtraUpperPick() {
        return extraUpperPick;
    }

    /**
     * Sets the extra top-row picks granted to the player.
     *
     * @param extraUpperPick the extra picks (must be non-negative)
     * @return true if set, false if the value is negative
     */
    public boolean setExtraUpperPick(int extraUpperPick) {
        if (extraUpperPick < 0) {
            return false;
        }
        this.extraUpperPick = extraUpperPick;
        return true;
    }

    /**
     * Computes the number of complete character sets the player owns, i.e. the
     * minimum count across all character types.
     *
     * @return the number of complete sets
     */
    public int calculateSet() {
        return Arrays.stream(CharacterEnum.values())
                .mapToInt(this::getCharacterCount)
                .min()
                .orElse(0);
    }

    /**
     * Sets the food bonus granted on totem placement.
     *
     * @param bonus the food bonus (must be non-negative)
     * @return true if set, false if the value is negative
     */
    public boolean setTotemPlacementBonusFood(int bonus) {
        if (bonus < 0) {
            return false;
        }
        totemPlacementBonus = bonus;
        return true;
    }

    /**
     * Replaces the accumulated builder prestige points (e.g. after applying a
     * multiplier).
     *
     * @param pp the new builder prestige points (must be non-negative)
     * @return true if set, false if the value is negative
     */
    public boolean setBuilderPp(int pp) {
        if (pp < 0) {
            return false;
        }
        builderPp = pp;
        return true;
    }

    /**
     * Returns the number of pairs the player owns of the given character type.
     *
     * @param type the character type
     * @return the number of pairs of that type
     */
    public int getCharacterPair(CharacterEnum type){
        return this.getCharacterCount(type) / 2;
    }

    /**
     * Returns the food bonus granted on totem placement.
     *
     * @return the totem placement food bonus
     */
    public int getTotemPlacementBonus() {
        return totemPlacementBonus;
    }

    /**
     * Sets the totem placement bonus directly (save/load restore).
     *
     * @param totemPlacementBonus the bonus to set
     * @return true
     */
    public boolean setTotemPlacementBonus(int totemPlacementBonus) {
        this.totemPlacementBonus = totemPlacementBonus;
        return true;
    }
}
