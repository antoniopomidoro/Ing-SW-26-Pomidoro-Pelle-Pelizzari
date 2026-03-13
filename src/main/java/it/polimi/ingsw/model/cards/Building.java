package it.polimi.ingsw.model.cards;

import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.cards.characters.*;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.effects.contextual.*;
import it.polimi.ingsw.model.effects.events.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;


/**
 * Represents a Building card, which has a food cost and provides prestige points.
 * Instances of this class are intended to be created from JSON data.
 */
public class Building extends Card {
    private String id;
    private int foodCost;
    private int pp;
    private ContextualEffect effect;
    private GamePhase triggerPhase;

    /**
     * Default constructor for Building.
     * The constructor is empty as instances of this class will be populated using JSON deserialization.
     */
    public Building() {
        super();
    }

    /**
     * Gets the ID of the building.
     * @return The building ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Triggers the effect of the building.
     * @param p The player triggering the effect.
     * @param state The game state.
     */
    public void triggerBuildingEffect(Player p, GameState state) {
        effect.executeEffect(p, state);
    }

    /**
     * Gets the food cost required to build this building.
     * @return The food cost.
     */
    public int getFoodCost() {
        return this.foodCost;
    }

    /**
     * Gets the prestige points (PP) provided by this building.
     * @return The prestige points.
     */
    public int getPP() {
        return this.pp;
    }

    /**
     * Gets the phase in which this building's effect is triggered.
     * @return The trigger phase.
     */
    public GamePhase getTriggerPhase() {
        return triggerPhase;
    }

    /**
     * Method triggered when the building is added to a player.
     * @param p The player adding the building.
     */
    public void onAddedToPlayer(Player p) {
        // Skeleton method
    }
}
