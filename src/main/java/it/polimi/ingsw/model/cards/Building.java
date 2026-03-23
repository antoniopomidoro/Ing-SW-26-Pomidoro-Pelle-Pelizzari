package it.polimi.ingsw.model.cards;

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


/**
 * Represents a Building card, which has food cost and provides prestige points.
 * Instances of this class are intended to be created from JSON data.
 */
public class Building extends Card {
    private String id;
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

    public Building(Age age, String id, int foodCost, int pp, ContextualEffect effect, TriggerKey triggerKey){
        this.age = age;
        this.id = id;
        this.foodCost = foodCost;
        this.pp = pp;
        this.effect = effect;
        this.triggerKey = triggerKey;

    }

    /**
     * Gets the ID of the building.
     *
     * @return The building ID.
     */
    public String getId() {
        return id;
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
    public TriggerKey getTriggerKey() {
        return triggerKey;
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

    @Override
    public Building copy() {
        return new Building(this.age, this.id, this.foodCost, this.pp, this.effect, this.triggerKey);
    }
    @Override
    public boolean addToDeck(Decks manager) {
        if (this.age == null) return false;
        manager.addBuilding(this);
        return true;
    }
}
