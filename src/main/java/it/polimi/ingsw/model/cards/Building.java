package it.polimi.ingsw.model.cards;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.cards.characters.*;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.effects.contextual.*;
import it.polimi.ingsw.model.effects.events.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * Represents a Building card, which has food cost and provides prestige points.
 * Instances of this class are intended to be created from JSON data.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
public class Building extends Card {
    private int foodCost;
    private int pp;
    private ContextualEffect effect;
    private TriggerKey triggerKey;

    /**
     * Default constructor for Building.
     * The constructor is empty as instances of this class will be populated using JSON deserialization.
     */
    public Building() {
        super();
    }

    /**
     * Constructs a fully specified Building.
     *
     * @param age        the age the building belongs to
     * @param cardId     the JSON card id used for client-side image association
     * @param foodCost   the food cost required to build it
     * @param pp         the prestige points it provides
     * @param effect     the contextual effect triggered by the building
     * @param triggerKey the key determining when the effect fires
     */
    public Building(Age age, String cardId, int foodCost, int pp, ContextualEffect effect, TriggerKey triggerKey){
        this.age = age;
        this.cardId = cardId;
        this.foodCost = foodCost;
        this.pp = pp;
        this.effect = effect;
        this.triggerKey = triggerKey;

    }

    /**
     * Constructor for test purposes.
     * @param age The age of the building.
     */
    public Building(Age age) {
        this.age = age;
    }

    /**
     * Triggers the effect of the building.
     *
     * @param p     The player triggering the effect.
     * @param state The game state.
     */
    public boolean triggerBuildingEffect(Player p, GameState state) {
        if (effect == null || p == null || state == null) {
            return false;
        }
        return effect.executeEffect(p, state);
    }

    /**
     * Gets the food cost required to build this building.
     *
     * @return The food cost.
     */
    public int getFoodCost() {
        return this.foodCost;
    }

    /**
     * Gets the prestige points (PP) provided by this building.
     *
     * @return The prestige points.
     */
    public int getPP() {
        return this.pp;
    }

    /**
     * Gets the trigger key that determines when this building's effect fires.
     *
     * @return The trigger key.
     */
    @Override
    public Optional<TriggerKey> getTriggerKey() {
        return Optional.ofNullable(triggerKey);
    }

    /**
     * Method triggered when the building is added to a player.
     * For one-shot buildings (triggerKey == ON_ACQUIRE), the effect is executed immediately.
     *
     * @param p The player adding the building.
     */
    public boolean onAddedToPlayer(Player p) {
        if (effect == null || p == null) {
            return false;
        }
        return effect.onAddedToPlayer(p);
    }

    /**
     * Registers this building into the deck manager's building collection
     * (Visitor pattern dispatch).
     *
     * @param manager the deck manager to add this building to
     * @return true if added, false if the age is not set
     */
    @Override
    public boolean addToDeck(Decks manager) {
        if (this.age == null) return false;
        manager.addBuilding(this);
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link CardCategory#BUILDING}
     */
    @Override
    public CardCategory getCategory() {
        return CardCategory.BUILDING;
    }

    /**
     * Returns a human-readable representation of this building.
     *
     * @return a string describing age, food cost, prestige points and effect
     */
    @Override
    public String toString(){
        return "BUILDING, " + super.age + ", FOOD COST: " + foodCost + ", PP: " + pp + ", EFFECT: " + effect.toString() + "\n";
    }
}
