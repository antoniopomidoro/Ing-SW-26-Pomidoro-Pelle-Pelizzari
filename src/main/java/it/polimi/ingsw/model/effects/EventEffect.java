package it.polimi.ingsw.model.effects;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.polimi.ingsw.model.effects.events.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;

import java.io.Serializable;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "effectType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Sustenance.class, name = "SUSTENANCE"),
        @JsonSubTypes.Type(value = Hunting.class, name = "HUNTING"),
        @JsonSubTypes.Type(value = ShamanicRitual.class, name = "SHAMANIC_RITUAL"),
        @JsonSubTypes.Type(value = CavePaintings.class, name = "CAVE_PAINTINGS")
})
/**
 * Strategy interface for the effects carried by event cards. Concrete
 * implementations are resolved polymorphically via Jackson {@code @JsonSubTypes}.
 * Implementations provide only the core logic in {@link #applyEffect}; the
 * shared {@link #executeEffect} template handles the building-trigger publishing.
 */
public interface EventEffect extends Serializable {

    /**
     * Core event logic — each concrete effect implements only this.
     * Players are retrieved from state.getOrderTileOrder().
     * Must NOT trigger buildings; that's handled by the template method.
     *
     * @param state the current game state
     * @param age   the age in which the event resolves
     * @return true if the effect was applied
     */
    boolean applyEffect(GameState state, Age age);

    /**
     * Template method: executes the event effect, then publishes the
     * corresponding trigger so that matching buildings are activated.
     * Concrete classes should NOT override this.
     *
     * @param state      the current game state
     * @param triggerKey the trigger to publish after the effect runs
     * @param age        the age in which the event resolves
     * @return true if the effect was applied and the trigger published,
     *         false if any argument is null or the effect failed
     */
    default boolean executeEffect(GameState state, TriggerKey triggerKey, Age age) {
        if (state == null || triggerKey == null || age == null) {
            return false;
        }
        if (!applyEffect(state, age)) {
            return false;
        }
        return state.publishTrigger(triggerKey);
    }
}
