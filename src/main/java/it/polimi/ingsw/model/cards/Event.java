package it.polimi.ingsw.model.cards;

import it.polimi.ingsw.model.cards.characters.Builder;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;


import java.util.List;

/**
 * Represents an Event card.
 * Instances of this class are intended to be created from JSON data.
 */
public class Event extends Card {
    private String id;
    private EventEffect effect;
    private TriggerKey triggerKey;
    private int resolutionPriority;

    /**
     * Default constructor for Event.
     * The constructor is empty as instances of this class will be populated using JSON deserialization.
     */
    public Event() {
        super();
    }

    public Event(Age age, String id, EventEffect effect, TriggerKey triggerKey, int resolutionPriority){
        this.age = age;
        this.id = id;
        this.effect = effect;
        this.triggerKey = triggerKey;
        this.resolutionPriority = resolutionPriority;
    }


    /**
     * Returns the resolution priority of this event.
     * Events with lower priority are resolved first during discard.
     * Sustenance events have higher priority so they are resolved last.
     * @return The resolution priority.
     */
    @Override
    public int getResolutionPriority() {
        return resolutionPriority;
    }

    /**
     * Gets the ID of the event.
     * @return The event ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Handles the logic when the event card is discarded (triggered).
     * @param state The current game state.
     * @return True if the event was executed successfully.
     */
    @Override
    public boolean onDiscard(GameState state){
        if (effect == null) {
            return false;
        }
        boolean result = effect.executeEffect(state, triggerKey, this.getAge());
        if (result) {
            state.raiseEvent(new GameEvent(GameEvent.Type.EVENT_CARD_TRIGGERED, null, triggerKey));
        }
        return result;
    }
    @Override
    public boolean isBuyable(){
        return false;
    }
    @Override
    public CardCategory getCategory() {
        return CardCategory.EVENT;
    }
    @Override
    public String toString(){
        return "EVENT, " + super.age + ", EFFECT: " + effect.toString() + "\n";
    }

}
