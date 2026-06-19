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

    /**
     * Constructs a fully specified Event.
     *
     * @param age                the age the event belongs to
     * @param cardId             the JSON card id used for client-side image association
     * @param effect             the effect executed when the event is discarded
     * @param triggerKey         the key identifying when the event resolves
     * @param resolutionPriority the discard resolution priority (lower resolves first)
     */
    public Event(Age age, String cardId, EventEffect effect, TriggerKey triggerKey, int resolutionPriority){
        this.age = age;
        this.cardId = cardId;
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
     * Trigger key associated with this event, if any.
     *
     * @return an Optional containing the TriggerKey, or empty if none is set
     */
    @Override
    public java.util.Optional<it.polimi.ingsw.model.game.TriggerKey> getTriggerKey() {
        return java.util.Optional.ofNullable(triggerKey);
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
    /**
     * Events are never buyable by players.
     *
     * @return false
     */
    @Override
    public boolean isBuyable(){
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link CardCategory#EVENT}
     */
    @Override
    public CardCategory getCategory() {
        return CardCategory.EVENT;
    }

    /**
     * Returns a human-readable representation of this event.
     *
     * @return a string describing age and effect
     */
    @Override
    public String toString(){
        return "EVENT, " + super.age + ", EFFECT: " + effect.toString() + "\n";
    }

}
