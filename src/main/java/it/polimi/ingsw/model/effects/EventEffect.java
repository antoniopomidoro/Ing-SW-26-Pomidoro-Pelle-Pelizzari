package it.polimi.ingsw.model.effects;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.polimi.ingsw.model.effects.events.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;


import java.util.List;

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
public interface EventEffect {

    /**
     * Core event logic — each concrete effect implements only this.
     * Must NOT trigger buildings; that's handled by the template method.
     */
    boolean applyEffect(List<Player> players, GameState state, Age age);

    /**
     * Template method: executes the event effect, then publishes the
     * corresponding trigger so that matching buildings are activated.
     * Concrete classes should NOT override this.
     */
    default boolean executeEffect(List<Player> players, GameState state, TriggerKey triggerKey, Age age) {
        if (players == null || state == null || triggerKey == null || age == null) {
            return false;
        }
        if (!applyEffect(players, state, age)) {
            return false;
        }
        return state.publishTrigger(triggerKey);
    }
}
